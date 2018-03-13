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
