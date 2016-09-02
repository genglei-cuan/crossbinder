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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.Locator;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;

/**
 * @author poroshuram
 *
 */

public class LocatorImpl implements Locator {

	private static final Logger LOGGER = Logger.getLogger(LocatorImpl.class.getName());

	private Set<Locator> chainedLocators;
	private Set<Binder>  globalInterceptors;
	private Set<Binder>  lifecycleInterceptors;
	private Set<Binder>  methodInterceptors;
	private Set<Binder>  providers;
	private Set<Binder>  externals;
	private Set<Binder>  singleProto;

	private Map<Class<?>, Set<Binder>> typeToBinderMap;
	private Map<String, Binder>        nameToBinderMap;

	LocatorImpl() {
		chainedLocators = new HashSet<>();
		globalInterceptors = new HashSet<>();
		lifecycleInterceptors = new HashSet<>();
		methodInterceptors = new HashSet<>();
		providers = new HashSet<>();
		externals = new HashSet<>();
		singleProto = new HashSet<>();
		typeToBinderMap = new HashMap<>();
		nameToBinderMap = new HashMap<>();
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface Locator

	@Override
	public <T> T get(Class<T> type) {
		if(!type.isInterface()) {
			throw new CrossbinderException("target type must be an interface : " + type.getName());
		}
		if(!typeToBinderMap.containsKey(type)) {
			LOGGER.fine(String.format("entity not found for type = %s", type.getName()));
			return getChained(type);
		}
		Set<Binder> binders = typeToBinderMap.get(type);
		if(binders.size() != 1) {
			LOGGER.fine(String.format("multiple binders available for type = %s", type));
			return null;
		}
		return binders.iterator().next().getInstance(type);
	}

	@Override
	public <T> T get(String name, Class<T> type) {
		if(!type.isInterface()) {
			throw new CrossbinderException("target type must be an interface : [" + name + "] " + type.getName());
		}
		Binder binder = nameToBinderMap.get(name);
		if(binder == null) {
			return getChained(name, type);
		}
		if(binder.getInterfaceTypes().contains(type)) {
			return binder.getInstance(type);
		}
		return getChained(name, type);
	}

	@Override
	public <T> List<T> getAll(Class<T> type) {
		if(!type.isInterface()) {
			throw new CrossbinderException("target type must be an interface : " + type.getName());
		}
		ArrayList<T> result = new ArrayList<>();
		if(typeToBinderMap.containsKey(type)) {
			Set<Binder> binders = typeToBinderMap.get(type);
			for(Binder binder : binders) {
				result.add(binder.getInstance(type));
			}
		}
		result.addAll(getAllChained(type));
		return result;
	}

	@Override
	public <T> Map<String, T> getAllNamed(Class<T> type) {
		Map<String, T> result = getAllNamedChained(type);
		if(!typeToBinderMap.containsKey(type)) {
			return result;
		}
		Set<Binder> binders = typeToBinderMap.get(type);
		for(Binder binder : binders) {
			if(binder.getName().length() > 0) {
				result.put(binder.getName(), binder.getInstance(type));
			}
		}
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	// Package Private Methods

	void dumpState() {
		System.out.println(typeToBinderMap);
	}

	void chain(Locator locator) {
		chainedLocators.add(locator);
	}

	void addGlobalLifecycleInterceptor(Binder binder) {
		if(!globalInterceptors.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate global lifecycle interceptor %s", binder));
		}
	}

	void addLifecycleInterceptor(Binder binder) {
		if(!lifecycleInterceptors.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate lifecycle interceptor %s", binder));
		}
	}

	void addMethodInterceptor(Binder binder) {
		if(!methodInterceptors.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate method interceptor %s", binder));
		}
	}

	void addProvider(Binder binder) {
		if(binder.getName().length() > 0 && nameToBinderMap.containsKey(binder.getName())) {
			LOGGER.warning(String.format("a binder already exists with the name = %s", binder.getName()));
			return;
		}
		if(!providers.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate provider %s", binder));
			return;
		}
		if(binder.getName().length() > 0) {
			nameToBinderMap.put(binder.getName(), binder);
		}
		for(Class<?> type : binder.getInterfaceTypes()) {
			if(typeToBinderMap.containsKey(type)) {
				typeToBinderMap.get(type).add(binder);
			}
			else {
				Set<Binder> binders = new HashSet<>();
				binders.add(binder);
				typeToBinderMap.put(type, binders);
			}
		}
	}

	void addExternal(Binder binder) {
		if(binder.getName().length() > 0 && nameToBinderMap.containsKey(binder.getName())) {
			LOGGER.warning(String.format("a binder already exists with the name = %s", binder.getName()));
			return;
		}
		if(!externals.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate external %s", binder));
			return;
		}
		if(binder.getName().length() > 0) {
			nameToBinderMap.put(binder.getName(), binder);
		}
		for(Class<?> type : binder.getInterfaceTypes()) {
			if(typeToBinderMap.containsKey(type)) {
				typeToBinderMap.get(type).add(binder);
			}
			else {
				Set<Binder> binders = new HashSet<>();
				binders.add(binder);
				typeToBinderMap.put(type, binders);
			}
		}
	}

	void addSingletonOrPrototype(Binder binder) {
		if(binder.getName().length() > 0 && nameToBinderMap.containsKey(binder.getName())) {
			LOGGER.warning(String.format("a binder already exists with the name = %s", binder.getName()));
			return;
		}
		if(!singleProto.add(binder)) {
			LOGGER.warning(String.format("attempt to add duplicate singleton/prototype %s", binder));
			return;
		}
		if(binder.getName().length() > 0) {
			nameToBinderMap.put(binder.getName(), binder);
		}
		for(Class<?> type : binder.getInterfaceTypes()) {
			if(typeToBinderMap.containsKey(type)) {
				typeToBinderMap.get(type).add(binder);
			}
			else {
				Set<Binder> binders = new HashSet<>();
				binders.add(binder);
				typeToBinderMap.put(type, binders);
			}
		}
	}

	Set<Binder> getGlobalLifecycleInterceptors() {
		return globalInterceptors;
	}

	Set<Binder> getLifecycleInterceptors() {
		return lifecycleInterceptors;
	}

	Set<Binder> getMethodInterceptors() {
		return methodInterceptors;
	}

	Set<Binder> getProviders() {
		return providers;
	}

	Set<Binder> getExternals() {
		return externals;
	}

	Set<Binder> getSingletonsPrototypes() {
		return singleProto;
	}

	boolean fromChainedLocator(Class<?> type) {
		return getChained(type) != null;
	}

	boolean fromChainedLocator(String name, Class<?> type) {
		return getChained(name, type) != null;
	}

	Binder getBinder(Class<?> type) {
		if(!typeToBinderMap.containsKey(type)) {
			return null;
		}
		Set<Binder> binders = typeToBinderMap.get(type);
		if(binders.size() != 1) {
			LOGGER.warning(String.format("multiple binders available for type = %s", type));
			return null;
		}
		return binders.iterator().next();
	}

	Binder getBinder(String name, Class<?> type) {
		Binder binder = nameToBinderMap.get(name);
		if(binder == null) {
			return null;
		}
		if(binder.getInterfaceTypes().contains(type)) {
			return binder;
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private <T> T getChained(Class<T> type) {
		for(Locator locator : chainedLocators) {
			T result = locator.get(type);
			if(result != null) {
				return result;
			}
		}
		return null;
	}

	private <T> T getChained(String name, Class<T> type) {
		for(Locator locator : chainedLocators) {
			T result = locator.get(name, type);
			if(result != null) {
				return result;
			}
		}
		return null;
	}

	private <T> List<T> getAllChained(Class<T> type) {
		List<T> result = new LinkedList<>();
		for(Locator locator : chainedLocators) {
			result.addAll(locator.getAll(type));
		}
		return result;
	}

	private <T> Map<String, T> getAllNamedChained(Class<T> type) {
		Map<String, T> result = new HashMap<>();
		for(Locator locator : chainedLocators) {
			result.putAll(locator.getAllNamed(type));
		}
		return result;
	}
}
