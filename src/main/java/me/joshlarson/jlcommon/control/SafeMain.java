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
import me.joshlarson.jlcommon.log.Log;

public class SafeMain {
	
	private static volatile boolean EXECUTING = false;
	private static volatile boolean ALIVE = false;
	private static volatile int RETURN_CODE = 0;
	
	/**
	 * Calls the specified runner, with the specified threadName, and the specified arguments. When the program is asked to stop, the thread will be interrupted. Once the thread terminates, the JVM
	 * will exit with an exit code specified by the return value of the function.
	 *
	 * @param threadName the name of the main thread group and thread
	 * @param runner     the main runner
	 * @param args       the arguments
	 */
	public static void main(String threadName, SafeMainRunnable runner, String[] args) {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(threadName));
		launchMain(threadName, createWrapper(runner, args));
		System.exit(RETURN_CODE);
	}
	
	/**
	 * Calls the specified runner, with the specified threadName, and no arguments. When the program is asked to stop, the thread will be interrupted. Once the thread terminates, the JVM will exit
	 * with an exit code of 0.
	 *
	 * @param threadName the name of the main thread group and thread
	 * @param runner     the main runner
	 */
	public static void main(String threadName, Runnable runner) {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(threadName));
		launchMain(threadName, createWrapper(runner));
		System.exit(RETURN_CODE);
	}
	
	private static void launchMain(String name, Runnable wrapper) {
		try {
			EXECUTING = true;
			ThreadGroup group = new ThreadGroup(name);
			Thread main = new Thread(group, wrapper, name);
			main.setUncaughtExceptionHandler((t, e) -> Log.e(e));
			main.start();
			ALIVE = true;
			while (ALIVE) {
				try {
					main.join();
				} catch (InterruptedException e) {
					main.interrupt();
					Delay.clearInterrupted();
				}
			}
		} finally {
			EXECUTING = false;
		}
	}
	
	private static Runnable createWrapper(Runnable runner) {
		return () -> {
			ALIVE = true;
			try {
				runner.run();
			} catch (Throwable t) {
				Log.e("Uncaught exception in main():");
				Log.e(t);
			}
			RETURN_CODE = 0;
			ALIVE = false;
		};
	}
	
	private static Runnable createWrapper(SafeMainRunnable runner, String[] args) {
		return () -> {
			ALIVE = true;
			try {
				RETURN_CODE = runner.main(args);
			} catch (Throwable t) {
				RETURN_CODE = -1;
				Log.e("Uncaught exception in main():");
				Log.e(t);
			}
			ALIVE = false;
		};
	}
	
	public interface SafeMainRunnable {
		
		int main(String[] args);
	}
	
	private static class ShutdownHook extends Thread {
		
		private final Thread mainThread;
		
		public ShutdownHook(String name) {
			super(name + "-cleanup");
			this.mainThread = Thread.currentThread();
		}
		
		public void run() {
			mainThread.interrupt();
			while (EXECUTING) {
				Delay.sleepMicro(10);
			}
		}
		
	}
	
}
