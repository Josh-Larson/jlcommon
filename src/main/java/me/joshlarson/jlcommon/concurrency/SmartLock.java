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
