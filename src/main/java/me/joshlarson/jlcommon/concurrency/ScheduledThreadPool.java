package me.joshlarson.jlcommon.concurrency;

import me.joshlarson.jlcommon.utilities.ThreadUtilities;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.*;

public class ScheduledThreadPool {
	
	private final ThreadRunningProtector running;
	private final int nThreads;
	private final ThreadFactory threadFactory;
	private ScheduledExecutorService executor;
	
	public ScheduledThreadPool(@Nonnegative int nThreads, @Nonnull String nameFormat) {
		this(nThreads, Thread.NORM_PRIORITY, nameFormat);
	}
	
	public ScheduledThreadPool(@Nonnegative int nThreads, @Nonnegative int priority, @Nonnull String nameFormat) {
		this.running = new ThreadRunningProtector();
		this.nThreads = nThreads;
		this.threadFactory = ThreadUtilities.newThreadFactory(nameFormat, priority);
		this.executor = null;
	}
	
	public void start() {
		if (!running.start())
			return;
		executor = Executors.newScheduledThreadPool(nThreads, threadFactory);
	}
	
	public void stop() {
		if (!running.stop())
			return;
		executor.shutdownNow();
	}
	
	public void executeWithFixedRate(@Nonnegative long initialDelay, @Nonnegative long time, @Nonnull Runnable runnable) {
		if (!running.expectRunning())
			return;
		executor.scheduleAtFixedRate(() -> ThreadUtilities.safeRun(runnable), initialDelay, time, TimeUnit.MILLISECONDS);
	}
	
	public void executeWithFixedDelay(@Nonnegative long initialDelay, @Nonnegative long time, @Nonnull Runnable runnable) {
		if (!running.expectRunning())
			return;
		executor.scheduleWithFixedDelay(() -> ThreadUtilities.safeRun(runnable), initialDelay, time, TimeUnit.MILLISECONDS);
	}
	
	@CheckForNull
	public ScheduledFuture<?> execute(@Nonnegative long delay, @Nonnull Runnable runnable) {
		if (!running.expectRunning())
			return null;
		return executor.schedule(() -> ThreadUtilities.safeRun(runnable), delay, TimeUnit.MILLISECONDS);
	}
	
	public boolean awaitTermination(@Nonnegative long time) {
		if (!running.expectCreated())
			return true;
		try {
			return executor.awaitTermination(time, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}
	
}
