package me.joshlarson.jlcommon.concurrency;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

public class BasicThread {
	
	private final ThreadPool threadPool;
	private final AtomicBoolean executing;
	private final Runnable runnable;
	
	public BasicThread(@Nonnull String name, @Nonnull Runnable runnable) {
		this.threadPool = new ThreadPool(1, name);
		this.executing = new AtomicBoolean(false);
		this.runnable = runnable;
	}
	
	public void start() {
		threadPool.start();
		threadPool.execute(() -> {
			executing.set(true);
			try {
				runnable.run();
			} finally {
				executing.set(false);
			}
		});
	}
	
	public void setPriority(int priority) {
		threadPool.setPriority(priority);
	}
	
	public void stop(boolean interrupt) {
		threadPool.stop(interrupt);
	}
	
	public boolean awaitTermination(long timeout) {
		return threadPool.awaitTermination(timeout);
	}
	
	public boolean isExecuting() {
		return executing.get();
	}
	
}
