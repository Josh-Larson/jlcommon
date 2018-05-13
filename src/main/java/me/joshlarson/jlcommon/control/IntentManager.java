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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class IntentManager implements IntentRegistry {
	
	private static final AtomicReference<IntentManager> INSTANCE = new AtomicReference<>(null);
	
	private final Map<Class<? extends Intent>, List<Consumer<? extends Intent>>> intentRegistrations;
	private final IntentSpeedRecorder speedRecorder;
	private final ThreadPool processThreads;
	
	public IntentManager(int threadCount) {
		this(false, threadCount);
	}
	
	public IntentManager(boolean priorityScheduling, int threadCount) {
		this.intentRegistrations = new ConcurrentHashMap<>();
		this.speedRecorder = new IntentSpeedRecorder();
		this.processThreads = new ThreadPool(priorityScheduling, threadCount, "intent-processor-%d");
		
		this.processThreads.setPriority(8);
	}
	
	public void initialize() {
		processThreads.start();
	}
	
	public void terminate() {
		terminate(true);
	}
	
	public void terminate(boolean interrupt) {
		processThreads.stop(interrupt);
		processThreads.awaitTermination(500);
	}
	
	public void setPriority(int priority) {
		processThreads.setPriority(priority);
	}
	
	public int getIntentCount() {
		return processThreads.getQueuedTasks();
	}
	
	@NotNull
	public IntentSpeedRecorder getSpeedRecorder() {
		return speedRecorder;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Intent> void broadcastIntent(@NotNull E i) {
		List<Consumer<? extends Intent>> receivers = intentRegistrations.get(i.getClass());
		if (receivers == null || !processThreads.isRunning() || receivers.isEmpty()) {
			i.markAsComplete(this);
			return;
		}
		
		AtomicInteger remaining = new AtomicInteger(receivers.size());
		receivers.forEach(r -> processThreads.execute(new IntentRunner<>((Consumer<E>) r, i, remaining)));
	}
	
	@Override
	public <T extends Intent> void registerForIntent(@NotNull Class<T> c, @NotNull Consumer<T> r) {
		intentRegistrations.computeIfAbsent(c, s -> new CopyOnWriteArrayList<>()).add(r);
	}
	
	@Override
	public <T extends Intent> void unregisterForIntent(@NotNull Class<T> c, @NotNull Consumer<T> r) {
		List<Consumer<? extends Intent>> intents = intentRegistrations.get(c);
		if (intents == null)
			return;
		intents.remove(r);
	}
	
	@Nullable
	public static IntentManager getInstance() {
		return INSTANCE.get();
	}
	
	public static void setInstance(IntentManager intentManager) {
		IntentManager prev = INSTANCE.getAndSet(intentManager);
		if (prev != null && prev.processThreads.isRunning())
			prev.terminate();
	}
	
	public static class IntentSpeedRecorder {
		
		private final Map<Consumer<? extends Intent>, IntentSpeedRecord> times;
		
		public IntentSpeedRecorder() {
			this.times = new ConcurrentHashMap<>();
		}
		
		private <E extends Intent> void addRecord(@NotNull Class<E> intent, @NotNull Consumer<E> consumer, long timeNanos) {
			IntentSpeedRecord record = times.computeIfAbsent(consumer, s -> new IntentSpeedRecord(intent, consumer));
			record.addTime(timeNanos);
		}
		
		@Nullable
		public IntentSpeedRecord getTime(@NotNull Consumer<? extends Intent> consumer) {
			return times.get(consumer);
		}
		
		@NotNull
		public List<IntentSpeedRecord> getAllTimes() {
			return new ArrayList<>(times.values());
		}
		
	}
	
	public static class IntentSpeedRecord implements Comparable<IntentSpeedRecord> {
		
		private final Class<? extends Intent> intent;
		private final Consumer<? extends Intent> consumer;
		private final AtomicLong time;
		private final AtomicLong count;
		
		public IntentSpeedRecord(@NotNull Class<? extends Intent> intent, @NotNull Consumer<? extends Intent> consumer) {
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
		public Consumer<? extends Intent> getConsumer() {
			return consumer;
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
		
		@Override
		public int compareTo(@NotNull IntentSpeedRecord record) {
			return Long.compare(record.getTime(), getTime());
		}
		
	}
	
	private class IntentRunner<E extends Intent> implements PrioritizedRunnable {
		
		private final Consumer<E> r;
		private final E i;
		private final AtomicInteger remaining;
		
		public IntentRunner(@NotNull Consumer<E> r, @NotNull E i, @NotNull AtomicInteger remaining) {
			this.r = r;
			this.i = i;
			this.remaining = remaining;
		}
		
		@Override
		public void run() {
			try {
				long start = System.nanoTime();
				r.accept(i);
				long time = System.nanoTime() - start;
				@SuppressWarnings("unchecked") // Must be of type E
						Class<E> klass = (Class<E>) i.getClass();
				speedRecorder.addRecord(klass, r, time);
			} catch (Throwable t) {
				Log.e("Fatal Exception while processing intent: " + i);
				Log.e(t);
			} finally {
				if (remaining.decrementAndGet() <= 0) {
					i.markAsComplete(IntentManager.this);
					Consumer<Intent> completedCallback = i.getCompletedCallback();
					if (completedCallback != null)
						completedCallback.accept(i);
				}
			}
		}
		
		@Override
		public int compareTo(@NotNull PrioritizedRunnable r) {
			if (r instanceof IntentRunner)
				return i.compareTo(((IntentRunner) r).i);
			return -1;
		}
		
	}
	
}
