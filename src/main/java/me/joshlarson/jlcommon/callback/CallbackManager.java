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
package me.joshlarson.jlcommon.callback;

import me.joshlarson.jlcommon.concurrency.ThreadPool;
import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CallbackManager<T> {
	
	private final ThreadPool executor;
	private final List<T> callbacks;
	private final AtomicInteger runningTasks;
	
	public CallbackManager(@NotNull String name) {
		this(name, 1);
	}
	
	public CallbackManager(@NotNull String name, int threadCount) {
		this.executor = new ThreadPool(threadCount, name);
		this.callbacks = new CopyOnWriteArrayList<>();
		this.runningTasks = new AtomicInteger(0);
	}
	
	public void addCallback(@NotNull T callback) {
		callbacks.add(callback);
	}
	
	public void removeCallback(@NotNull T callback) {
		callbacks.remove(callback);
	}
	
	public void setCallback(@NotNull T callback) {
		callbacks.clear();
		callbacks.add(callback);
	}
	
	public void clearCallbacks() {
		callbacks.clear();
	}
	
	public void start() {
		executor.start();
	}
	
	public void stop() {
		executor.stop(false);
	}
	
	public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) {
		return executor.awaitTermination(unit.toMillis(timeout));
	}
	
	public boolean isRunning() {
		return executor.isRunning();
	}
	
	public boolean isQueueEmpty() {
		return runningTasks.get() == 0;
	}
	
	public void callOnEach(@NotNull CallCallback<T> call) {
		runningTasks.incrementAndGet();
		executor.execute(() -> {
			for (T callback : callbacks) {
				try {
					call.run(callback);
				} catch (Throwable t) {
					Log.e(t);
				}
			}
			runningTasks.decrementAndGet();
		});
	}
	
	public interface CallCallback<T> {
		
		void run(@NotNull T callback);
	}
}
