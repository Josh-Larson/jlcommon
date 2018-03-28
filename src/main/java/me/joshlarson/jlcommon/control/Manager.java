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

import me.joshlarson.jlcommon.log.Log;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Manager is a class that will manage services, and generally controls the program as a whole
 */
public abstract class Manager extends Service {
	
	private final List<Service> initialized;
	private final List<Service> started;
	private final List<Service> children;
	
	public Manager() {
		initialized = new CopyOnWriteArrayList<>();
		started = new CopyOnWriteArrayList<>();
		children = new ArrayList<>();
		
		ManagerStructure annotation = getClass().getAnnotation(ManagerStructure.class);
		if (annotation == null)
			throw new ManagerCreationException("Manager must have defined children!");
		
		Class<? extends Service> [] annotatedChildren = annotation.children();
		for (Class<? extends Service> service : annotatedChildren) {
			if (service == null)
				throw new NullPointerException("Service is null!");
			try {
				children.add(service.getConstructor().newInstance());
			} catch (NoSuchMethodException e) {
				throw new ManagerCreationException("No valid default constructor for " + service.getName());
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
				throw new ManagerCreationException("Failed to instantiate service "+service.getName()+". "+e.getClass().getName() + ": " + e.getMessage());
			}
		}
	}
	
	protected final void registerIntentHandlers(@Nonnull IntentManager intentManager) {
		
	}
	
	/**
	 * Initializes this manager. If the manager returns false on this method then the initialization failed and may not work as intended. This will initialize all children automatically.
	 *
	 * @return TRUE if initialization was successful, FALSE otherwise
	 */
	@Override
	public final boolean initialize() {
		for (Service child : children) {
			try {
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
	 * Starts this manager. If the manager returns false on this method then the manger failed to start and may not work as intended. This will start all children automatically.
	 *
	 * @return TRUE if starting was successful, FALSE otherwise
	 */
	@Override
	public final boolean start() {
		for (Service child : children) {
			try {
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
	 * Stops this manager. If the manager returns false on this method then the manger failed to stop and may not have fully locked down. This will start all children automatically.
	 *
	 * @return TRUE if stopping was successful, FALSE otherwise
	 */
	@Override
	public final boolean stop() {
		boolean success = true;
		for (Service child : started) {
			try {
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
		return success;
	}
	
	/**
	 * Terminates this manager. If the manager returns false on this method then the manager failed to shut down and resources may not have been cleaned up. This will terminate all children
	 * automatically.
	 *
	 * @return TRUE if termination was successful, FALSE otherwise
	 */
	@Override
	public final boolean terminate() {
		boolean success = true;
		for (Service child : initialized) {
			try {
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
		return success;
	}
	
	/**
	 * Determines whether or not this manager is operational
	 *
	 * @return TRUE if this manager is operational, FALSE otherwise
	 */
	@Override
	public final boolean isOperational() {
		for (Service child : children) {
			if (!child.isOperational())
				return false;
		}
		return true;
	}
	
	@Nonnull
	public final List<Service> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	@Override
	public void setIntentManager(@Nonnull IntentManager intentManager) {
		super.setIntentManager(intentManager);
		for (Service s : children) {
			s.setIntentManager(intentManager);
		}
	}
	
	public static class ManagerCreationException extends RuntimeException {
		
		public ManagerCreationException(String message) {
			super(message);
		}
		
	}
	
}
