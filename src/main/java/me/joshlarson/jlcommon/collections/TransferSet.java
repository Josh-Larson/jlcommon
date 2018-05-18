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
package me.joshlarson.jlcommon.collections;

import me.joshlarson.jlcommon.concurrency.beans.ConcurrentMap;
import me.joshlarson.jlcommon.log.Log;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TransferSet<T, S> implements Set<S> {
	
	private final Map<Object, S> transferred;
	private final Collection<S> immutable;
	private final Function<T, Object> keyMapping;
	private final Function<T, S> objectMapping;
	private final Map<Object, Consumer<S>> createCallback;
	private final Map<Object, Consumer<S>> destroyCallback;
	
	/**
	 * Creates a new transfer set with the specified key and object mappings.  The key mapping
	 * serves as the unique identifier for each T object, and the object mapping converts the
	 * T objects into S objects.
	 * @param keyMapping the function to create unique keys for all source items
	 * @param objectMapping the function to convert source items into this set
	 */
	public TransferSet(Function<T, Object> keyMapping, Function<T, S> objectMapping) {
		this.transferred = new ConcurrentHashMap<>();
		this.immutable = Collections.unmodifiableCollection(transferred.values());
		this.keyMapping = keyMapping;
		this.objectMapping = objectMapping;
		this.createCallback = new ConcurrentHashMap<>();
		this.destroyCallback = new ConcurrentHashMap<>();
	}
	
	/**
	 * Synchronizes the source collection 'c' with this transfer set. The connection between 'c'
	 * and this set is formed by the unique keys generated by the keyMapping function.  When an
	 * item from 'c' needs to be transferred into this set, it uses the objectMapping function
	 * @param c the source collection
	 */
	public void synchronize(Collection<T> c) {
		Map<Object, T> m = new HashMap<>();
		// O(|b|)
		for (T t : c) {
			m.put(keyMapping.apply(t), t);
		}
		// O(|a|)
		retainAll(m);
		// O(|b|)
		for (Entry<Object, T> e : m.entrySet()) {
			transferred.computeIfAbsent(e.getKey(), k -> computeIfAbsent(e.getValue()));
		}
	}
	
	public void addCreateCallback(Consumer<S> callback) {
		addCreateCallback(callback, callback);
	}
	
	public void addCreateCallback(Object key, Consumer<S> callback) {
		this.createCallback.put(key, callback);
	}
	
	public void addDestroyCallback(Consumer<S> callback) {
		addCreateCallback(callback, callback);
	}
	
	public void addDestroyCallback(Object key, Consumer<S> callback) {
		this.destroyCallback.put(key, callback);
	}
	
	public void removeCreateCallback(Object key) {
		this.createCallback.remove(key);
	}
	
	public void removeDestroyCallback(Object key) {
		this.destroyCallback.remove(key);
	}
	
	public void clearCreateCallbacks() {
		this.createCallback.clear();
	}
	
	public void clearDestroyCallbacks() {
		this.destroyCallback.clear();
	}
	
	@Override
	public int size() {
		return immutable.size();
	}
	
	@Override
	public boolean isEmpty() {
		return immutable.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return immutable.contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<S> iterator() {
		return immutable.iterator();
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return immutable.toArray();
	}
	
	@NotNull
	@Override
	public <U> U[] toArray(@NotNull U[] a) {
		return immutable.toArray(a);
	}
	
	@Override
	public boolean add(S s) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return immutable.containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends S> c) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public boolean removeIf(Predicate<? super S> filter) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unable to change transfer set without synchronize");
	}
	
	@Override
	public Spliterator<S> spliterator() {
		return immutable.spliterator();
	}
	
	@Override
	public Stream<S> stream() {
		return immutable.stream();
	}
	
	@Override
	public Stream<S> parallelStream() {
		return immutable.parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super S> action) {
		immutable.forEach(action);
	}
	
	private S computeIfAbsent(T t) {
		S val = objectMapping.apply(t);
		for (Consumer<S> call : createCallback.values()) {
			try {
				call.accept(val);
			} catch (Throwable e) {
				Log.e(e);
			}
		}
		return val;
	}
	
	private void retainAll(Map<Object, T> m) {
		for (Iterator<Entry<Object, S>> it = transferred.entrySet().iterator(); it.hasNext(); ) {
			Entry<Object, S> e = it.next();
			if (!m.containsKey(e.getKey())) {
				for (Consumer<S> call : destroyCallback.values()) {
					try {
						call.accept(e.getValue());
					} catch (Throwable t) {
						Log.e(t);
					}
				}
				it.remove();
			}
		}
	}
}
