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

import me.joshlarson.jlcommon.concurrency.beans.ConcurrentCollection.ComplexCollectionChangedListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class TestConcurrentCollection {
	
	@Test
	public void testListCallbacks() {
		ConcurrentList<String> list = new ConcurrentList<>();
		ConcurrentBoolean cbool = new ConcurrentBoolean(false);
		
		Runnable run = () -> cbool.set(true);
		ComplexCollectionChangedListener<ConcurrentCollection<List<String>, String>> complexRun = l -> cbool.set(true);
		list.addCollectionChangedListener(run);
		list.add("test1");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test1", list.get(0));
		
		list.clear();
		cbool.set(false);
		list.removeCollectionChangedListener(run);
		list.add("test2");
		Assert.assertFalse(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test2", list.get(0));
		
		list.clear();
		cbool.set(false);
		list.addCollectionChangedListener("list", complexRun);
		list.add("test3");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test3", list.get(0));
		
		list.clear();
		cbool.set(false);
		list.removeCollectionChangedListener("list");
		list.addCollectionChangedListener(complexRun);
		list.add("test4");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test4", list.get(0));
		
		list.clear();
		cbool.set(false);
		list.clearCollectionChangedListeners();
		list.add("test5");
		Assert.assertFalse(cbool.get());
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("test5", list.get(0));
	}
	
	@Test
	public void testSet() {
		ConcurrentSet<String> list = new ConcurrentSet<>();
		ConcurrentBoolean cbool = new ConcurrentBoolean(false);
		
		Runnable run = () -> cbool.set(true);
		list.addCollectionChangedListener(run);
		
		list.add("test1");
		Assert.assertTrue(cbool.get());
		Assert.assertEquals(1, list.size());
		
		cbool.set(false);
		list.add("test1");
		Assert.assertFalse(cbool.get());
		Assert.assertEquals(1, list.size());
	}
	
	@Test
	public void testQueue() {
		ConcurrentQueue<String> queue = new ConcurrentQueue<>();
		
		queue.add("testing");
		Assert.assertEquals("testing", queue.poll());
	}
	
}
