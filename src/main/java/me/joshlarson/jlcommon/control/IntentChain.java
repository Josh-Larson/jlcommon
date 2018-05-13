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
import java.util.concurrent.atomic.AtomicReference;

public class IntentChain {
	
	private final IntentManager intentManager;
	private final AtomicReference<Intent> intent;
	
	public IntentChain() {
		this(IntentManager.getInstance());
	}
	
	public IntentChain(IntentManager intentManager) {
		this(intentManager, null);
	}
	
	public IntentChain(@Nullable Intent i) {
		this(IntentManager.getInstance(), i);
	}
	
	public IntentChain(IntentManager intentManager, @Nullable Intent i) {
		this.intentManager = intentManager;
		this.intent = new AtomicReference<>(null);
	}
	
	public void reset() {
		intent.set(null);
	}
	
	public void broadcastAfter(IntentManager intentManager, @NotNull Intent i) {
		i.broadcastAfterIntent(intent.getAndSet(i), intentManager);
	}
	
	public void broadcastAfter(@NotNull Intent i) {
		Objects.requireNonNull(intentManager, "IntentManager is null");
		i.broadcastAfterIntent(intent.getAndSet(i), intentManager);
	}
	
}
