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

import me.joshlarson.jlcommon.concurrency.ThreadPool.PrioritizedRunnable;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JUnit4.class)
public class TestThreadPool {
	
	@Test
	public void testStartThread() {
		AtomicBoolean started = new AtomicBoolean(false);
		ThreadPool thread = new ThreadPool(1, "thread");
		thread.start();
		thread.execute(() -> {
			started.set(true);
			thread.stop(false);
		});
		thread.awaitTermination(100);
		Assert.assertTrue(started.get());
	}
	
	@Test(expected=AssertionError.class)
	public void testExecuteNotRunning() {
		ThreadPool thread = new ThreadPool(1, "thread");
		thread.start();
		thread.stop(true);
		thread.awaitTermination(100);
		thread.execute(() -> {});
	}
	
	@Test(expected=AssertionError.class)
	public void testExecuteNotStarted() {
		ThreadPool thread = new ThreadPool(1, "thread");
		thread.execute(() -> {});
	}
	
	@Test
	public void testThreadPriority() {
		AtomicBoolean valid = new AtomicBoolean(false);
		ThreadPool thread = new ThreadPool(1, "thread");
		thread.setPriority(1);
		thread.start();
		thread.execute(() -> valid.set(Thread.currentThread().getPriority() == 1));
		thread.stop(false);
		thread.awaitTermination(100);
		Assert.assertTrue(valid.get());
	}
	
	@Test
	public void testPrioritizationEnabled() {
		ThreadPool thread = new ThreadPool(true, 1, "thread");
		thread.start();
		for (int i = 0; i < 1000; i++)
			thread.execute(new TestRunnable());
		thread.stop(false);
		thread.awaitTermination(100);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPrioritizationInvalidRunnable() {
		ThreadPool thread = new ThreadPool(true, 1, "thread");
		thread.start();
		thread.execute(() -> {});
	}
	
	private static class TestRunnable implements PrioritizedRunnable {
		
		@Override
		public int compareTo(@NotNull PrioritizedRunnable o) {
			return 0;
		}
		
		@Override
		public void run() {
			
		}
	}
	
}
