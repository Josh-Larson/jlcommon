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
package me.joshlarson.jlcommon.data;

import java.util.HashMap;
import java.util.Map;

public class EnumLookup<K, T extends Enum<?>> {
	
	private final Map<K, T> lookup;
	
	public EnumLookup(Class<T> c, CustomLookupAdder<K, T> adder) {
		lookup = new HashMap<>();
		for (T t : c.getEnumConstants()) {
			lookup.put(adder.getKey(t), t);
		}
	}
	
	public T getEnum(K k, T def) {
		T t = lookup.get(k);
		if (t == null)
			return def;
		return t;
	}
	
	public boolean containsEnum(K k) {
		return lookup.containsKey(k);
	}
	
	public int size() {
		return lookup.size();
	}
	
	public interface CustomLookupAdder<K, T> {
		K getKey(T t);
	}
	
}
