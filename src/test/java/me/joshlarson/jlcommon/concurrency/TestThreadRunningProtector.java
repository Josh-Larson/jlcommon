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
package me.joshlarson.jlcommon.concurrency;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestThreadRunningProtector {
	
	@Test
	public void testStart() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
	}
	
	@Test(expected=AssertionError.class)
	public void testStartAlreadyStarted() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		protector.start();
	}
	
	@Test
	public void testStartStop() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		protector.stop();
	}
	
	@Test(expected=AssertionError.class)
	public void testStartStopStop() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		protector.stop();
		protector.stop();
	}
	
	@Test(expected=AssertionError.class)
	public void testStopNotStarted() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.stop();
	}
	
	@Test
	public void testExpectCreatedRunning() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		protector.expectCreated();
		protector.expectRunning();
		Assert.assertTrue(protector.isRunning());
	}
	
	@Test(expected=AssertionError.class)
	public void testExpectCreatedNotStarted() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.expectCreated();
	}
	
	@Test(expected=AssertionError.class)
	public void testExpectRunningNotStarted() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		protector.stop();
		protector.expectRunning();
	}
	
	@Test(expected=AssertionError.class)
	public void testExpectRunningStopped() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.expectRunning();
	}
	
	@Test
	public void testExpectCreatedStopped() {
		ThreadRunningProtector protector = new ThreadRunningProtector();
		protector.start();
		Assert.assertTrue(protector.isRunning());
		protector.expectCreated();
		protector.stop();
		protector.expectCreated();
	}
	
}
