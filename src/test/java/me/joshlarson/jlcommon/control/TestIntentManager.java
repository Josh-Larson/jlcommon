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

import me.joshlarson.jlcommon.concurrency.Delay;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnit4.class)
public class TestIntentManager {
	
	@Test
	public void testBroadcast() {
		IntentManager intentManager = new IntentManager(1);
		intentManager.initialize();
		IntentManager.setInstance(intentManager);
		
		registerDummyHandler();
		TestIntent test = new TestIntent();
		test.broadcast();
		long started = System.nanoTime();
		while (!test.isComplete() && System.nanoTime() - started <= 1E9) {
			Delay.sleepMicro(10);
		}
		Assert.assertTrue(test.isBroadcasted());
		Assert.assertTrue(test.isComplete());
		
		intentManager.terminate();
	}
	
	@Test
	public void testServiceRegistration() {
		IntentManager intentManager = new IntentManager(1);
		intentManager.initialize();
		IntentManager.setInstance(intentManager);
		
		AtomicInteger called = new AtomicInteger(0);
		TestService service = new TestService(called);
		service.setIntentManager(intentManager);
		
		TestIntent test = new TestIntent();
		test.broadcast();
		long started = System.nanoTime();
		while (!test.isComplete() && System.nanoTime() - started <= 1E9) {
			Delay.sleepMicro(10);
		}
		Assert.assertTrue(test.isBroadcasted());
		Assert.assertTrue(test.isComplete());
		Assert.assertEquals(1, called.get());
		
		service.setIntentManager(null);
		
		called.set(0);
		test = new TestIntent();
		test.broadcast();
		Delay.sleepMilli(100);
		Assert.assertTrue(test.isBroadcasted());
		Assert.assertTrue(test.isComplete());
		Assert.assertEquals(0, called.get());
		
		intentManager.terminate();
	}
	
	@Test
	public void testSubServiceRegistration() {
		IntentManager intentManager = new IntentManager(1);
		intentManager.initialize();
		IntentManager.setInstance(intentManager);
		
		AtomicInteger called = new AtomicInteger(0);
		TestSubService service = new TestSubService(called);
		service.setIntentManager(intentManager);
		
		TestIntent test = new TestIntent();
		test.broadcast();
		long started = System.nanoTime();
		while (!test.isComplete() && System.nanoTime() - started <= 1E9) {
			Delay.sleepMicro(10);
		}
		Assert.assertTrue(test.isBroadcasted());
		Assert.assertTrue(test.isComplete());
		Assert.assertEquals(2, called.get());
		
		service.setIntentManager(null);
		
		called.set(0);
		test = new TestIntent();
		test.broadcast();
		Delay.sleepMilli(100);
		Assert.assertTrue(test.isBroadcasted());
		Assert.assertTrue(test.isComplete());
		Assert.assertEquals(0, called.get());
		
		intentManager.terminate();
	}
	
	private static void registerDummyHandler() {
		IntentManager intentManager = IntentManager.getInstance();
		Assert.assertNotNull(intentManager);
		intentManager.registerForIntent(TestIntent.class, intent -> {});
	}
	
	private static class TestService extends Service {
		
		private final AtomicInteger called;
		
		public TestService(AtomicInteger called) {
			this.called = called;
		}
		
		@IntentHandler
		private void handleTestIntent(TestIntent ti) {
			called.incrementAndGet();
		}
		
	}
	
	private static class TestSubService extends TestService {
		
		private final AtomicInteger called;
		
		public TestSubService(AtomicInteger called) {
			super(called);
			this.called = called;
		}
		
		@IntentHandler
		private void handleTestIntentTwo(TestIntent ti) {
			called.incrementAndGet();
		}
		
	}
	
	private static class TestIntent extends Intent {
		
	}
	
}
