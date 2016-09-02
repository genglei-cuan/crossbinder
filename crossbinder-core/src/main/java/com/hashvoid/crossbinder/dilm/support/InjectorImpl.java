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

package com.hashvoid.crossbinder.dilm.support;

import java.util.List;

import com.hashvoid.crossbinder.dilm.ConfigurationProvider;
import com.hashvoid.crossbinder.dilm.Injector;
import com.hashvoid.crossbinder.dilm.Locator;
import com.hashvoid.crossbinder.dilm.support.binder.types.ConfigProcessor;
import com.hashvoid.crossbinder.dilm.support.binder.types.InjectProcessor;

/**
 * @author poroshuram
 *
 */

public class InjectorImpl implements Injector {

	private Locator         locator;
	private InjectProcessor injectProc;
	private ConfigProcessor configProc;

	InjectorImpl(Locator locator, List<ConfigurationProvider> providers) {
		this.locator = locator;
		configProc = new ConfigProcessor(providers);
		injectProc = new InjectProcessor();
	}

	@Override
	public void inject(Object target) {
		configProc.configure(target);
		injectProc.injectDependencies(target, locator);
	}
}
