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

import me.joshlarson.jlcommon.concurrency.beans.ConcurrentMap.ComplexMapChangedListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestConcurrentMap {
	
	@Test
	public void testMapCallbacks() {
		ConcurrentMap<String, String> list = new ConcurrentMap<>();
		ConcurrentBoolean cbool = new ConcurrentBoolean(false);
		
		Runnable run = () -> cbool.set(true);
		ComplexMapChangedListener<ConcurrentMap<String, String>> complexRun = l -> cbool.set(true);
		list.addMapChangedListener(run);
		list.put("test1", "test");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test", list.get("test1"));
		
		list.clear();
		cbool.set(false);
		list.removeMapChangedListener(run);
		list.put("test2", "test");
		Assert.assertFalse(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test", list.get("test2"));
		
		list.clear();
		cbool.set(false);
		list.addMapChangedListener("list", complexRun);
		list.put("test3", "test");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test", list.get("test3"));
		
		list.clear();
		cbool.set(false);
		list.removeMapChangedListener("list");
		list.addMapChangedListener(complexRun);
		list.put("test4", "test");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test", list.get("test4"));
		
		list.clear();
		cbool.set(false);
		list.clearMapChangedListeners();
		list.put("test5", "test");
		Assert.assertFalse(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test", list.get("test5"));
	}
	
}
