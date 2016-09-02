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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.InjectorAware;
import com.hashvoid.crossbinder.dilm.LocatorAware;
import com.hashvoid.crossbinder.dilm.Provides;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

class ProviderBinderGroup {

	private Class<?>        implCls;
	private BinderContext   binderCtxt;
	private Set<Dependency> dependencies;
	private List<Binder>    binders;
	private Object          provider;
	private boolean         started;

	public ProviderBinderGroup(Class<?> implCls, BinderContext ctxt) {
		this.implCls = implCls;
		binderCtxt = ctxt;
		dependencies = new InjectProcessor().extractDependencies(implCls);
		new InitProcessor().resolve(implCls);
		createBinders();
	}

	public Set<Dependency> getDependencies() {
		return dependencies;
	}

	public synchronized void start() {
		if(started) {
			return;
		}

		// Create new provider instance via invocation of default constructor.
		try {
			provider = implCls.newInstance();
		}
		catch (InstantiationException | IllegalAccessException exep) {
			throw new CrossbinderException("unable to instantiate provider", exep);
		}

		// Process annotations and inject configuration.
		new ConfigProcessor(binderCtxt.getConfigurationProviders()).configure(provider);

		// Do injection
		if(provider instanceof LocatorAware) {
			((LocatorAware) provider).setLocator(binderCtxt.getLocator());
		}
		if(provider instanceof InjectorAware) {
			((InjectorAware) provider).setInjector(binderCtxt.getInjector());
		}
		new InjectProcessor().injectDependencies(provider, binderCtxt.getLocator());

		// Invoke init method on target instance
		new InitProcessor().execute(provider);

		started = true;
	}

	public Object getProvider() {
		return provider;
	}

	public List<Binder> getBinders() {
		return binders;
	}

	public BinderContext getBinderContext() {
		return binderCtxt;
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void createBinders() {
		List<Class<?>> uniqueCls = new ArrayList<>();
		binders = new ArrayList<>();

		Method[] methods = implCls.getMethods();
		for(Method method : methods) {
			if(method.getAnnotation(Provides.class) == null) {
				//method must be annotated with @Provides
				continue;
			}
			if(method.getParameterCount() != 0) {
				//method should not take any arguments
				continue;
			}
			Class<?> retype = method.getReturnType();
			if(!retype.isInterface()) {
				// the declared method return type must be an interface
				continue;
			}
			if(uniqueCls.contains(retype)) {
				//TODO: Log error
				throw new CrossbinderException("duplicate methods providing the same type within the same provider");
			}

			Provides ann = method.getAnnotation(Provides.class);
			String name = ann.name().trim();
			binders.add(new ProviderBinder(this, name, method));
			uniqueCls.add(retype);
		}
	}
}
