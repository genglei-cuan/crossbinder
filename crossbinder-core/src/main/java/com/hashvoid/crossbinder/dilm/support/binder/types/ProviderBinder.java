/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.hashvoid.crossbinder.dilm.support.binder.types;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class ProviderBinder implements Binder {

	private static final Logger LOGGER = Logger.getLogger(ProviderBinder.class.getName());

	private String              name;
	private ProviderBinderGroup binderGroup;
	private Method              provMthd;

	ProviderBinder(ProviderBinderGroup group, String name, Method mthd) {
		this.name = name;
		binderGroup = group;
		provMthd = mthd;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface Binder

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<Class<?>> getInterfaceTypes() {
		HashSet<Class<?>> reset = new HashSet<>();
		reset.add(provMthd.getReturnType());
		return reset;
	}

	@Override
	public Set<Dependency> getDependencies() {
		return binderGroup.getDependencies();
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		if(type == null) {
			LOGGER.fine("lookup type cannot be null");
			return null;
		}

		try {
			Object result = provMthd.invoke(binderGroup.getProvider());
			Object protoProxy = createProxyInstance(result);
			return type.cast(protoProxy);
		}
		catch (IllegalAccessException
				| IllegalArgumentException | InvocationTargetException exep) {
			LOGGER.warning(String.format("unable to create provided instance: provider = %s, method = %s",
				provMthd.getDeclaringClass().getName(), provMthd.getName()));
			throw new CrossbinderException(
				"unable to create provided instance",
				(exep.getCause() != null)? exep.getCause() : exep);
		}
	}

	@Override
	public void start() {
		binderGroup.start();
	}

	/* (non-Javadoc)
	 * @see com.crossbinder.dilm.support.binder.Binder#stop()
	 */
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of base class Object

	@Override
	public int hashCode() {
		return provMthd.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ProviderBinder) {
			ProviderBinder binder = (ProviderBinder) obj;
			return binder.provMthd.equals(provMthd);
		}
		return false;
	}

	private Object createProxyInstance(Object prototype) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) {
			cl = getClass().getClassLoader();
		}
		return Proxy.newProxyInstance(cl, new Class[]{provMthd.getReturnType()},
				new ProvidedInvocationHandler(prototype));
	}

	////////////////////////////////////////////////////////////////////////////
	// Inner class that implements the InvocationHandler

	private class ProvidedInvocationHandler implements InvocationHandler {

		private Object provided;

		public ProvidedInvocationHandler(Object provided) {
			this.provided = provided;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Method outMthd = provided.getClass().getMethod(method.getName(), method.getParameterTypes());

			binderGroup.getBinderContext().getEventHandler()
					.beforeMethod(provided, outMthd, args);
			Object retVal = null;
			try {
				retVal = binderGroup.getBinderContext().getEventHandler()
						.wrapMethod(provided, outMthd, args);
			}
			catch(InvocationTargetException exep) {
				binderGroup.getBinderContext().getEventHandler()
						.afterMethodFail(provided, outMthd, exep.getCause());
				throw exep.getCause();
			}
			binderGroup.getBinderContext()
					.getEventHandler().afterMethodSuccess(provided, outMthd, retVal);
			return retVal;
		}
	}
}
