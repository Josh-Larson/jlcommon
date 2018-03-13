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
import java.util.ArrayList;
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
		children = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Initializes this manager. If the manager returns false on this method then the initialization failed and may not work as intended. This will initialize all children automatically.
	 *
	 * @return TRUE if initialization was successful, FALSE otherwise
	 */
	@Override
	public boolean initialize() {
		boolean success = super.initialize();
		for (Service child : children) {
			if (!child.initialize()) {
				Log.e(child.getClass().getSimpleName() + " failed to initialize!");
				success = false;
				break;
			}
			initialized.add(child);
		}
		return success;
	}
	
	/**
	 * Starts this manager. If the manager returns false on this method then the manger failed to start and may not work as intended. This will start all children automatically.
	 *
	 * @return TRUE if starting was successful, FALSE otherwise
	 */
	@Override
	public boolean start() {
		boolean success = super.start();
		for (Service child : children) {
			if (!child.start()) {
				Log.e(child.getClass().getSimpleName() + " failed to start!");
				success = false;
				break;
			}
			started.add(child);
		}
		return success;
	}
	
	/**
	 * Stops this manager. If the manager returns false on this method then the manger failed to stop and may not have fully locked down. This will start all children automatically.
	 *
	 * @return TRUE if stopping was successful, FALSE otherwise
	 */
	@Override
	public boolean stop() {
		boolean success = super.stop();
		for (Service child : started) {
			if (!child.stop()) {
				Log.e(child.getClass().getSimpleName() + " failed to stop!");
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
	public boolean terminate() {
		boolean success = super.terminate();
		for (Service child : initialized) {
			if (!child.terminate())
				success = false;
		}
		return success;
	}
	
	/**
	 * Determines whether or not this manager is operational
	 *
	 * @return TRUE if this manager is operational, FALSE otherwise
	 */
	@Override
	public boolean isOperational() {
		boolean success = true;
		for (Service child : children) {
			if (!child.isOperational())
				success = false;
		}
		return success;
	}
	
	/**
	 * Adds a child to the manager's list of children. This creates a tree of managers that allows information to propogate freely through the network in an easy way.
	 *
	 * @param service the service to add as a child.
	 */
	public void addChildService(@Nonnull Service service) {
		for (Service child : children) {
			if (service == child || service.equals(child))
				return;
		}
		children.add(service);
		IntentManager manager = getIntentManager();
		if (manager != null)
			service.setIntentManager(manager);
	}
	
	/**
	 * Removes the sub-manager from the list of children
	 *
	 * @param service the service to remove
	 */
	public void removeChildService(@Nonnull Service service) {
		children.remove(service);
	}
	
	/**
	 * Returns a copied ArrayList of the children of this manager
	 *
	 * @return a copied ArrayList of the children of this manager
	 */
	@Nonnull
	public List<Service> getManagerChildren() {
		return new ArrayList<>(children);
	}
	
	@Override
	public void setIntentManager(@Nonnull IntentManager intentManager) {
		super.setIntentManager(intentManager);
		for (Service s : children) {
			s.setIntentManager(intentManager);
		}
	}
	
}
