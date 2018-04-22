/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2018 Josh Larson                                                  *
 *                                                                                 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy    *
 * of this software and associated documentation files (the "Software"), to deal   *
 * in the Software without restriction, including without limitation the rights    *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 * copies of the Software, and to permit persons to whom the Software is           *
 * furnished to do so, subject to the following conditions:                        *
 *                                                                                 *
 * The above copyright notice and this permission notice shall be included in all  *
 * copies or substantial portions of the Software.                                 *
 *                                                                                 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 * SOFTWARE.                                                                       *
 ***********************************************************************************/
package me.joshlarson.jlcommon.control;

import me.joshlarson.jlcommon.concurrency.Delay;
import me.joshlarson.jlcommon.log.Log;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A Manager is a class that encourages a tree structure to services
 */
public abstract class Manager implements ServiceBase {
	
	private final List<ServiceBase> children;
	private final List<ServiceBase> initialized;
	private final List<ServiceBase> started;
	
	public Manager() {
		children = new ArrayList<>();
		initialized = new ArrayList<>();
		started = new ArrayList<>();
		
		ManagerStructure annotation = getClass().getAnnotation(ManagerStructure.class);
		if (annotation == null)
			throw new ManagerCreationException("Manager must have defined children!");
		
		Class<? extends ServiceBase>[] annotatedChildren = annotation.children();
		for (Class<? extends ServiceBase> service : annotatedChildren) {
			if (service == null)
				throw new NullPointerException("Child is null!");
			try {
				children.add(service.getConstructor().newInstance());
			} catch (NoSuchMethodException e) {
				throw new ManagerCreationException("No valid default constructor for " + service.getName());
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Initializes this manager and all children
	 *
	 * @return TRUE if initialization was successful, FALSE otherwise
	 */
	@Override
	public final boolean initialize() {
		for (ServiceBase child : children) {
			try {
				Log.t("%s: Initializing %s...", getClass().getSimpleName(), child.getClass().getSimpleName());
				if (!child.initialize()) {
					Log.e(child.getClass().getSimpleName() + " failed to initialize!");
					return false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during initialize. Service: %s", child.getClass().getName());
				Log.e(t);
				return false;
			}
			initialized.add(child);
		}
		return true;
	}
	
	/**
	 * Starts this manager and all children
	 *
	 * @return TRUE if starting was successful, FALSE otherwise
	 */
	@Override
	public final boolean start() {
		for (ServiceBase child : children) {
			try {
				Log.t("%s: Starting %s...", getClass().getSimpleName(), child.getClass().getSimpleName());
				if (!child.start()) {
					Log.e(child.getClass().getSimpleName() + " failed to start!");
					return false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during start. Service: %s", child.getClass().getName());
				Log.e(t);
				return false;
			}
			started.add(child);
		}
		return true;
	}
	
	/**
	 * Stops this manager and all children
	 *
	 * @return TRUE if stopping was successful, FALSE otherwise
	 */
	@Override
	public final boolean stop() {
		boolean success = true;
		for (ServiceBase child : started) {
			try {
				Log.t("%s: Stopping %s...", getClass().getSimpleName(), child.getClass().getSimpleName());
				if (!child.stop()) {
					Log.e(child.getClass().getSimpleName() + " failed to stop!");
					success = false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during stop. Service: %s", child.getClass().getName());
				Log.e(t);
				success = false;
			}
		}
		started.clear();
		return success;
	}
	
	/**
	 * Terminates this manager and all children
	 *
	 * @return TRUE if termination was successful, FALSE otherwise
	 */
	@Override
	public final boolean terminate() {
		boolean success = true;
		for (ServiceBase child : initialized) {
			try {
				Log.t("%s: Terminating %s...", getClass().getSimpleName(), child.getClass().getSimpleName());
				if (!child.terminate()) {
					Log.e(child.getClass().getSimpleName() + " failed to terminate!");
					success = false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during terminate. Service: %s", child.getClass().getName());
				Log.e(t);
				success = false;
			}
		}
		initialized.clear();
		return success;
	}
	
	/**
	 * Determines whether or not this manager and all children are operational
	 *
	 * @return TRUE if this manager is operational, FALSE otherwise
	 */
	@Override
	public final boolean isOperational() {
		for (ServiceBase child : children) {
			if (!child.isOperational()) {
				Log.e("Child '%s' is no longer operational.", child.getClass().getName());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Sets the intent registry for each child in the manager
	 *
	 * @param intentManager intentManager
	 */
	@Override
	public void setIntentManager(IntentManager intentManager) {
		for (ServiceBase s : children) {
			s.setIntentManager(intentManager);
		}
	}
	
	/**
	 * Returns a list of each child in an unmodifiable list
	 *
	 * @return the unmodifiable list of children
	 */
	@Nonnull
	public final List<ServiceBase> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/**
	 * Starts, runs, and then gracefully stops all services within the list
	 *
	 * @param services the list of services
	 */
	public static void startRunStop(ServiceBase ... services) {
		startRunStop(Arrays.asList(services));
	}
	
	/**
	 * Starts, runs, and then gracefully stops all services within the collection
	 *
	 * @param services the collection of services
	 */
	public static void startRunStop(Collection<ServiceBase> services) {
		if (start(services))
			run(services);
		stop(services);
	}
	
	/**
	 * Starts, runs, and then gracefully stops all services within the list
	 *
	 * @param periodicSleepTime the time between isOperational checks during the run phase
	 * @param services          the list of services
	 */
	public static void startRunStop(long periodicSleepTime, ServiceBase ... services) {
		startRunStop(periodicSleepTime, Arrays.asList(services));
	}
	
	/**
	 * Starts, runs, and then gracefully stops all services within the collection
	 *
	 * @param periodicSleepTime the time between isOperational checks during the run phase
	 * @param services          the collection of services
	 */
	public static void startRunStop(long periodicSleepTime, Collection<ServiceBase> services) {
		if (start(services))
			run(services, periodicSleepTime);
		stop(services);
	}
	
	/**
	 * Attempts to start each of the services in the collection
	 *
	 * @param services the collection of services
	 * @return TRUE if each endpoint was successfully started, FALSE otherwise
	 */
	public static boolean start(Collection<ServiceBase> services) {
		Log.i("Starting...");
		for (ServiceBase s : services) {
			try {
				if (!s.initialize() || !s.start()) {
					Log.e("Failed to start endpoint: %s", s.getClass().getName());
					return false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during start. Service: %s", s.getClass().getName());
				Log.e(t);
				return false;
			}
		}
		Log.i("Started.");
		return true;
	}
	
	/**
	 * Runs each of the services in the collection with the default periodicSleepTime of 100ms
	 *
	 * @param services the collection of services
	 */
	public static void run(Collection<ServiceBase> services) {
		run(services, 100);
	}
	
	/**
	 * Runs each of the services in the collection with the specified periodicSleepTime
	 *
	 * @param services          the collection of services
	 * @param periodicSleepTime the time to sleep between isOperational calls
	 */
	public static void run(Collection<ServiceBase> services, long periodicSleepTime) {
		while (Delay.sleepMilli(periodicSleepTime)) {
			for (ServiceBase s : services) {
				try {
					if (!s.isOperational()) {
						Log.e("Manager '%s' is no longer operational.", s.getClass().getName());
						return;
					}
				} catch (Throwable t) {
					Log.e("Caught exception during isOperational. Service: %s", s.getClass().getName());
					Log.e(t);
					return;
				}
			}
		}
		Delay.clearInterrupted();
	}
	
	/**
	 * Attempts to stop each of the services in the collection
	 *
	 * @param services the collection of services
	 */
	public static void stop(Collection<ServiceBase> services) {
		Log.i("Stopping...");
		for (ServiceBase s : services) {
			try {
				s.stop();
				s.terminate();
			} catch (Throwable t) {
				Log.e("Caught exception during stop. Service: %s", s.getClass().getName());
				Log.e(t);
			}
		}
		Log.i("Stopped.");
	}
	
	public static class ManagerCreationException extends RuntimeException {
		
		ManagerCreationException(String message) {
			super(message);
		}
		
	}
	
}
