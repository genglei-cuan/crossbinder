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
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.ConfigurationProvider;
import com.hashvoid.crossbinder.dilm.Injector;
import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.Locator;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.BinderContext;
import com.hashvoid.crossbinder.dilm.support.binder.EventHandler;
import com.hashvoid.crossbinder.dilm.support.binder.types.InterceptorBinderFactory;
import com.hashvoid.crossbinder.dilm.support.binder.types.PrototypeBinderFactory;
import com.hashvoid.crossbinder.dilm.support.binder.types.ProviderBinderFactory;
import com.hashvoid.crossbinder.dilm.support.binder.types.SingletonBinderFactory;

/**
 * @author poroshuram
 *
 */

public class BinderCreator {

	private static final Logger LOGGER = Logger.getLogger(BinderCreator.class.getName());

	private Injector                    injector;
	private LocatorImpl                 locator;
	private EventHandlerImpl            evtHandler;
	private ScannerImpl                 scanner;
	private List<ConfigurationProvider> configProviders;

	public void setLocator(LocatorImpl locator) {
		this.locator = locator;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public void setEventHandler(EventHandlerImpl handler) {
		evtHandler = handler;
	}

	public void setScanner(ScannerImpl scanner) {
		this.scanner = scanner;
	}

	public void setConfigurationProviders(List<ConfigurationProvider> providers) {
		configProviders = providers;
	}

	public void addInterceptor(MethodInterceptor mi) {
		InterceptorBinderFactory iceptBndf = new InterceptorBinderFactory();
		Binder binder = iceptBndf.createExternalBinder(mi);
		if(binder != null) {
			locator.addMethodInterceptor(binder);
			LOGGER.fine(String.format("method_interceptor = %s", mi.getClass().getName()));
		}
	}

	public void addInterceptor(LifecycleInterceptor li) {
		InterceptorBinderFactory iceptBndf = new InterceptorBinderFactory();
		Binder binder = iceptBndf.createExternalBinder(li);
		if(binder != null) {
			locator.addLifecycleInterceptor(binder);
			LOGGER.fine(String.format("lifecycle_interceptor = %s", li.getClass().getName()));
		}
	}

	public void loadScanned() {

		BinderContext binderCtxt = new BinderContextImpl();

		InterceptorBinderFactory iceptBndf = new InterceptorBinderFactory();
		List<Class<?>> glciList = scanner.listGlobalLifecycleInterceptors();
		for(Class<?> lciType : glciList) {
			Binder binder = iceptBndf.createBinder(lciType, binderCtxt);
			if(binder != null) {
				locator.addGlobalLifecycleInterceptor(binder);
				LOGGER.fine(String.format("global_lifecycle_interceptor = %s", lciType.getName()));
			}
		}

		List<Class<?>> lciList = scanner.listLifecycleInterceptors();
		for(Class<?> lciType : lciList) {
			Binder binder = iceptBndf.createBinder(lciType, binderCtxt);
			if(binder != null) {
				locator.addLifecycleInterceptor(binder);
				LOGGER.fine(String.format("lifecycle_interceptor = %s", lciType.getName()));
			}
		}

		List<Class<?>> miList = scanner.listMethodInterceptors();
		for(Class<?> miType : miList) {
			Binder binder = iceptBndf.createBinder(miType, binderCtxt);
			if(binder != null) {
				locator.addMethodInterceptor(binder);
				LOGGER.fine(String.format("method_interceptor = %s", miType.getName()));
			}
		}

		ProviderBinderFactory provBndf = new ProviderBinderFactory();
		List<Class<?>> provList = scanner.listProviders();
		for(Class<?> provType : provList) {
			List<Binder> binders = provBndf.createBinders(provType, binderCtxt);
			for(Binder binder : binders) {
				locator.addProvider(binder);
			}
		}

		SingletonBinderFactory singleBndf = new SingletonBinderFactory();
		List<Class<?>> singleList = scanner.listSingletons();
		for(Class<?> singleType : singleList) {
			Binder binder = singleBndf.createBinder(singleType, binderCtxt);
			if(binder != null) {
				locator.addSingletonOrPrototype(binder);
				LOGGER.fine(String.format("singleton = %s", singleType.getName()));
			}
		}

		PrototypeBinderFactory protoBndf = new PrototypeBinderFactory();
		List<Class<?>> protoList = scanner.listPrototypes();
		for(Class<?> protoType : protoList) {
			Binder binder = protoBndf.createBinder(protoType, binderCtxt);
			if(binder != null) {
				locator.addSingletonOrPrototype(binder);
				LOGGER.fine(String.format("prototype = %s", protoType.getName()));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Inner class that provides a minimal binder context implementation

	private class BinderContextImpl implements BinderContext {

		@Override
		public Locator getLocator() {
			return locator;
		}

		@Override
		public Injector getInjector() {
			return injector;
		}

		@Override
		public EventHandler getEventHandler() {
			return evtHandler;
		}

		@Override
		public List<ConfigurationProvider> getConfigurationProviders() {
			return configProviders;
		}
	}
}
