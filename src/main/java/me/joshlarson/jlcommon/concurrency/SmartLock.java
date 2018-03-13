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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SmartLock {
	
	private final Lock lock;
	private final Condition condition;
	
	public SmartLock() {
		this.lock = new ReentrantLock(true);
		this.condition = lock.newCondition();
	}
	
	public void lock() {
		lock.lock();
	}
	
	public void lockInterruptibly() throws InterruptedException {
		lock.lockInterruptibly();
	}
	
	public boolean tryLock() {
		return lock.tryLock();
	}
	
	public boolean tryLock(@Nonnegative long time, @Nonnull TimeUnit unit) throws InterruptedException {
		return lock.tryLock(time, unit);
	}
	
	public void unlock() {
		lock.unlock();
	}
	
	public void await() throws InterruptedException {
		lock();
		try {
			condition.await();
		} finally {
			unlock();
		}
	}
	
	public void awaitUninterruptibly() {
		lock();
		try {
			condition.awaitUninterruptibly();
		} finally {
			unlock();
		}
	}
	
	public long awaitNanos(@Nonnegative long nanosTimeout) throws InterruptedException {
		lock();
		try {
			return condition.awaitNanos(nanosTimeout);
		} finally {
			unlock();
		}
	}
	
	public boolean await(@Nonnegative long time, @Nonnull TimeUnit unit) throws InterruptedException {
		lock();
		try {
			return condition.await(time, unit);
		} finally {
			unlock();
		}
	}
	
	public boolean awaitUntil(@Nonnull Date deadline) throws InterruptedException {
		lock();
		try {
			return condition.awaitUntil(deadline);
		} finally {
			unlock();
		}
	}
	
	public void signal() {
		lock();
		try {
			condition.signal();
		} finally {
			unlock();
		}
	}
	
	public void signalAll() {
		lock();
		try {
			condition.signalAll();
		} finally {
			unlock();
		}
	}
	
}
