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
import java.util.stream.IntStream;

public class ConcurrentString extends ConcurrentBase<String> implements CharSequence {
	
	public ConcurrentString() {
		super();
	}
	
	public ConcurrentString(String value) {
		super(value);
	}
	
	@Override
	public int length() {
		return internalGet().length();
	}
	
	@Override
	public char charAt(int index) {
		return internalGet().charAt(index);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		return internalGet().subSequence(start, end);
	}
	
	@Override
	public IntStream chars() {
		return internalGet().chars();
	}
	
	@Override
	public IntStream codePoints() {
		return internalGet().codePoints();
	}
	
	@Override
	@NotNull
	public String toString() {
		return internalGet();
	}
	
	public String updateAndGet(@NotNull UnaryOperator<String> op) {
		synchronized (getMutex()) {
			String newValue = op.apply(internalGet());
			internalSet(newValue);
			return newValue;
		}
	}
	
	public String getAndUpdate(@NotNull UnaryOperator<String> op) {
		synchronized (getMutex()) {
			return internalSet(op.apply(internalGet()));
		}
	}
	
}
