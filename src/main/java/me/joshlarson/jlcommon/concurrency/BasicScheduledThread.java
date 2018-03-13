package me.joshlarson.jlcommon.concurrency;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class BasicScheduledThread {
	
	private final ScheduledThreadPool threadPool;
	private final Runnable runnable;
	
	public BasicScheduledThread(@Nonnull String name, @Nonnull Runnable runnable) {
		this.threadPool = new ScheduledThreadPool(1, name);
		this.runnable = runnable;
	}
	
	public void startWithFixedRate(@Nonnegative long initialDelay, @Nonnegative long periodicDelay) {
		threadPool.start();
		threadPool.executeWithFixedRate(initialDelay, periodicDelay, runnable);
	}
	
	public void startWithFixedDelay(@Nonnegative long initialDelay, @Nonnegative long periodicDelay) {
		threadPool.start();
		threadPool.executeWithFixedDelay(initialDelay, periodicDelay, runnable);
	}
	
	public void stop() {
		threadPool.stop();
	}
	
	public boolean awaitTermination(long time) {
		return threadPool.awaitTermination(time);
	}
	
}
