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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConcurrentCollection<S extends Collection<T>, T> extends ConcurrentBase<S> implements Collection<T> {
	
	private final Map<Object, ComplexCollectionChangedListener<ConcurrentCollection<S, T>>> listeners;
	
	public ConcurrentCollection(S value) {
		super(true, value);
		this.listeners = new HashMap<>();
	}
	
	@Override
	protected S internalSet(S value) {
		throw new UnsupportedOperationException("Cannot set a new collection!");
	}
	
	@Override
	public int size() {
		return internalGet().size();
	}
	
	@Override
	public boolean isEmpty() {
		return internalGet().isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return internalGet().contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<T> iterator() {
		return internalGet().iterator();
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return internalGet().toArray();
	}
	
	@NotNull
	@Override
	public <T1> T1[] toArray(@NotNull T1[] a) {
		return internalGet().toArray(a);
	}
	
	@Override
	public boolean add(T t) {
		synchronized (getMutex()) {
			boolean ret = internalGet().add(t);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean remove(Object o) {
		synchronized (getMutex()) {
			boolean ret = internalGet().remove(o);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return internalGet().containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends T> c) {
		synchronized (getMutex()) {
			boolean ret = internalGet().addAll(c);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		synchronized (getMutex()) {
			boolean ret = internalGet().removeAll(c);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean removeIf(Predicate<? super T> filter) {
		synchronized (getMutex()) {
			boolean ret = internalGet().removeIf(filter);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		synchronized (getMutex()) {
			boolean ret = internalGet().retainAll(c);
			if (ret)
				callCollectionChangedListeners();
			return ret;
		}
	}
	
	@Override
	public void clear() {
		synchronized (getMutex()) {
			internalGet().clear();
			callCollectionChangedListeners();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof ConcurrentCollection && internalGet().equals(((ConcurrentCollection) o).get());
	}
	
	@Override
	public int hashCode() {
		return internalGet().hashCode();
	}
	
	@Override
	public Spliterator<T> spliterator() {
		return internalGet().spliterator();
	}
	
	@Override
	public Stream<T> stream() {
		return internalGet().stream();
	}
	
	@Override
	public Stream<T> parallelStream() {
		return internalGet().parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super T> action) {
		internalGet().forEach(action);
	}
	
	public void addCollectionChangedListener(@NotNull Runnable listener) {
		addCollectionChangedListener(listener, listener);
	}
	
	public void addCollectionChangedListener(@NotNull Object key, @NotNull Runnable listener) {
		addCollectionChangedListener(key, obs -> listener.run());
	}
	
	public void addCollectionChangedListener(@NotNull ComplexCollectionChangedListener<ConcurrentCollection<S, T>> listener) {
		addCollectionChangedListener(listener, listener);
	}
	
	public void addCollectionChangedListener(@NotNull Object key, @NotNull ComplexCollectionChangedListener<ConcurrentCollection<S, T>> listener) {
		synchronized (listeners) {
			this.listeners.put(key, listener);
		}
	}
	
	public boolean removeCollectionChangedListener(@NotNull Object key) {
		synchronized (listeners) {
			return listeners.remove(key) != null;
		}
	}
	
	public void clearCollectionChangedListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	protected void callCollectionChangedListeners() {
		synchronized (listeners) {
			for (ComplexCollectionChangedListener<ConcurrentCollection<S, T>> listener : listeners.values()) {
				listener.accept(this);
			}
		}
	}
	
	public interface ComplexCollectionChangedListener<S> extends Consumer<S> {
		
	}
	
}
