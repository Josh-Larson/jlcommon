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

import me.joshlarson.jlcommon.utilities.ThreadUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;

public class ScheduledThreadPool {
	
	private final ThreadRunningProtector running;
	private final int nThreads;
	private final ThreadFactory threadFactory;
	private ScheduledExecutorService executor;
	
	public ScheduledThreadPool(int nThreads, @NotNull String nameFormat) {
		this(nThreads, Thread.NORM_PRIORITY, nameFormat);
	}
	
	public ScheduledThreadPool(int nThreads, int priority, @NotNull String nameFormat) {
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
	
	public boolean isRunning() {
		return running.isRunning();
	}
	
	public void executeWithFixedRate(long initialDelay, long time, @NotNull Runnable runnable) {
		if (!running.expectRunning())
			return;
		executor.scheduleAtFixedRate(() -> ThreadUtilities.safeRun(runnable), initialDelay, time, TimeUnit.MILLISECONDS);
	}
	
	public void executeWithFixedDelay(long initialDelay, long time, @NotNull Runnable runnable) {
		if (!running.expectRunning())
			return;
		executor.scheduleWithFixedDelay(() -> ThreadUtilities.safeRun(runnable), initialDelay, time, TimeUnit.MILLISECONDS);
	}
	
	@Nullable
	public ScheduledFuture<?> execute(long delay, @NotNull Runnable runnable) {
		if (!running.expectRunning())
			return null;
		return executor.schedule(() -> ThreadUtilities.safeRun(runnable), delay, TimeUnit.MILLISECONDS);
	}
	
	public boolean awaitTermination(long time) {
		if (!running.expectCreated())
			return true;
		try {
			return executor.awaitTermination(time, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}
	
}
