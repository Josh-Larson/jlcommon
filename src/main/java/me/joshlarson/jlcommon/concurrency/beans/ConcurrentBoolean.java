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

import java.util.function.UnaryOperator;

public class ConcurrentBoolean extends ConcurrentBase<Boolean> {
	
	public ConcurrentBoolean() {
		super(true, false);
	}
	
	public ConcurrentBoolean(@NotNull Boolean value) {
		super(true, value);
	}
	
	public ConcurrentBoolean(boolean value) {
		super(true, value);
	}
	
	@Override
	@NotNull
	public Boolean get() {
		return super.get();
	}
	
	@Override
	@NotNull
	public Boolean set(@NotNull Boolean value) {
		return super.set(value);
	}
	
	public boolean getValue() {
		return get();
	}
	
	public boolean setValue(boolean value) {
		return super.set(value);
	}
	
	public boolean updateAndGet(@NotNull UnaryOperator<Boolean> op) {
		synchronized (getMutex()) {
			Boolean newValue = op.apply(internalGet());
			internalSet(newValue);
			return newValue;
		}
	}
	
	public boolean getAndUpdate(@NotNull UnaryOperator<Boolean> op) {
		synchronized (getMutex()) {
			return internalSet(op.apply(internalGet()));
		}
	}
	
}
