package me.joshlarson.jlcommon.utilities;

import me.joshlarson.jlcommon.log.Log;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

public class ThreadUtilities {
	
	public static ThreadFactory newThreadFactory(@Nonnull String pattern) {
		return new CustomThreadFactory(pattern, Thread.NORM_PRIORITY);
	}
	
	public static ThreadFactory newThreadFactory(@Nonnull String pattern, @Nonnegative int priority) {
		return new CustomThreadFactory(pattern, priority);
	}
	
	public static void safeRun(@Nonnull Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			Log.e(t);
		}
	}
	
	private static class CustomThreadFactory implements ThreadFactory {
		
		private final String pattern;
		private final int priority;
		private int counter;
		
		public CustomThreadFactory(@Nonnull String pattern, @Nonnegative int priority) {
			this.pattern = pattern;
			this.priority = priority;
			this.counter = 0;
		}
		
		@Override
		@Nonnull
		public Thread newThread(@Nonnull Runnable r) {
			String name;
			if (pattern.contains("%d"))
				name = String.format(pattern, counter++);
			else
				name = pattern;
			Thread t = new Thread(r, name);
			t.setPriority(priority);
			return t;
		}
		
	}
	
}
