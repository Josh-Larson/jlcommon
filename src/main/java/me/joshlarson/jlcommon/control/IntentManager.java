package me.joshlarson.jlcommon.control;

import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.log.Log;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class IntentManager {
	
	private static final AtomicReference<IntentManager> INSTANCE = new AtomicReference<>(null);
	
	private final Map<Class<? extends Intent>, List<Consumer<? extends Intent>>> intentRegistrations;
	private final IntentSpeedRecorder speedRecorder;
	private final ThreadPool processThreads;
	private final AtomicBoolean initialized;
	
	public IntentManager(@Nonnegative int threadCount) {
		this.intentRegistrations = new ConcurrentHashMap<>();
		this.speedRecorder = new IntentSpeedRecorder();
		this.processThreads = new ThreadPool(true, threadCount, "intent-processor-%d");
		this.initialized = new AtomicBoolean(false);
		
		this.processThreads.setPriority(8);
	}
	
	public void initialize() {
		if (initialized.getAndSet(true))
			return;
		processThreads.start();
	}
	
	public void terminate() {
		if (!initialized.getAndSet(false))
			return;
		processThreads.stop(true);
	}
	
	public void setPriority(@Nonnegative int priority) {
		processThreads.setPriority(priority);
	}
	
	@Nonnegative
	public int getIntentCount() {
		return processThreads.getQueuedTasks();
	}
	
	@Nonnull
	public IntentSpeedRecorder getSpeedRecorder() {
		return speedRecorder;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Intent> void broadcastIntent(@Nonnull E i) {
		List<Consumer<? extends Intent>> receivers = intentRegistrations.get(i.getClass());
		if (receivers == null || !processThreads.isRunning())
			return;
		
		AtomicInteger remaining = new AtomicInteger(receivers.size());
		receivers.forEach(r -> processThreads.execute(new IntentRunner<>((Consumer<E>) r, i, remaining)));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Intent> void registerForIntent(@Nonnull Class<T> c, @Nonnull Consumer<T> r) {
		List<Consumer<? extends Intent>> intents = intentRegistrations.computeIfAbsent(c, s -> new CopyOnWriteArrayList<>());
		intents.add(r);
	}
	
	public <T extends Intent> void unregisterForIntent(@Nonnull Class<T> c, @Nonnull Consumer<T> r) {
		List<Consumer<? extends Intent>> intents = intentRegistrations.get(c);
		if (intents == null)
			return;
		intents.remove(r);
	}
	
	@CheckForNull
	public static IntentManager getInstance() {
		return INSTANCE.get();
	}
	
	public static void setInstance(@Nonnull IntentManager intentManager) {
		IntentManager prev = INSTANCE.getAndSet(intentManager);
		if (prev != null)
			prev.terminate();
	}
	
	public static class IntentSpeedRecorder {
		
		private final Map<Consumer<? extends Intent>, IntentSpeedRecord> times;
		
		public IntentSpeedRecorder() {
			this.times = new ConcurrentHashMap<>();
		}
		
		private <E extends Intent> void addRecord(@Nonnull Class<E> intent, @Nonnull Consumer<E> consumer, @Nonnegative long timeNanos) {
			IntentSpeedRecord record = times.computeIfAbsent(consumer, s -> new IntentSpeedRecord(intent, consumer));
			record.addTime(timeNanos);
		}
		
		@CheckForNull
		public IntentSpeedRecord getTime(@Nonnull Consumer<? extends Intent> consumer) {
			return times.get(consumer);
		}
		
		@Nonnull
		public List<IntentSpeedRecord> getAllTimes() {
			return new ArrayList<>(times.values());
		}
		
	}
	
	public static class IntentSpeedRecord implements Comparable<IntentSpeedRecord> {
		
		private final Class<? extends Intent> intent;
		private final Consumer<? extends Intent> consumer;
		private final AtomicLong time;
		private final AtomicLong count;
		
		public IntentSpeedRecord(@Nonnull Class<? extends Intent> intent, @Nonnull Consumer<? extends Intent> consumer) {
			this.intent = intent;
			this.consumer = consumer;
			this.time = new AtomicLong(0);
			this.count = new AtomicLong(0);
		}
		
		@Nonnull
		public Class<? extends Intent> getIntent() {
			return intent;
		}
		
		@Nonnull
		public Consumer<? extends Intent> getConsumer() {
			return consumer;
		}
		
		@Nonnegative
		public long getTime() {
			return time.get();
		}
		
		@Nonnegative
		public long getCount() {
			return count.get();
		}
		
		public void addTime(@Nonnegative long timeNanos) {
			time.addAndGet(timeNanos);
			count.incrementAndGet();
		}
		
		@Override
		public int compareTo(@Nonnull IntentSpeedRecord record) {
			return Long.compare(record.getTime(), getTime());
		}
		
	}
	
	private class IntentRunner<E extends Intent> implements Comparable<IntentRunner>, Runnable {
		
		private final Consumer<E> r;
		private final E i;
		private final AtomicInteger remaining;
		
		public IntentRunner(@Nonnull Consumer<E> r, @Nonnull E i, @Nonnull AtomicInteger remaining) {
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
		public int compareTo(@Nonnull IntentRunner r) {
			return i.compareTo(r.i);
		}
		
	}
	
}
