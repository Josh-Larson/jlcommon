package me.joshlarson.jlcommon.concurrency;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Queue;

public class TaskThreadPool<T> extends ThreadPool {
	
	private final Queue<T> tasks;
	private final Runnable runner;
	
	public TaskThreadPool(@Nonnegative int nThreads, @Nonnull String namePattern, @Nonnull TaskExecutor<T> executor) {
		super(nThreads, namePattern);
		this.tasks = new ArrayDeque<>();
		this.runner = () -> {
			T t;
			synchronized (tasks) {
				t = tasks.poll();
			}
			if (t != null)
				executor.run(t);
		};
	}
	
	@Override
	public void execute(@Nonnull Runnable runnable) {
		throw new UnsupportedOperationException("Runnable are posted automatically by addTask!");
	}
	
	public void addTask(@Nonnull T t) {
		synchronized (tasks) {
			tasks.add(t);
		}
		super.execute(runner);
	}
	
	@Nonnegative
	public int getTaskCount() {
		return tasks.size();
	}
	
	public interface TaskExecutor<T> {
		
		void run(@Nonnull T t);
	}
	
}
