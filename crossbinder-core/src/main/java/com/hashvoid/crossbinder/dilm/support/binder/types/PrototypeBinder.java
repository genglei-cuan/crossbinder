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

import com.hashvoid.crossbinder.dilm.Bindable;
import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.InjectorAware;
import com.hashvoid.crossbinder.dilm.LocatorAware;
import com.hashvoid.crossbinder.dilm.NonBindable;
import com.hashvoid.crossbinder.dilm.Prototype;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class PrototypeBinder implements Binder {

	private static final Logger LOGGER = Logger.getLogger(PrototypeBinder.class.getName());

	private Class<?>        implCls;
	private BinderContext   binderCtxt;
	private Set<Dependency> dependencies;
	private Set<Class<?>>   bindToList;

	PrototypeBinder(Class<?> implCls, BinderContext ctxt) {
		binderCtxt = ctxt;
		this.implCls = implCls;
		Set<Class<?>> ifaces = new HashSet<>();
		retrieveInterfaces(implCls, ifaces);
		Set<Class<?>> bindables = retrieveBindables(ifaces);
		if(bindables.isEmpty()) {
			bindToList = ifaces;
		}
		else {
			bindToList = bindables;
		}
		dependencies = new InjectProcessor().extractDependencies(implCls);
		new InitProcessor().resolve(implCls);
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface Binder

	@Override
	public String getName() {
		Prototype ann = implCls.getAnnotation(Prototype.class);
		return ann.name().trim();
	}

	@Override
	public Set<Class<?>> getInterfaceTypes() {
		return bindToList;
	}

	@Override
	public Set<Dependency> getDependencies() {
		return dependencies;
	}

	@Override
	public <T> T getInstance(Class<T> type) {
		if(type == null) {
			LOGGER.fine("lookup type cannot be null");
			return null;
		}
		if(!bindToList.contains(type)) {
			LOGGER.fine(String.format("instance not found type = %s", type.getName()));
			return null;
		}

		Object prototype = null;
		try {
			prototype = implCls.newInstance();
		}
		catch (InstantiationException | IllegalAccessException exep) {
			throw new CrossbinderException("unable to instantiate prototype " + this, exep);
		}

		// Notify event processors that object has been created.
		binderCtxt.getEventHandler().instanceCreated(prototype);

		// Process annotations and inject configuration.
		new ConfigProcessor(binderCtxt.getConfigurationProviders()).configure(prototype);

		// Do injection
		if(prototype instanceof LocatorAware) {
			((LocatorAware) prototype).setLocator(binderCtxt.getLocator());
		}
		if(prototype instanceof InjectorAware) {
			((InjectorAware) prototype).setInjector(binderCtxt.getInjector());
		}
		new InjectProcessor().injectDependencies(prototype, binderCtxt.getLocator());

		// Notify event processors that object has been injected.
		binderCtxt.getEventHandler().instanceInjected(prototype);

		// Invoke init method on target instance
		new InitProcessor().execute(prototype);

		// Notify event processors that object has been initialized.
		binderCtxt.getEventHandler().instanceInitialized(prototype);

		Object protoProxy = createProxyInstance(prototype);
		return type.cast(protoProxy);
	}

	@Override
	public void start() {
		// NOOP
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("prototype: ")
			.append(implCls.getName());
		return builder.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of base class Object

	@Override
	public int hashCode() {
		if(implCls != null) {
			return implCls.hashCode();
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PrototypeBinder) {
			PrototypeBinder binder = (PrototypeBinder) obj;
			return (binder.implCls != null && implCls != null && binder.implCls.equals(implCls));
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void retrieveInterfaces(Class<?> cls, Set<Class<?>> interfaces) {
		if(!cls.isInterface()) {
			Class<?> superCls = cls.getSuperclass();
			if(superCls != null) {
				retrieveInterfaces(superCls, interfaces);
			}
		}

		Class<?>[] ifaces = cls.getInterfaces();
		if(ifaces.length == 0) {
			return;
		}
		for(Class<?> iface : ifaces) {
			if(iface.getAnnotation(NonBindable.class) == null) {
				interfaces.add(iface);
			}
		}
		for(Class<?> iface : ifaces) {
			retrieveInterfaces(iface, interfaces);
		}
	}

	private Set<Class<?>> retrieveBindables(Set<Class<?>> interfaces) {
		Set<Class<?>> result = new HashSet<Class<?>>();
		for(Class<?> iface : interfaces) {
			if(iface.getAnnotation(Bindable.class) != null) {
				result.add(iface);
			}
		}
		return result;
	}

	private Object createProxyInstance(Object prototype) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) {
			cl = getClass().getClassLoader();
		}
		Class<?>[] ifaces = bindToList.toArray(new Class[bindToList.size()]);
		return Proxy.newProxyInstance(cl, ifaces, new PrototypeInvocationHandler(prototype));
	}

	////////////////////////////////////////////////////////////////////////////
	// Inner class that implements the InvocationHandler

	private class PrototypeInvocationHandler implements InvocationHandler {

		private Object prototype;

		public PrototypeInvocationHandler(Object proto) {
			prototype = proto;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Method outMthd = implCls.getMethod(method.getName(), method.getParameterTypes());

			binderCtxt.getEventHandler().beforeMethod(prototype, outMthd, args);
			Object retVal = null;
			try {
				retVal = binderCtxt.getEventHandler().wrapMethod(prototype, outMthd, args);
			}
			catch(InvocationTargetException exep) {
				binderCtxt.getEventHandler().afterMethodFail(prototype, outMthd, exep.getCause());
				throw exep.getCause();
			}
			binderCtxt.getEventHandler().afterMethodSuccess(prototype, outMthd, retVal);
			return retVal;
		}
	}
}
