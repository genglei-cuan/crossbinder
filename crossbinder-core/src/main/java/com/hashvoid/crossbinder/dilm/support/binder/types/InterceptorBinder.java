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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.GlobalLifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.InjectorAware;
import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.LocatorAware;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class InterceptorBinder implements Binder {

	private static final Logger LOGGER = Logger.getLogger(InterceptorBinder.class.getName());

	private Class<?>        implCls;
	private BinderContext   binderCtxt;
	private Set<Class<?>>   ifaceTypes;
	private Set<Dependency> dependencies;
	private Object          interceptor;

	InterceptorBinder(Class<?> implCls, BinderContext ctxt) {
		this.implCls = implCls;
		binderCtxt = ctxt;
		ifaceTypes = new HashSet<>();
		if(GlobalLifecycleInterceptor.class.isAssignableFrom(implCls)) {
			ifaceTypes.add(GlobalLifecycleInterceptor.class);
		}
		if(LifecycleInterceptor.class.isAssignableFrom(implCls)) {
			ifaceTypes.add(LifecycleInterceptor.class);
		}
		if(MethodInterceptor.class.isAssignableFrom(implCls)) {
			ifaceTypes.add(MethodInterceptor.class);
		}
		dependencies = new InjectProcessor().extractDependencies(implCls);
		new InitProcessor().resolve(implCls);
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface Binder

	@Override
	public String getName() {
		// interceptors are not named. return null ... always.
		return null;
	}

	@Override
	public Set<Class<?>> getInterfaceTypes() {
		return ifaceTypes;
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
		if(!type.equals(GlobalLifecycleInterceptor.class) &&
				!type.equals(LifecycleInterceptor.class) && !type.equals(MethodInterceptor.class)) {
			LOGGER.warning(String.format("wrong target type, %s", type.getName()));
			throw new CrossbinderException("wrong target type expected. "
					+ "must be one of GlobalLifecycleInterceptor, LifecycleInterceptor or MethodInterceptor");
		}
		if(!type.isAssignableFrom(implCls)) {
			LOGGER.warning(String.format("{%s} unable to typecast to target type {%s}",
					implCls.getName(), type.getName()));
			throw new CrossbinderException("unable to typecast to target type");
		}
		return type.cast(interceptor);
	}

	@Override
	public void start() {
		// Create new interceptor instance via invocation of default constructor.
		try {
			interceptor = implCls.newInstance();
		}
		catch (InstantiationException | IllegalAccessException exep) {
			throw new CrossbinderException("unable to instantiate interceptor", exep);
		}

		// Process annotations and inject configuration.
		new ConfigProcessor(binderCtxt.getConfigurationProviders()).configure(interceptor);

		// Do injection
		if(interceptor instanceof LocatorAware) {
			((LocatorAware) interceptor).setLocator(binderCtxt.getLocator());
		}
		if(interceptor instanceof InjectorAware) {
			((InjectorAware) interceptor).setInjector(binderCtxt.getInjector());
		}
		new InjectProcessor().injectDependencies(interceptor, binderCtxt.getLocator());

		// Invoke init method on target instance
		new InitProcessor().execute(interceptor);
	}

	/* (non-Javadoc)
	 * @see com.crossbinder.dilm.internal.bind.Binder#stop()
	 */
	@Override
	public void stop() {
		// TODO: run finalizers.
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
		if(obj instanceof InterceptorBinder) {
			InterceptorBinder binder = (InterceptorBinder) obj;
			return (binder.implCls != null && implCls != null && binder.implCls.equals(implCls));
		}
		return false;
	}
}
