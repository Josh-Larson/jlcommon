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
package me.joshlarson.jlcommon.concurrency.beans;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongUnaryOperator;

public class ConcurrentLong extends ConcurrentBase<Long> {
	
	public ConcurrentLong() {
		super(true, 0L);
	}
	
	public ConcurrentLong(@NotNull Long value) {
		super(true, value);
	}
	
	public ConcurrentLong(long value) {
		super(true, value);
	}
	
	@Override
	@NotNull
	public Long get() {
		return super.get();
	}
	
	@Override
	@NotNull
	public Long set(@NotNull Long value) {
		return super.set(value);
	}
	
	public long getValue() {
		return get();
	}
	
	public long setValue(long value) {
		return set(value);
	}
	
	public long incrementAndGet() {
		return addAndGet(1);
	}
	
	public long decrementAndGet() {
		return addAndGet(-1);
	}
	
	public long updateAndGet(@NotNull LongUnaryOperator op) {
		synchronized (getMutex()) {
			long newValue = op.applyAsLong(internalGet());
			internalSet(newValue);
			return newValue;
		}
	}
	
	public boolean compareAndSet(long expected, long newValue) {
		synchronized (getMutex()) {
			if (internalGet() != expected)
				return false;
			long prev = internalSet(newValue);
			assert prev == expected : "Concurrent modification";
		}
		return true;
	}
	
	public long getAndIncrement() {
		synchronized (getMutex()) {
			return internalSet(internalGet() + 1);
		}
	}
	
	public long getAndDecrement() {
		synchronized (getMutex()) {
			return internalSet(internalGet() - 1);
		}
	}
	
	public long getAndUpdate(@NotNull LongUnaryOperator op) {
		synchronized (getMutex()) {
			return internalSet(op.applyAsLong(internalGet()));
		}
	}
	
	public long addAndGet(long delta) {
		synchronized (getMutex()) {
			long newValue = internalGet() + delta;
			internalSet(newValue);
			return newValue;
		}
	}
	
	public long getAndAdd(long delta) {
		synchronized (getMutex()) {
			return internalSet(internalGet() + delta);
		}
	}
	
}
