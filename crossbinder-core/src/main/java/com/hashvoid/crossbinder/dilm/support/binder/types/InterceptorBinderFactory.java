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

import java.util.HashMap;
import java.util.Map;

import com.hashvoid.crossbinder.dilm.Prototype;
import com.hashvoid.crossbinder.dilm.Provider;
import com.hashvoid.crossbinder.dilm.Singleton;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;

/**
 * @author poroshuram
 *
 */

public class InterceptorBinderFactory {

	private Map<Class<?>, Binder> interceptors;

	public InterceptorBinderFactory() {
		interceptors = new HashMap<>();
	}

	public Binder createBinder(Class<?> implCls, BinderContext ctxt) {
		if(!BinderValidations.checkInstantiable(implCls)) {
			return null;
		}

		if(!BinderValidations.checkNotAnnotatedWith(implCls, Provider.class, Singleton.class, Prototype.class)) {
			return null;
		}

		Binder oldBinder = interceptors.get(implCls);
		if(oldBinder != null) {
			return oldBinder;
		}
		InterceptorBinder binder = new InterceptorBinder(implCls, ctxt);
		interceptors.put(implCls, binder);
		return binder;
	}

	public Binder createExternalBinder(Object external) {
		if(!BinderValidations.checkNotAnnotatedWith(external.getClass(),
				Provider.class, Singleton.class, Prototype.class)) {
			return null;
		}

		Binder oldBinder = interceptors.get(external.getClass());
		if(oldBinder != null) {
			return oldBinder;
		}
		ExtInterceptorBinder binder = new ExtInterceptorBinder(external);
		interceptors.put(external.getClass(), binder);
		return binder;
	}
}
