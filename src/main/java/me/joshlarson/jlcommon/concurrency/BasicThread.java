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

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicThread {
	
	private final ThreadPool threadPool;
	private final AtomicBoolean executing;
	private final Runnable runnable;
	
	public BasicThread(@NotNull String name, @NotNull Runnable runnable) {
		this.threadPool = new ThreadPool(1, name);
		this.executing = new AtomicBoolean(false);
		this.runnable = runnable;
	}
	
	public void start() {
		threadPool.start();
		threadPool.execute(() -> {
			executing.set(true);
			try {
				runnable.run();
			} finally {
				executing.set(false);
			}
		});
	}
	
	public void setPriority(int priority) {
		threadPool.setPriority(priority);
	}
	
	public void stop(boolean interrupt) {
		threadPool.stop(interrupt);
	}
	
	public boolean awaitTermination(long timeout) {
		return threadPool.awaitTermination(timeout);
	}
	
	public boolean isExecuting() {
		return executing.get();
	}
	
}
