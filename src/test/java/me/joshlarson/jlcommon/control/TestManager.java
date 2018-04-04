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
package me.joshlarson.jlcommon.control;

import me.joshlarson.jlcommon.control.Manager.ManagerCreationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestManager {
	
	@Test
	public void testServiceCreation() {
		Service s = new TestService();
	}
	
	@Test(expected=ManagerCreationException.class)
	public void testManagerNoChildren() {
		TestBadManager manager = new TestBadManager();
	}
	
	@Test
	public void testManagerSingleDepthChildren() {
		TestManagerSingleDepth manager = new TestManagerSingleDepth();
		Assert.assertEquals(1, manager.getChildren().size());
		Assert.assertSame(TestService.class, manager.getChildren().get(0).getClass());
	}
	
	@Test
	public void testManagerMultipleDepthChildren() {
		TestManagerMultipleDepth manager = new TestManagerMultipleDepth();
		Assert.assertEquals(1, manager.getChildren().size());
		Assert.assertSame(TestManagerSingleDepth.class, manager.getChildren().get(0).getClass());
		TestManagerSingleDepth subManager = (TestManagerSingleDepth) manager.getChildren().get(0);
		Assert.assertEquals(1, subManager.getChildren().size());
		Assert.assertSame(TestService.class, subManager.getChildren().get(0).getClass());
	}
	
	public static class TestBadManager extends Manager {}
	
	@ManagerStructure(children = { TestService.class})
	public static class TestManagerSingleDepth extends Manager {}
	
	@ManagerStructure(children = { TestManagerSingleDepth.class})
	public static class TestManagerMultipleDepth extends Manager {}
	
	public static class TestService extends Service {}
}
