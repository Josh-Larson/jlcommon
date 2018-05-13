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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class Intent implements Comparable<Intent> {
	
	private boolean broadcasted;
	private boolean complete;
	private Intent parallel;
	private Intent sequential;
	private Consumer<Intent> completedCallback;
	
	protected Intent() {
		this.broadcasted = false;
		this.complete = false;
		this.parallel = null;
		this.sequential = null;
		this.completedCallback = null;
	}
	
	/**
	 * Called when the intent has been completed
	 */
	synchronized void markAsComplete(@NotNull IntentManager intentManager) {
		this.complete = true;
		if (sequential != null)
			sequential.broadcast(intentManager);
		sequential = null;
		parallel = null;
	}
	
	Consumer<Intent> getCompletedCallback() {
		return completedCallback;
	}
	
	public void setCompletedCallback(@NotNull Consumer<Intent> completedCallback) {
		this.completedCallback = completedCallback;
	}
	
	public IntentPriority getPriority() {
		return IntentPriority.MEDIUM;
	}
	
	/**
	 * Determines whether or not the intent has been broadcasted and processed by the system
	 *
	 * @return TRUE if the intent has been broadcasted and processed, FALSE otherwise
	 */
	public synchronized boolean isComplete() {
		return complete;
	}
	
	/**
	 * Determines whether or not the intent has been broadcasted to the system
	 *
	 * @return TRUE if the intent has been broadcasted, FALSE otherwise
	 */
	public synchronized boolean isBroadcasted() {
		return broadcasted;
	}
	
	/**
	 * Waits for the intent as the parameter to finish before this intent starts
	 *
	 * @param i the intent to execute after
	 */
	public synchronized void broadcastAfterIntent(@Nullable Intent i) {
		IntentManager intentManager = IntentManager.getInstance();
		Objects.requireNonNull(intentManager, "IntentManager is null");
		broadcastAfterIntent(i, intentManager);
	}
	
	/**
	 * Waits for the intent as the parameter to finish before this intent starts
	 *
	 * @param i             the intent to execute after
	 * @param intentManager the intent manager to broadcast this intent on
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public synchronized void broadcastAfterIntent(@Nullable Intent i, @NotNull IntentManager intentManager) {
		if (i == null) {
			broadcast(intentManager);
			return;
		}
		synchronized (i) {
			if (i.isComplete())
				broadcast(intentManager);
			else
				i.setAsSequential(this);
		}
	}
	
	/**
	 * Waits for the intent as the parameter to start before this intent starts
	 *
	 * @param i the intent to execute with
	 */
	public synchronized void broadcastWithIntent(@Nullable Intent i) {
		IntentManager intentManager = IntentManager.getInstance();
		Objects.requireNonNull(intentManager, "IntentManager is null");
		broadcastWithIntent(i, intentManager);
	}
	
	/**
	 * Waits for the intent as the parameter to start before this intent starts
	 *
	 * @param i             the intent to execute with
	 * @param intentManager the intent manager to broadcast this intent on
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	public synchronized void broadcastWithIntent(@Nullable Intent i, @NotNull IntentManager intentManager) {
		if (i == null) {
			broadcast(intentManager);
			return;
		}
		synchronized (i) {
			if (!isComplete()) {
				setAsParallel(i);
			}
			broadcast(intentManager);
		}
	}
	
	/**
	 * Broadcasts this node to the system
	 */
	public synchronized void broadcast() {
		IntentManager intentManager = IntentManager.getInstance();
		Objects.requireNonNull(intentManager, "IntentManager is null");
		broadcast(intentManager);
	}
	
	/**
	 * Broadcasts this node to the system with the specified intent manager
	 *
	 * @param intentManager the intent manager to broadcast this intent on
	 */
	public synchronized void broadcast(@NotNull IntentManager intentManager) {
		if (broadcasted)
			throw new IllegalStateException("Intent has already been broadcasted!");
		broadcasted = true;
		intentManager.broadcastIntent(this);
		if (parallel != null)
			parallel.broadcast(intentManager);
		parallel = null;
	}
	
	@Override
	public synchronized String toString() {
		return getClass().getSimpleName();
	}
	
	@Override
	public int compareTo(@NotNull Intent i) {
		return getPriority().comparePriorityTo(i.getPriority());
	}
	
	private synchronized void setAsParallel(@NotNull Intent i) {
		if (parallel == null)
			parallel = i;
		else
			parallel.setAsParallel(i);
	}
	
	private synchronized void setAsSequential(@NotNull Intent i) {
		if (sequential == null)
			sequential = i;
		else
			sequential.setAsParallel(i);
	}
	
	public enum IntentPriority implements Comparable<IntentPriority> {
		VERY_LOW(10),
		LOW(8),
		MEDIUM(5),
		HIGH(2),
		VERY_HIGH(0);
		
		private final int priority;
		
		IntentPriority(int priority) {
			this.priority = priority;
		}
		
		public int getPriority() {
			return priority;
		}
		
		public int comparePriorityTo(@NotNull IntentPriority ip) {
			return Integer.compare(priority, ip.priority);
		}
		
	}
	
}
