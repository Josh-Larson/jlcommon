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
package me.joshlarson.jlcommon.concurrency;

import java.util.concurrent.atomic.AtomicBoolean;

class ThreadRunningProtector {
	
	private final AtomicBoolean running;
	private boolean created;
	
	public ThreadRunningProtector() {
		this.running = new AtomicBoolean(false);
		this.created = false;
	}
	
	/**
	 * Marks the thread(s) as started
	 * @return TRUE if valid operation, FALSE otherwise
	 */
	public boolean start() {
		created = true;
		assert !running.get() : "Thread has already been started";
		return running.getAndSet(true);
	}
	
	/**
	 * Marks the thread(s) as stopped
	 * @return TRUE if valid operation, FALSE otherwise
	 */
	public boolean stop() {
		assert running.get() : "Thread is not running";
		return running.getAndSet(false);
	}
	
	/**
	 * Asserts that the thread(s) have been created
	 * @return TRUE if the thread(s) have been created, FALSE otherwise
	 */
	public boolean expectCreated() {
		assert created : "Thread has not been started yet";
		return hasBeenCreated();
	}
	
	/**
	 * Asserts that the thread(s) are running
	 * @return TRUE if the thread(s) are running, FALSE otherwise
	 */
	public boolean expectRunning() {
		assert running.get() : "Thread has not been started yet";
		return running.get();
	}
	
	/**
	 * Returns whether or not the thread(s) have ever been started
	 * @return TRUE if the thread(s) have ever beeen started, FALSE otherwise
	 */
	public boolean hasBeenCreated() {
		return created;
	}
	
	/**
	 * Returns whether or not the thread(s) are running
	 * @return TRUE if the thread(s) are running, FALSE otherwise
	 */
	public boolean isRunning() {
		return running.get();
	}
	
}
