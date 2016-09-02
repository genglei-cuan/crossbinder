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
import com.hashvoid.crossbinder.dilm.Singleton;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class SingletonBinder implements Binder {

	private static final Logger LOGGER = Logger.getLogger(SingletonBinder.class.getName());

	private Class<?>        implCls;
	private BinderContext   binderCtxt;
	private Set<Dependency> dependencies;
	private Set<Class<?>>   bindToList;
	private Object          singleton;
	private Object          proxySingleton;

	SingletonBinder(Class<?> implCls, BinderContext ctxt) {
		this.implCls = implCls;
		binderCtxt = ctxt;
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
		Singleton ann = implCls.getAnnotation(Singleton.class);
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
		return type.cast(proxySingleton);
	}

	@Override
	public void start() {
		LOGGER.fine(String.format("starting singleton = {%s}", implCls.getName()));
		Singleton ann = implCls.getAnnotation(Singleton.class);
		if(!ann.lazyLoading()) {
			// keep the instance ready in case lazy loading is not true
			createInstance();
		}
		//and create the proxy instance for the real instance
		createProxyInstance();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("singleton: ")
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
		if(obj instanceof SingletonBinder) {
			SingletonBinder binder = (SingletonBinder) obj;
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

	private void createInstance() throws CrossbinderException {
		try {
			singleton = implCls.newInstance();
		}
		catch (InstantiationException | IllegalAccessException exep) {
			throw new CrossbinderException("unable to instantiate singeton " + this, exep);
		}

		// Notify event processors that object has been created.
		binderCtxt.getEventHandler().instanceCreated(singleton);

		// Process annotations and inject configuration.
		new ConfigProcessor(binderCtxt.getConfigurationProviders()).configure(singleton);

		// Do injection
		if(singleton instanceof LocatorAware) {
			((LocatorAware) singleton).setLocator(binderCtxt.getLocator());
		}
		if(singleton instanceof InjectorAware) {
			((InjectorAware) singleton).setInjector(binderCtxt.getInjector());
		}
		new InjectProcessor().injectDependencies(singleton, binderCtxt.getLocator());

		// Notify event processors that object has been injected.
		binderCtxt.getEventHandler().instanceInjected(singleton);

		// Invoke init method on target instance
		new InitProcessor().execute(singleton);

		// Notify event processors that object has been initialized.
		binderCtxt.getEventHandler().instanceInitialized(singleton);
	}

	private void createProxyInstance() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) {
			cl = getClass().getClassLoader();
		}
		Class<?>[] ifaces = bindToList.toArray(new Class[bindToList.size()]);
		proxySingleton = Proxy.newProxyInstance(cl, ifaces, new SingletonInvocationHandler());
	}

	////////////////////////////////////////////////////////////////////////////
	// Inner class that implements the InvocationHandler

	private class SingletonInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(singleton == null) {
				createInstance();
			}
			Method outMthd = implCls.getMethod(method.getName(), method.getParameterTypes());

			binderCtxt.getEventHandler().beforeMethod(singleton, outMthd, args);
			Object retVal = null;
			try {
				retVal = binderCtxt.getEventHandler().wrapMethod(singleton, outMthd, args);
			}
			catch(InvocationTargetException exep) {
				binderCtxt.getEventHandler().afterMethodFail(singleton, outMthd, exep.getCause());
				throw exep.getCause();
			}
			binderCtxt.getEventHandler().afterMethodSuccess(singleton, outMthd, retVal);
			return retVal;
		}
	}
}
