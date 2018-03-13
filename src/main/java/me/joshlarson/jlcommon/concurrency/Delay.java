package me.joshlarson.jlcommon.concurrency;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Delay {
	
	/**
	 * Sleeps for the specified number of nanoseconds
	 *
	 * @param nanos the number of nanoseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepNano(@Nonnegative long nanos) {
		LockSupport.parkNanos(nanos);
		return !isInterrupted();
	}
	
	/**
	 * Sleeps for the specified number of microseconds
	 *
	 * @param micro the number of microseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepMicro(@Nonnegative long micro) {
		return sleepNano(micro * 1000);
	}
	
	/**
	 * Sleeps for the specified number of milliseconds
	 *
	 * @param milli the number of milliseconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepMilli(@Nonnegative long milli) {
		return sleepNano(milli * 1000000);
	}
	
	/**
	 * Sleeps for the specified number of seconds
	 *
	 * @param sec the number of seconds to sleep
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleepSeconds(@Nonnegative long sec) {
		return sleepNano(sec * 1000000000);
	}
	
	/**
	 * Sleeps for the specified amount of time
	 *
	 * @param time the amount of time to sleep
	 * @param unit the unit of time
	 * @return FALSE if this thread has been interrupted, TRUE otherwise
	 */
	public static boolean sleep(@Nonnegative long time, @Nonnull TimeUnit unit) {
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
		Thread.interrupted();
	}
	
}
