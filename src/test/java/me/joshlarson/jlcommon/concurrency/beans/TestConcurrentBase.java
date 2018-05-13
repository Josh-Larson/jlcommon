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

import org.junit.Assert;
import org.junit.Test;

public class TestConcurrentBase {
	
	@Test
	public void testListeners() {
		ConcurrentInteger ci = new ConcurrentInteger(0);
		ConcurrentBoolean cb = new ConcurrentBoolean(false);
		ci.addListener(i -> cb.set(true));
		Assert.assertFalse(cb.getValue());
		Assert.assertEquals(0, ci.getValue());
		ci.set(1);
		Assert.assertTrue(cb.getValue());
		Assert.assertEquals(1, (int) ci.get());
	}
	
	@Test
	public void testTransformListeners() {
		ConcurrentInteger ci = new ConcurrentInteger(0);
		ConcurrentBoolean cb = new ConcurrentBoolean(false);
		ci.addTransformListener(i -> true, cb::set);
		Assert.assertFalse(cb.getValue());
		Assert.assertEquals(0, ci.getValue());
		ci.setValue(1);
		Assert.assertTrue(cb.getValue());
		Assert.assertEquals(1, (int) ci.get());
	}
	
	@Test
	public void testUpdates() {
		ConcurrentLong cl = new ConcurrentLong();
		Assert.assertEquals(0, cl.getValue());
		Assert.assertEquals(1, cl.updateAndGet(u -> u+1));
		
		ConcurrentInteger ci = new ConcurrentInteger();
		Assert.assertEquals(0, ci.getValue());
		Assert.assertEquals(0, ci.getAndUpdate(u -> u+1));
	}
	
	@Test
	public void testNoRedundantUpdates() {
		ConcurrentInteger cint = new ConcurrentInteger(0);
		ConcurrentLong clong = new ConcurrentLong(0);
		ConcurrentDouble cdouble = new ConcurrentDouble(0);
		ConcurrentString cstr = new ConcurrentString(null);
		ConcurrentBoolean cbool = new ConcurrentBoolean(false);
		
		cint.addListener(c -> cbool.set(true));
		clong.addListener(c -> cbool.set(true));
		cdouble.addListener(c -> cbool.set(true));
		cstr.addListener(c -> cbool.set(true));
		
		cint.setValue(0);
		clong.setValue(0);
		cdouble.setValue(0);
		cstr.set(null);
		
		Assert.assertFalse(cbool.get());
		cstr.set("");
		Assert.assertTrue(cbool.get());
		
		cbool.set(false);
		cstr.set(null);
		Assert.assertTrue(cbool.get());
	}
	
	@Test
	public void testNamedListeners() {
		ConcurrentBoolean cb = new ConcurrentBoolean(false);
		ConcurrentString str = new ConcurrentString("");
		
		str.addListener("list", s -> cb.set(true));
		str.set("test");
		Assert.assertTrue(cb.getValue());
		
		cb.set(false);
		str.removeListener("list");
		str.set("test2");
		Assert.assertFalse(cb.getValue());
		
		str.addTransformListener("list", s -> true, cb::set);
		str.set("test");
		Assert.assertTrue(cb.getValue());
		
		cb.set(false);
		str.clearListeners();
		str.set("test2");
		Assert.assertFalse(cb.getValue());
	}
	
	@Test
	public void testComplexListeners() {
		ConcurrentString str = new ConcurrentString();
		ConcurrentBoolean cb = new ConcurrentBoolean(false);
		
		str.addListener((obs, prev, next) -> cb.set(true));
		str.set("text1");
		Assert.assertTrue(cb.getValue());
		
		cb.set(false);
		str.clearListeners();
		str.addListener("list", (obs, prev, next) -> cb.set(true));
		str.set("text2");
		Assert.assertTrue(cb.getValue());
		
		cb.set(false);
		str.addTransformListener("list", s -> true, (obs, prev, next) -> cb.set(true));
		str.set("text3");
		Assert.assertTrue(cb.getValue());
		
		cb.set(false);
		str.clearListeners();
		str.addTransformListener(s -> true, (obs, prev, next) -> cb.set(true));
		str.set("text4");
		Assert.assertTrue(cb.getValue());
	}
	
	@Test
	public void testObjectMethods() {
		ConcurrentString str1 = new ConcurrentString("test");
		ConcurrentString str2 = new ConcurrentString("test");
		
		Assert.assertEquals(str1, str2);
		Assert.assertEquals("test", str1.toString());
	}
	
	@Test
	public void testReference() {
		ConcurrentReference<String> ref = new ConcurrentReference<>(null);
		
		Assert.assertNull(ref.get());
		ref.set("");
		Assert.assertEquals("", ref.get());
	}
	
	@Test
	public void testString() {
		ConcurrentString str = new ConcurrentString("");
		
		Assert.assertEquals("", str.get());
		Assert.assertEquals("hello ", str.updateAndGet(s -> s + "hello "));
		Assert.assertEquals("hello ", str.getAndUpdate(s -> s + "world!"));
		Assert.assertEquals("hello world!", str.get());
		Assert.assertEquals("hello world!", str.toString());
	}
	
	@Test
	public void testBoolean() {
		ConcurrentBoolean bool = new ConcurrentBoolean((Boolean) false);
		Assert.assertFalse(bool.get());
		bool = new ConcurrentBoolean();
		Assert.assertFalse(bool.get());
		
		Assert.assertTrue(bool.updateAndGet(b -> !b));
		Assert.assertTrue(bool.getAndUpdate(b -> !b));
		Assert.assertFalse(bool.get());
		
		bool.setValue(true);
		Assert.assertTrue(bool.get());
	}
	
	@Test
	public void testDouble() {
		ConcurrentDouble d = new ConcurrentDouble(0);
		Assert.assertEquals(0, d.get(), 1E-7);
		
		d = new ConcurrentDouble((Double) 0d);
		Assert.assertEquals(0, d.get(), 1E-7);
		
		d = new ConcurrentDouble();
		Assert.assertEquals(0, d.get(), 1E-7);
		
		Assert.assertEquals(1, d.updateAndGet(b -> b+1), 1E-7);
		Assert.assertEquals(1, d.getAndUpdate(b -> b+1), 1E-7);
		Assert.assertEquals(2, d.get(), 1E-7);
		
		d.setValue(1.5);
		Assert.assertEquals(1.5, d.get(), 1E-7);
		d.set(2.5);
		Assert.assertEquals(2.5, d.getValue(), 1E-7);
	}
	
	@Test
	public void testInteger() {
		ConcurrentInteger i = new ConcurrentInteger(2);
		Assert.assertEquals(2, i.getValue());
		
		i = new ConcurrentInteger((Integer) 1);
		Assert.assertEquals(1, i.getValue());
		
		i = new ConcurrentInteger();
		Assert.assertEquals(0, (int) i.get());
		
		Assert.assertEquals(1, i.updateAndGet(b -> b+1));
		Assert.assertEquals(1, i.getAndUpdate(b -> b+1));
		Assert.assertEquals(2, i.getValue());
		
		i.setValue(1);
		Assert.assertEquals(1, i.getValue());
		i.set(3);
		Assert.assertEquals(3, i.getValue());
		
		Assert.assertEquals(4, i.incrementAndGet());
		Assert.assertEquals(4, i.getAndIncrement());
		Assert.assertEquals(4, i.decrementAndGet());
		Assert.assertEquals(4, i.getAndDecrement());
		Assert.assertEquals(10, i.addAndGet(7));
		Assert.assertEquals(10, i.getAndAdd(10));
		Assert.assertTrue(i.compareAndSet(20, 0));
		Assert.assertFalse(i.compareAndSet(20, 1));
		Assert.assertEquals(0, i.getValue());
	}
	
	@Test
	public void testLong() {
		ConcurrentLong i = new ConcurrentLong(2);
		Assert.assertEquals(2, i.getValue());
		
		i = new ConcurrentLong((Long) 1L);
		Assert.assertEquals(1, i.getValue());
		
		i = new ConcurrentLong();
		Assert.assertEquals(0, (long) i.get());
		
		Assert.assertEquals(1, i.updateAndGet(b -> b+1));
		Assert.assertEquals(1, i.getAndUpdate(b -> b+1));
		Assert.assertEquals(2, i.getValue());
		
		i.setValue(1);
		Assert.assertEquals(1, i.getValue());
		i.set(3L);
		Assert.assertEquals(3, i.getValue());
		
		Assert.assertEquals(4, i.incrementAndGet());
		Assert.assertEquals(4, i.getAndIncrement());
		Assert.assertEquals(4, i.decrementAndGet());
		Assert.assertEquals(4, i.getAndDecrement());
		Assert.assertEquals(10, i.addAndGet(7));
		Assert.assertEquals(10, i.getAndAdd(10));
		Assert.assertTrue(i.compareAndSet(20, 0));
		Assert.assertFalse(i.compareAndSet(20, 1));
		Assert.assertEquals(0, i.getValue());
	}
	
}
