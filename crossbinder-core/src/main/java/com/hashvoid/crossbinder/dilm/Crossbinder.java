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

package com.hashvoid.crossbinder.dilm;

import java.util.logging.Logger;

/**
 * This class forms the entry point to the Crossbinder container for singletons,
 * prototypes and providers.
 *
 * @author poroshuram
 */

public abstract class Crossbinder {

	private static final Logger LOGGER = Logger.getLogger(Crossbinder.class.getName());

	protected Crossbinder() {
		//NOOP
	}

	public static Crossbinder create() throws CrossbinderException {
		LOGGER.info("Crossbinder: Lightweight Dependency Injection and Lifecycle Management");

		String clsName = Crossbinder.class.getPackage().getName() + ".support.CrossbinderImpl";
		LOGGER.fine(String.format("crossbinder_engine = %s", clsName));

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) {
			cl = ClassLoader.getSystemClassLoader();
		}

		try {
			return (Crossbinder) cl.loadClass(clsName).newInstance();
		}
		catch (ClassNotFoundException exep) {
			throw new CrossbinderException("crossbinder implementation class not found " + clsName, exep);
		}
		catch (InstantiationException | IllegalAccessException exep) {
			throw new CrossbinderException("unable to create crossbinder instance " + clsName, exep);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods to be implemented in concrete classes

	public abstract Crossbinder configure(ConfigurationProvider provider);

	public abstract Crossbinder addInterceptor(MethodInterceptor mi);

	public abstract Crossbinder addInterceptor(LifecycleInterceptor li);

	public abstract Crossbinder start() throws CrossbinderException;

	public abstract void stop() throws CrossbinderException;

	public abstract boolean isStarted();

	public abstract Scanner scanner();

/**
 * Retrieves the injector facet that is associated with this Crossbinder. The injector is used to
 * populate external objects with entities managed by this Crossbinder.
 * <p>
 *
 * @return	the injector facet for this Crossbinder.
 */

	public abstract Injector injector();

/**
 * Retrieves the locator facet that is associated with this Crossbinder. The locator is used to
 * access entities managed by this Crossbinder from an external scope.
 * <p>
 *
 * @return	the locator facet for this Crossbinder.
 */

	public abstract Locator locator();

	public abstract void addLocator(Locator locator);
}
