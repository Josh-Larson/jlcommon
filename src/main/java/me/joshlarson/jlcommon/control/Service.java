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

import me.joshlarson.jlcommon.log.Log;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A Service is a class that does a specific job for the application
 */
public abstract class Service implements ServiceBase {
	
	private final Map<Class<? extends Intent>, List<Consumer<? extends Intent>>> registration;
	private final AtomicReference <IntentManager> intentManager;
	
	public Service() {
		this.registration = new HashMap<>();
		this.intentManager = new AtomicReference<>(null);
	}
	
	@Override
	public boolean initialize() {
		return true;
	}
	
	@Override
	public boolean start() {
		return true;
	}
	
	@Override
	public boolean stop() {
		return true;
	}
	
	@Override
	public boolean terminate() {
		return true;
	}
	
	@Override
	public boolean isOperational() {
		return true;
	}
	
	@Override
	public void setIntentManager(IntentManager intentManager) {
		IntentManager prev = this.intentManager.getAndSet(intentManager);
		if (prev != null) {
			unregisterIntentHandlers(prev);
		}
		if (intentManager != null) {
			registerIntentHandlers(intentManager);
		}
	}
	
	protected IntentManager getIntentManager() {
		return intentManager.get();
	}
	
	@SuppressWarnings("unchecked")
	private void unregisterIntentHandlers(@Nonnull IntentRegistry registry) {
		for (Entry<Class<? extends Intent>, List<Consumer<? extends Intent>>> e : registration.entrySet()) {
			e.getValue().forEach(c -> registry.unregisterForIntent((Class<Intent>)e.getKey(), (Consumer<Intent>)c));
		}
		registration.clear();
	}
	
	@SuppressWarnings("unchecked")
	private void registerIntentHandlers(@Nonnull IntentRegistry registry) {
		registerIntentHandlers(getClass(), registry);
	}
	
	@SuppressWarnings("unchecked")
	private void registerIntentHandlers(@Nonnull Class<? extends Service> klass, @Nonnull IntentRegistry registry) {
		for (Method m : klass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(IntentHandler.class)) {
				if (m.getParameterCount() == 1) {
					Parameter p = m.getParameters()[0];
					Class<?> paramClass = p.getType();
					if (Intent.class.isAssignableFrom(paramClass)) {
						if (Modifier.isProtected(m.getModifiers()) || Modifier.isPublic(m.getModifiers()))
							Log.w("Intent handler '%s::%s' is not (package) private!", klass.getName(), m.getName());
						m.setAccessible(true);
						Class<Intent> intentClass = (Class<Intent>) paramClass;
						Consumer<Intent> intentConsumer = i -> invoke(m, i, paramClass);
						registry.registerForIntent(intentClass, intentConsumer);
						registration.computeIfAbsent(intentClass, c -> new CopyOnWriteArrayList<>()).add(intentConsumer);
					}
				}
			}
		}
		Class<?> superKlass = klass.getSuperclass();
		if (Service.class.isAssignableFrom(superKlass))
			registerIntentHandlers((Class<? extends Service>) superKlass, registry);
	}
	
	private void invoke(Method m, Intent intent, Class<?> klass) {
		try {
			m.invoke(this, klass.cast(intent));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
