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
