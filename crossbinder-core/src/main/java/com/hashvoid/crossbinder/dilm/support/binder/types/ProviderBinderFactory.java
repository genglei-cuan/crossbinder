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

import java.util.List;

import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.Prototype;
import com.hashvoid.crossbinder.dilm.Singleton;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;

/**
 * @author poroshuram
 *
 */

public class ProviderBinderFactory {

	public List<Binder> createBinders(Class<?> implCls, BinderContext ctxt) {
		if(!BinderValidations.checkInstantiable(implCls)) {
			return null;
		}

		if(!BinderValidations.checkNotAnnotatedWith(implCls, Singleton.class, Prototype.class)) {
			return null;
		}

		if(!BinderValidations.checkNotSubtypeOf(implCls, LifecycleInterceptor.class, MethodInterceptor.class)) {
			return null;
		}

		return new ProviderBinderGroup(implCls, ctxt).getBinders();
	}
}
