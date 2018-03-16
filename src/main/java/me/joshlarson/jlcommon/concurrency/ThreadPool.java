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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
	
	private static final PrioritizedRunnable END_OF_QUEUE = new EndOfQueueTask();
	
	private final ThreadRunningProtector running;
	private final boolean priorityScheduling;
	private final int nThreads;
	private final String nameFormat;
	private final AtomicInteger priority;
	private ThreadExecutor executor;
	
	public ThreadPool(@Nonnegative int nThreads, @Nonnull String nameFormat) {
		this(false, nThreads, nameFormat);
	}
	
	public ThreadPool(boolean priorityScheduling, @Nonnegative int nThreads, @Nonnull String nameFormat) {
		this.running = new ThreadRunningProtector();
		this.priorityScheduling = priorityScheduling;
		this.nThreads = nThreads;
		this.nameFormat = nameFormat;
		this.executor = null;
		this.priority = new AtomicInteger(Thread.NORM_PRIORITY);
	}
	
	public void setPriority(@Nonnegative int priority) {
		this.priority.set(priority);
	}
	
	public void start() {
		if (!running.start())
			return;
		executor = new ThreadExecutor(priorityScheduling, nThreads, ThreadUtilities.newThreadFactory(nameFormat, priority.get()));
		executor.start();
	}
	
	public void stop(boolean interrupt) {
		if (!running.stop())
			return;
		executor.stop(interrupt);
	}
	
	public boolean awaitTermination(@Nonnegative long timeout) {
		return running.expectCreated() && executor.awaitTermination(timeout);
	}
	
	@Nonnegative
	public int getQueuedTasks() {
		return executor.getQueuedTasks();
	}
	
	public void execute(@Nonnull Runnable runnable) {
		if (running.expectRunning()) {
			if (!priorityScheduling)
				executor.execute(runnable);
			else
				throw new IllegalArgumentException("Must use Comparable<Runnable>!");
		}
	}
	
	public void execute(@Nonnull PrioritizedRunnable runnable) {
		if (running.expectRunning())
			executor.execute(runnable);
	}
	
	public boolean isRunning() {
		return running.isRunning();
	}
	
	public interface PrioritizedRunnable extends Runnable, Comparable<PrioritizedRunnable> {
		
	}
	
	private static class ThreadExecutor {
		
		private final AtomicInteger runningThreads;
		private final BlockingQueue<Runnable> tasks;
		private final List<Thread> threads;
		private final int nThreads;
		
		public ThreadExecutor(boolean priorityScheduling, @Nonnegative int nThreads, @Nonnull ThreadFactory threadFactory) {
			this.runningThreads = new AtomicInteger(0);
			if (priorityScheduling)
				this.tasks = new PriorityBlockingQueue<>();
			else
				this.tasks = new LinkedBlockingQueue<>();
			this.threads = new ArrayList<>(nThreads);
			this.nThreads = nThreads;
			for (int i = 0; i < nThreads; i++) {
				threads.add(threadFactory.newThread(this::threadExecutor));
			}
		}
		
		public void start() {
			runningThreads.set(nThreads);
			for (Thread t : threads) {
				t.start();
			}
		}
		
		public void stop(boolean interrupt) {
			for (int i = 0; i < nThreads; i++) {
				tasks.add(END_OF_QUEUE);
			}
			if (interrupt) {
				for (Thread t : threads) {
					t.interrupt();
				}
			}
		}
		
		@Nonnegative
		public int getQueuedTasks() {
			return tasks.size();
		}
		
		public void execute(@Nonnull Runnable runnable) {
			tasks.offer(runnable);
		}
		
		public boolean awaitTermination(@Nonnegative long time) {
			try {
				synchronized (runningThreads) {
					while (runningThreads.get() > 0 && time > 0) {
						long startWait = System.nanoTime();
						runningThreads.wait(time);
						time -= (long) ((System.nanoTime() - startWait) / 1E6 + 0.5);
					}
				}
			} catch (InterruptedException e) {
				return false;
			}
			return runningThreads.get() == 0;
		}
		
		private void threadExecutor() {
			try {
				Runnable task = null;
				while (task != END_OF_QUEUE) {
					task = tasks.take();
					ThreadUtilities.safeRun(task);
				}
			} catch (InterruptedException e) {
				// Suppressed
			} finally {
				synchronized (runningThreads) {
					runningThreads.decrementAndGet();
					runningThreads.notifyAll();
				}
			}
		}
		
	}
	
	private static class EndOfQueueTask implements PrioritizedRunnable {
		
		@Override
		public void run() {
			
		}
		
		@Override
		public int compareTo(@Nonnull PrioritizedRunnable o) {
			return 1;
		}
		
	}
	
}
