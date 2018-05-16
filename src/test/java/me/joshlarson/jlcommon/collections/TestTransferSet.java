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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashSet;
import java.util.Set;

@RunWith(JUnit4.class)
public class TestTransferSet {
	
	@Test
	public void testBasicSynchronize() {
		Set<String> source = new HashSet<>();
		TransferSet<String, String> destination = new TransferSet<>(s -> s, s -> s);
		source.add("obj1");
		source.add("obj2");
		
		Assert.assertEquals(0, destination.size());
		Assert.assertEquals(2, source.size());
		Assert.assertTrue(source.contains("obj1"));
		Assert.assertTrue(source.contains("obj2"));
		
		destination.synchronize(source);
		Assert.assertEquals(2, destination.size());
		Assert.assertEquals(2, source.size());
		Assert.assertTrue(destination.contains("obj1"));
		Assert.assertTrue(destination.contains("obj2"));
	}
	
	@Test
	public void testTransferSynchronize() {
		Set<Integer> source = new HashSet<>();
		TransferSet<Integer, String> destination = new TransferSet<>(s -> s, String::valueOf);
		source.add(10);
		source.add(100);
		
		Assert.assertEquals(0, destination.size());
		Assert.assertEquals(2, source.size());
		Assert.assertTrue(source.contains(10));
		Assert.assertTrue(source.contains(100));
		
		destination.synchronize(source);
		Assert.assertEquals(2, destination.size());
		Assert.assertEquals(2, source.size());
		Assert.assertTrue(destination.contains("10"));
		Assert.assertTrue(destination.contains("100"));
	}
	
}
