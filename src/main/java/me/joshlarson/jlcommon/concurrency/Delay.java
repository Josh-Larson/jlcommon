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

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Delay {
	
	/**
	 * Sleeps for the specified number of nanoseconds
	 *
	 * @param nanos the number of nanoseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepNano(long nanos) {
		LockSupport.parkNanos(nanos);
		return !isInterrupted();
	}
	
	/**
	 * Sleeps for the specified number of microseconds
	 *
	 * @param micro the number of microseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepMicro(long micro) {
		return sleepNano(micro * 1000);
	}
	
	/**
	 * Sleeps for the specified number of milliseconds
	 *
	 * @param milli the number of milliseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepMilli(long milli) {
		return sleepNano(milli * 1000000);
	}
	
	/**
	 * Sleeps for the specified number of seconds
	 *
	 * @param sec the number of seconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepSeconds(long sec) {
		return sleepNano(sec * 1000000000);
	}
	
	/**
	 * Sleeps for the specified amount of time
	 *
	 * @param time the amount of time to sleep
	 * @param unit the unit of time
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleep(long time, @NotNull TimeUnit unit) {
		return sleepNano(unit.toNanos(time));
	}
	
	/**
	 * Returns whether or not this thread has been interrupted
	 *
	 * @return TRUE if this thread has been interrupted, FALSE otherwise
	 */
	public static boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}
	
	/**
	 * Clears the interrupted flag so future calls to isInterrupted will return FALSE
	 */
	public static void clearInterrupted() {
		//noinspection ResultOfMethodCallIgnored - just need to clear the flag
		Thread.interrupted();
	}
	
}
