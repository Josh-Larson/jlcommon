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

import java.util.Collection;

public abstract class Endpoint extends Manager {
	
	public final boolean startEndpoint() {
		return initialize() && start();
	}
	
	public final void stopEndpoint() {
		stop();
		terminate();
	}
	
	/**
	 * Starts, runs, and then gracefully stops all endpoints within the collection
	 *
	 * @param endpoints the collection of endpoints
	 */
	public static void startRunStop(Collection<Endpoint> endpoints) {
		if (start(endpoints))
			run(endpoints);
		stop(endpoints);
	}
	
	/**
	 * Starts, runs, and then gracefully stops all endpoints within the collection
	 *
	 * @param endpoints         the collection of endpoints
	 * @param periodicSleepTime the time between isOperational checks during the run phase
	 */
	public static void startRunStop(Collection<Endpoint> endpoints, long periodicSleepTime) {
		if (start(endpoints))
			run(endpoints, periodicSleepTime);
		stop(endpoints);
	}
	
	/**
	 * Attempts to start each of the endpoints in the collection
	 *
	 * @param endpoints the collection of endpoints
	 * @return TRUE if each endpoint was successfully started, FALSE otherwise
	 */
	public static boolean start(Collection<Endpoint> endpoints) {
		Log.i("Starting...");
		for (Endpoint e : endpoints) {
			try {
				if (!e.startEndpoint()) {
					Log.e("Failed to start endpoint: %s", e.getClass().getName());
					return false;
				}
			} catch (Throwable t) {
				Log.e("Caught exception during start. Endpoint: %s", e.getClass().getName());
				Log.e(t);
				return false;
			}
		}
		Log.i("Started.");
		return true;
	}
	
	/**
	 * Runs each of the endpoints in the collection with the default periodicSleepTime of 100ms
	 *
	 * @param endpoints the collection of endpoints
	 */
	public static void run(Collection<Endpoint> endpoints) {
		run(endpoints, 100);
	}
	
	/**
	 * Runs each of the endpoints in the collection with the specified periodicSleepTime
	 *
	 * @param endpoints         the collection of endpoints
	 * @param periodicSleepTime the time to sleep between isOperational calls
	 */
	public static void run(Collection<Endpoint> endpoints, long periodicSleepTime) {
		while (Delay.sleepMilli(periodicSleepTime)) {
			for (Endpoint e : endpoints) {
				try {
					if (!e.isOperational()) {
						Log.e("Endpoint '%s' is no longer operational.", e.getClass().getName());
						return;
					}
				} catch (Throwable t) {
					Log.e("Caught exception during isOperational. Endpoint: %s", e.getClass().getName());
					Log.e(t);
					return;
				}
			}
		}
	}
	
	/**
	 * Attempts to stop each of the endpoints in the collection
	 *
	 * @param endpoints the collection of endpoints
	 */
	public static void stop(Collection<Endpoint> endpoints) {
		Log.i("Stopping...");
		for (Endpoint e : endpoints) {
			try {
				e.stopEndpoint();
			} catch (Throwable t) {
				Log.e("Caught exception during stop. Endpoint: %s", e.getClass().getName());
				Log.e(t);
			}
		}
		Log.i("Stopped.");
	}
	
}
