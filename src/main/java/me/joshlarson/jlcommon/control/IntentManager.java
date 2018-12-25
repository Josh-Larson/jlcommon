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

import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.concurrency.ThreadPool.PrioritizedRunnable;
import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class IntentManager implements AutoCloseable {
	
	private static final AtomicReference<IntentManager> INSTANCE = new AtomicReference<>(null);
	
	private final Map<Class<? extends Intent>, List<IntentRunner<? extends Intent>>> intentRegistrations;
	private final ThreadPool processThreads;
	private final AtomicLong queuedIntents;
	
	public IntentManager(int threadCount) {
		this(false, threadCount);
	}
	
	public IntentManager(boolean priorityScheduling, int threadCount) {
		this(priorityScheduling, threadCount, 8);
	}
	
	public IntentManager(boolean priorityScheduling, int threadCount, int priority) {
		this.intentRegistrations = new ConcurrentHashMap<>();
		this.processThreads = new ThreadPool(priorityScheduling, threadCount, "intent-processor-%d");
		this.queuedIntents = new AtomicLong(0);
		
		this.processThreads.setPriority(priority);
		this.processThreads.start();
	}
	
	public boolean isRunning() {
		return processThreads.isRunning();
	}
	
	@Override
	public void close() {
		close(true, 1000);
	}
	
	public boolean close(boolean interrupt, long timeout) {
		if (!processThreads.isRunning())
			return true;
		processThreads.stop(interrupt);
		return processThreads.awaitTermination(timeout);
	}
	
	public void setPriority(int priority) {
		processThreads.setPriority(priority);
	}
	
	public long getIntentCount() {
		return queuedIntents.get();
	}
	
	@NotNull
	public List<IntentSpeedStatistics> getSpeedRecorder() {
		return intentRegistrations.values().stream().flatMap(Collection::stream).map(IntentRunner::toSpeedStatistics).collect(toList());
	}
	
	public <E extends Intent> void broadcastIntent(@NotNull E i) {
		List<IntentRunner<? extends Intent>> receivers = intentRegistrations.get(i.getClass());
		if (receivers == null || !processThreads.isRunning() || receivers.isEmpty()) {
			i.setRemaining(0);
			i.markAsComplete(this);
			return;
		}
		
		queuedIntents.incrementAndGet();
		i.setRemaining(receivers.size());
		for (IntentRunner<? extends Intent> r : receivers)
			processThreads.execute(new IntentExecutor<>(r, i));
	}
	
	public <T extends Intent> void registerForIntent(@NotNull Class<T> c, @NotNull Object consumerKey, @NotNull Consumer<T> r) {
		intentRegistrations.computeIfAbsent(c, s -> new CopyOnWriteArrayList<>()).add(new IntentRunner<>(consumerKey, c, r));
	}
	
	@Deprecated
	public <T extends Intent> void registerForIntent(@NotNull Class<T> c, @NotNull Consumer<T> r) {
		registerForIntent(c, r, r);
	}
	
	public <T extends Intent> void unregisterForIntent(@NotNull Class<T> c, @NotNull Object consumerKey) {
		List<IntentRunner<? extends Intent>> intents = intentRegistrations.get(c);
		if (intents == null)
			return;
		intents.removeIf(runner -> runner.getKey().equals(consumerKey));
	}
	
	@Nullable
	public static IntentManager getInstance() {
		return INSTANCE.get();
	}
	
	public static void setInstance(IntentManager intentManager) {
		IntentManager prev = INSTANCE.getAndSet(intentManager);
		if (prev != null)
			prev.close();
	}
	
	public static class IntentSpeedStatistics implements Comparable<IntentSpeedStatistics> {
		
		private final Object key;
		private final Class<? extends Intent> intent;
		private final long totalTime;
		private final long count;
		private final double average;
		
		public IntentSpeedStatistics(Object key, Class<? extends Intent> intent, long totalTime, long count) {
			this.key = key;
			this.intent = intent;
			this.totalTime = totalTime;
			this.count = count;
			this.average = totalTime / (double) count;
		}
		
		@NotNull
		public Object getKey() {
			return key;
		}
		
		@NotNull
		public Class<? extends Intent> getIntent() {
			return intent;
		}
		
		public long getTotalTime() {
			return totalTime;
		}
		
		public long getCount() {
			return count;
		}
		
		public double getAverage() {
			return average;
		}
		
		@Override
		public int compareTo(@NotNull IntentManager.IntentSpeedStatistics o) {
			return Long.compare(totalTime, o.totalTime);
		}
		
	}
	
	private class IntentRunner<E extends Intent> implements Comparable<IntentRunner> {
		
		private final Object key;
		private final Class<E> intent;
		private final Consumer<E> consumer;
		private final AtomicLong time;
		private final AtomicLong count;
		
		public IntentRunner(@NotNull Object key, @NotNull Class<E> intent, @NotNull Consumer<E> consumer) {
			this.key = key;
			this.intent = intent;
			this.consumer = consumer;
			this.time = new AtomicLong(0);
			this.count = new AtomicLong(0);
		}
		
		@NotNull
		public Class<? extends Intent> getIntent() {
			return intent;
		}
		
		@NotNull
		public Object getKey() {
			return key;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends Intent> void broadcast(T intent) {
			assert intent.getClass().equals(this.intent) : "invalid intent type";
			try {
				long start = System.nanoTime();
				consumer.accept((E) intent);
				long time = System.nanoTime() - start;
				
				this.time.addAndGet(time);
				this.count.incrementAndGet();
			} catch (Throwable t) {
				Log.e("Fatal Exception while processing intent: " + intent);
				Log.e(t);
			} finally {
				if (intent.decrementRemaining(IntentManager.this)) {
					Consumer<Intent> completedCallback = intent.getCompletedCallback();
					if (completedCallback != null)
						completedCallback.accept(intent);
					queuedIntents.decrementAndGet();
				}
			}
		}
		
		public long getTime() {
			return time.get();
		}
		
		public long getCount() {
			return count.get();
		}
		
		public void addTime(long timeNanos) {
			time.addAndGet(timeNanos);
			count.incrementAndGet();
		}
		
		@NotNull
		public IntentSpeedStatistics toSpeedStatistics() {
			return new IntentSpeedStatistics(key, intent, time.get(), count.get());
		}
		
		@Override
		public int compareTo(@NotNull IntentRunner runner) {
			return Long.compare(runner.getTime(), getTime());
		}
		
	}
	
	private class IntentExecutor<E extends Intent> implements PrioritizedRunnable {
		
		private final IntentRunner<E> r;
		private final Intent i;
		
		public IntentExecutor(@NotNull IntentRunner<E> r, @NotNull Intent i) {
			this.r = r;
			this.i = i;
		}
		
		@Override
		public void run() {
			r.broadcast(i);
		}
		
		@Override
		public int compareTo(@NotNull PrioritizedRunnable r) {
			if (r instanceof IntentExecutor)
				return i.compareTo(((IntentExecutor) r).i);
			return -1;
		}
		
	}
	
}
