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

import java.util.function.DoubleUnaryOperator;

public class ConcurrentDouble extends ConcurrentBase<Double> {
	
	public ConcurrentDouble() {
		super(true, 0d);
	}
	
	public ConcurrentDouble(@NotNull Double value) {
		super(true, value);
	}
	
	public ConcurrentDouble(double value) {
		super(true, value);
	}
	
	@Override
	@NotNull
	public Double get() {
		return super.get();
	}
	
	@Override
	@NotNull
	public Double set(@NotNull Double value) {
		return super.set(value);
	}
	
	public double getValue() {
		return get();
	}
	
	public double setValue(double value) {
		return super.set(value);
	}
	
	public double updateAndGet(@NotNull DoubleUnaryOperator op) {
		synchronized (getMutex()) {
			double newValue = op.applyAsDouble(internalGet());
			internalSet(newValue);
			return newValue;
		}
	}
	
	public double getAndUpdate(@NotNull DoubleUnaryOperator op) {
		synchronized (getMutex()) {
			return internalSet(op.applyAsDouble(internalGet()));
		}
	}
	
}
