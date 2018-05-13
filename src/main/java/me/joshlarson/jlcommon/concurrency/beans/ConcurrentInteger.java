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

import java.util.function.IntUnaryOperator;

public class ConcurrentInteger extends ConcurrentBase<Integer> {
	
	public ConcurrentInteger() {
		super(true, 0);
	}
	
	public ConcurrentInteger(@NotNull Integer value) {
		super(true, value);
	}
	
	public ConcurrentInteger(int value) {
		super(true, value);
	}
	
	@Override
	@NotNull
	public Integer get() {
		return super.get();
	}
	
	@Override
	@NotNull
	public Integer set(@NotNull Integer value) {
		return super.set(value);
	}
	
	public int getValue() {
		return get();
	}
	
	public int setValue(int value) {
		return set(value);
	}
	
	public int incrementAndGet() {
		return addAndGet(1);
	}
	
	public int decrementAndGet() {
		return addAndGet(-1);
	}
	
	public int updateAndGet(@NotNull IntUnaryOperator op) {
		synchronized (getMutex()) {
			int newValue = op.applyAsInt(internalGet());
			internalSet(newValue);
			return newValue;
		}
	}
	
	public boolean compareAndSet(int expected, int newValue) {
		synchronized (getMutex()) {
			if (internalGet() != expected)
				return false;
			int prev = internalSet(newValue);
			assert prev == expected : "Concurrent modification";
			return true;
		}
	}
	
	public int getAndIncrement() {
		synchronized (getMutex()) {
			return internalSet(internalGet() + 1);
		}
	}
	
	public int getAndDecrement() {
		synchronized (getMutex()) {
			return internalSet(internalGet() - 1);
		}
	}
	
	public int getAndUpdate(@NotNull IntUnaryOperator op) {
		synchronized (getMutex()) {
			return internalSet(op.applyAsInt(internalGet()));
		}
	}
	
	public int addAndGet(int delta) {
		synchronized (getMutex()) {
			int newValue = internalGet() + delta;
			internalSet(newValue);
			return newValue;
		}
	}
	
	public int getAndAdd(int delta) {
		synchronized (getMutex()) {
			return internalSet(internalGet() + delta);
		}
	}
	
}
