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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConcurrentBase<T> {
	
	private final Map<Object, ComplexListener<T, ConcurrentBase<T>>> listeners;
	
	private final Object mutex;
	private final boolean prohibitNull;
	
	private T value;
	
	public ConcurrentBase() {
		this(false, null);
	}
	
	public ConcurrentBase(T value) {
		this(false, value);
	}
	
	protected ConcurrentBase(boolean prohibitNull, T value) {
		this.mutex = new Object();
		this.prohibitNull = prohibitNull;
		this.listeners = new HashMap<>();
		
		internalSet(value);
	}
	
	public T get() {
		return internalGet();
	}
	
	public T set(T value) {
		return internalSet(value);
	}
	
	public void addListener(@NotNull Consumer<T> listener) {
		addListener(listener, listener);
	}
	
	public <S> void addTransformListener(@NotNull Function<T, S> transformer, @NotNull Consumer<S> listener) {
		addTransformListener(listener, transformer, listener);
	}
	
	public void addListener(@NotNull Object key, @NotNull Consumer<T> listener) {
		addListener(key, (obs, prev, next) -> listener.accept(next));
	}
	
	public <S> void addTransformListener(@NotNull Object key, @NotNull Function<T, S> transformer, @NotNull Consumer<S> listener) {
		addListener(key, (obs, prev, next) -> listener.accept(transformer.apply(next)));
	}
	
	public void addListener(@NotNull ComplexListener<T, ConcurrentBase<T>> listener) {
		addListener(listener, listener);
	}
	
	public <S> void addTransformListener(@NotNull Function<T, S> transformer, @NotNull ComplexListener<S, ConcurrentBase<T>> listener) {
		addTransformListener(listener, transformer, listener);
	}
	
	public void addListener(@NotNull Object key, @NotNull ComplexListener<T, ConcurrentBase<T>> listener) {
		synchronized (listeners) {
			this.listeners.put(key, listener);
		}
	}
	
	public <S> void addTransformListener(@NotNull Object key, @NotNull Function<T, S> transformer, @NotNull ComplexListener<S, ConcurrentBase<T>> listener) {
		synchronized (listeners) {
			this.listeners.put(key, (obs, prev, next) -> listener.accept(obs, transformer.apply(prev), transformer.apply(next)));
		}
	}
	
	public boolean removeListener(@NotNull Object key) {
		synchronized (listeners) {
			return listeners.remove(key) != null;
		}
	}
	
	public void clearListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	@Override
	@NotNull
	public String toString() {
		T value = this.value;
		return value == null ? "null" : value.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ConcurrentBase))
			return false;
		Object myValue = this.value;
		Object theirValue = ((ConcurrentBase) o).value;
		return myValue == theirValue || (myValue != null && myValue.equals(theirValue));
	}
	
	protected T internalGet() {
		return value;
	}
	
	protected T internalSet(T value) {
		if (prohibitNull)
			Objects.requireNonNull(value, "Value cannot be set to null!");
		synchronized (getMutex()) {
			T prev = this.value;
			this.value = value;
			if (prev != value && (prev == null || !prev.equals(value)))
				callListeners(prev, value);
			return prev;
		}
	}
	
	protected void callListeners(T prev, T next) {
		synchronized (listeners) {
			for (ComplexListener<T, ConcurrentBase<T>> listener : listeners.values()) {
				listener.accept(this, prev, next);
			}
		}
	}
	
	protected Object getMutex() {
		return mutex;
	}
	
	public interface ComplexListener<S, U extends ConcurrentBase<?>> {
		void accept(@NotNull U concurrentObject, S prev, S next);
	}
	
}
