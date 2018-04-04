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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class IntentMultiplexer {
	
	private final Map<Class<?>, Method> methods;
	private final Service service;
	private final int expectedArgs;
	
	public IntentMultiplexer(@Nonnull Service service, Class<?>... parameters) {
		this.methods = new HashMap<>();
		this.service = service;
		this.expectedArgs = parameters.length;
		
		getMethods(methods, service.getClass(), parameters);
	}
	
	public void call(Object... args) {
		if (args.length != expectedArgs)
			throw new IllegalArgumentException("Invalid arguments!");
		try {
			Method method = methods.get(args[args.length-1].getClass());
			if (method != null)
				method.invoke(service, args);
		} catch (InvocationTargetException | IllegalAccessException e) {
			Log.e(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void getMethods(Map<Class<?>, Method> methods, Class<? extends Service> klass, Class<?>... parameters) {
		method_loop:
		for (Method m : klass.getDeclaredMethods()) {
			if (!m.isAnnotationPresent(Multiplexer.class))
				continue;
			
			Parameter[] params = m.getParameters();
			if (params.length != parameters.length)
				continue;
			for (int i = 0; i < params.length; i++) {
				if (!parameters[i].isAssignableFrom(params[i].getType()))
					continue method_loop;
			}
			
			m.setAccessible(true);
			methods.put(params[params.length-1].getType(), m);
		}
		Class<?> superKlass = klass.getSuperclass();
		if (Service.class.isAssignableFrom(superKlass))
			getMethods(methods, (Class<? extends Service>) superKlass, parameters);
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Multiplexer {
		
	}
	
}
