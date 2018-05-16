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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConcurrentMap<K, V> extends ConcurrentBase<Map<K, V>> implements Map<K, V> {
	
	private final Map<Object, ComplexMapChangedListener<ConcurrentMap<K, V>>> listeners;
	
	public ConcurrentMap() {
		this(new HashMap<>());
	}
	
	public ConcurrentMap(Map<K, V> value) {
		super(true, value);
		this.listeners = new HashMap<>();
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
	public boolean containsKey(Object key) {
		return internalGet().containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return internalGet().containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return internalGet().get(key);
	}
	
	@Nullable
	@Override
	public V put(K key, V value) {
		synchronized (getMutex()) {
			V ret = internalGet().put(key, value);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public V remove(Object key) {
		synchronized (getMutex()) {
			V ret = internalGet().remove(key);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		synchronized (getMutex()) {
			internalGet().putAll(m);
			callMapChangedListeners();
		}
	}
	
	@Override
	public void clear() {
		synchronized (getMutex()) {
			internalGet().clear();
			callMapChangedListeners();
		}
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return internalGet().keySet();
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return internalGet().values();
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return internalGet().entrySet();
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return internalGet().getOrDefault(key, defaultValue);
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		internalGet().forEach(action);
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		synchronized (getMutex()) {
			internalGet().replaceAll(function);
			callMapChangedListeners();
		}
	}
	
	@Nullable
	@Override
	public V putIfAbsent(K key, V value) {
		synchronized (getMutex()) {
			V ret = internalGet().putIfAbsent(key, value);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		synchronized (getMutex()) {
			boolean ret = internalGet().remove(key, value);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		synchronized (getMutex()) {
			boolean ret = internalGet().replace(key, oldValue, newValue);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Nullable
	@Override
	public V replace(K key, V value) {
		synchronized (getMutex()) {
			V ret = internalGet().replace(key, value);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		synchronized (getMutex()) {
			V ret = internalGet().computeIfAbsent(key, mappingFunction);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		synchronized (getMutex()) {
			V ret = internalGet().computeIfPresent(key, remappingFunction);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		synchronized (getMutex()) {
			V ret = internalGet().compute(key, remappingFunction);
			callMapChangedListeners();
			return ret;
		}
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		synchronized (getMutex()) {
			V ret = internalGet().merge(key, value, remappingFunction);
			callMapChangedListeners();
			return ret;
		}
	}
	
	public void addMapChangedListener(@NotNull Runnable listener) {
		addMapChangedListener(listener, listener);
	}
	
	public void addMapChangedListener(@NotNull Object key, @NotNull Runnable listener) {
		addMapChangedListener(key, obs -> listener.run());
	}
	
	public void addMapChangedListener(@NotNull ConcurrentMap.ComplexMapChangedListener<ConcurrentMap<K, V>> listener) {
		addMapChangedListener(listener, listener);
	}
	
	public void addMapChangedListener(@NotNull Object key, @NotNull ConcurrentMap.ComplexMapChangedListener<ConcurrentMap<K, V>> listener) {
		synchronized (listeners) {
			this.listeners.put(key, listener);
		}
	}
	
	public boolean removeMapChangedListener(@NotNull Object key) {
		synchronized (listeners) {
			return listeners.remove(key) != null;
		}
	}
	
	public void clearMapChangedListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	protected void callMapChangedListeners() {
		synchronized (listeners) {
			for (ComplexMapChangedListener<ConcurrentMap<K, V>> listener : listeners.values()) {
				listener.accept(this);
			}
		}
	}
	
	public interface ComplexMapChangedListener<S> extends Consumer<S> {
		
	}
	
}
