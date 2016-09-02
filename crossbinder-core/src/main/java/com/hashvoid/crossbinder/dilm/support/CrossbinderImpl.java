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
import java.util.List;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.ConfigurationProvider;
import com.hashvoid.crossbinder.dilm.Crossbinder;
import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.GlobalLifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.Injector;
import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.Locator;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.Scanner;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;

/**
 * @author poroshuram
 *
 */

public class CrossbinderImpl extends Crossbinder {

	private static final Logger LOGGER = Logger.getLogger(CrossbinderImpl.class.getName());

	private ScannerImpl                 scanner;
	private List<ConfigurationProvider> configProviders;
	private LocatorImpl                 locator;
	private InjectorImpl                injector;
	private EventHandlerImpl            evtHandler;
	private BinderCreator               bindCreator;
	private BinderOrdering              bindOrder;
	private boolean                     startFlag;

	public CrossbinderImpl() {
		scanner = new ScannerImpl();
		locator = new LocatorImpl();
		evtHandler = new EventHandlerImpl(locator);
		configProviders = new ArrayList<>();
		injector = new InjectorImpl(locator, configProviders);

		bindCreator = new BinderCreator();
		bindCreator.setScanner(scanner);
		bindCreator.setLocator(locator);
		bindCreator.setInjector(injector);
		bindCreator.setEventHandler(evtHandler);
		bindCreator.setConfigurationProviders(configProviders);

		bindOrder = new BinderOrdering();
		bindOrder.setLocator(locator);
		startFlag = false;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of base class CrossBinder

	@Override
	public Crossbinder configure(ConfigurationProvider provider) {
		configProviders.add(provider);
		return this;
	}

	@Override
	public Crossbinder addInterceptor(MethodInterceptor mi) {
		bindCreator.addInterceptor(mi);
		return this;
	}

	@Override
	public Crossbinder addInterceptor(LifecycleInterceptor li) {
		bindCreator.addInterceptor(li);
		return this;
	}

	@Override
	public Crossbinder start() throws CrossbinderException {
		scanner.scan();
		bindCreator.loadScanned();
		locator.dumpState(); //for debug purposes

		CircularDependencies circDep = new CircularDependencies();
		circDep.setLocator(locator);
		circDep.check();

		bindOrder.resolve();

		for(Binder binder : bindOrder.getStage1Binders()) {
			binder.start();
		}
		LOGGER.fine("stage1 binders started");

		//activate the event handler to start handling events from this point onwards.
		evtHandler.getReady();

		for(Binder binder : bindOrder.getStage2Binders()) {
			binder.start();
		}
		LOGGER.fine("stage2 binders started");

		for(Binder binder : locator.getGlobalLifecycleInterceptors()) {
			GlobalLifecycleInterceptor glci = binder.getInstance(GlobalLifecycleInterceptor.class);
			if(glci != null) {
				glci.afterStart();
			}
		}
		startFlag = true;
		return this;
	}

	@Override
	public void stop() {

		for(Binder binder : locator.getGlobalLifecycleInterceptors()) {
			GlobalLifecycleInterceptor glci = binder.getInstance(GlobalLifecycleInterceptor.class);
			if(glci != null) {
				glci.beforeStop();
			}
		}

		for(Binder binder : bindOrder.getStage2Binders()) {
			binder.stop();
		}

		for(Binder binder : bindOrder.getStage1Binders()) {
			binder.stop();
		}
		startFlag = false;
	}

	@Override
	public boolean isStarted() {
		return startFlag;
	}

	@Override
	public Scanner scanner() {
		return scanner;
	}

	@Override
	public Injector injector() {
		return injector;
	}

	@Override
	public Locator locator() {
		return locator;
	}

	@Override
	public void addLocator(Locator locator) {
		this.locator.chain(locator);
	}
}
