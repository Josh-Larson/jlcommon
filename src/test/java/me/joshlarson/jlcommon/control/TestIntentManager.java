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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TestIntentManager {
	
	@Test
	public void testStartStop() {
		IntentManager intentManager = new IntentManager(1);
		
		Assert.assertTrue(intentManager.isRunning());
		
		intentManager.close();
		
		Assert.assertFalse(intentManager.isRunning());
	}
	
	@Test
	public void testBroadcast() {
		try (IntentManager intentManager = new IntentManager(1)) {
			IntentManager.setInstance(intentManager);
			
			registerDummyHandler();
			TestIntent test = new TestIntent();
			test.broadcast();
			waitForTrue(test::isComplete);
			
			Assert.assertEquals(0, intentManager.getIntentCount());
			Assert.assertTrue(test.isBroadcasted());
			Assert.assertTrue(test.isComplete());
		}
	}
	
	@Test
	public void testServiceRegistration() {
		try (IntentManager intentManager = new IntentManager(1)) {
			IntentManager.setInstance(intentManager);
			
			AtomicInteger called = new AtomicInteger(0);
			TestService service = new TestService(called);
			service.setIntentManager(intentManager);
			
			TestIntent test = new TestIntent();
			test.broadcast();
			long started = System.nanoTime();
			while (!test.isComplete() && System.nanoTime() - started <= 1E9) {
				Delay.sleepMilli(10);
			}
			Assert.assertEquals(0, intentManager.getIntentCount());
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
		}
	}
	
	@Test
	public void testSubServiceRegistration() {
		try (IntentManager intentManager = new IntentManager(1)) {
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
			Assert.assertEquals(0, intentManager.getIntentCount());
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
		}
	}
	
	private static void registerDummyHandler() {
		IntentManager intentManager = IntentManager.getInstance();
		Assert.assertNotNull(intentManager);
		intentManager.registerForIntent(TestIntent.class, "dummy", intent -> {});
	}
	
	private static void waitForTrue(Supplier<Boolean> test) {
		long started = System.nanoTime();
		while (!test.get() && System.nanoTime() - started <= 1E9) {
			Delay.sleepMicro(10);
		}
		Assert.assertTrue(test.get());
	}
	
	private static class TestService extends Service {
		
		private final AtomicInteger called;
		
		public TestService(AtomicInteger called) {
			this.called = called;
		}
		
		@IntentHandler
		private void handleTestIntent(TestIntent ti) {
			Assert.assertEquals(1, getIntentManager().getIntentCount());
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
			Assert.assertEquals(1, getIntentManager().getIntentCount());
			called.incrementAndGet();
		}
		
	}
	
	private static class TestIntent extends Intent {
		
	}
	
}
