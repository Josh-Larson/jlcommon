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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class ConcurrentList<T> extends ConcurrentCollection<List<T>, T> implements List<T> {
	
	public ConcurrentList() {
		super(new ArrayList<>());
	}
	
	public ConcurrentList(List<T> value) {
		super(value);
	}
	
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends T> c) {
		boolean ret = internalGet().addAll(index, c);
		callCollectionChangedListeners();
		return false;
	}
	
	@Override
	public T get(int index) {
		return internalGet().get(index);
	}
	
	@Override
	public T set(int index, T element) {
		synchronized (getMutex()) {
			T ret = internalGet().set(index, element);
			callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public void add(int index, T element) {
		synchronized (getMutex()) {
			internalGet().add(index, element);
			callCollectionChangedListeners();
		}
	}
	
	@Override
	public T remove(int index) {
		synchronized (getMutex()) {
			T ret = internalGet().remove(index);
			callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public int indexOf(Object o) {
		return internalGet().indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return internalGet().lastIndexOf(o);
	}
	
	@NotNull
	@Override
	public ListIterator<T> listIterator() {
		return internalGet().listIterator();
	}
	
	@NotNull
	@Override
	public ListIterator<T> listIterator(int index) {
		return internalGet().listIterator(index);
	}
	
	@NotNull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return internalGet().subList(fromIndex, toIndex);
	}
	
}
