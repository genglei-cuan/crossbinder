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

/**
 * Allows insertion of custom logic at specific points in the overall lifecycle of singletons and
 * protptypes managed by Crossbinder. The intention is to allow for common operations to be
 * performed as and when entities get created and destroyed.
 * <p>
 *
 * Multiple such interceptors (one instance per type) may be active for a given Crossbinder
 * instance. All interceptors get an equal opportunity to execute their logic on a managed entity
 * against a given lifecycle stage. The order in which these interceptors are invoked is determined
 * by Crossbinder. The ordering is fixed for a given Crossbinder instance, but may vary
 * unpredictably across Crossbinder lifecycles.
 * <p>
 *
 * A lifecycle interceptor can be instantiated by Crossbinder after scanning the classpath, in which
 * case it is injected with dependencies and configuration data like any other managed entity. Such
 * interceptors may also be created externally and then associated with a Crossbinder via the
 * {@link Crossbinder#addInterceptor(LifecycleInterceptor)} method.
 *
 * @author poroshuram
 */

public interface LifecycleInterceptor {

/**
 * This method gets invoked after a singleton or prototype has been created by Crossbinder, and
 * prior to any other dependency injection or execution of initialization routines.
 *
 * @param	target the singleton or prototype created by Crossbinder
 */

	void afterCreation(Object target);

/**
 * This method gets invoked after a singleton or prototype has been created and injected with all
 * available dependencies by Crossbinder.
 *
 * @param	target the singleton or prototype managed by Crossbinder
 */

	void afterInjection(Object target);

/**
 * This method gets invoked after a singleton or prototype has been created, injected with all
 * available dependencies and execution of init-method by Crossbinder.
 *
 * @param	target the singleton or prototype managed by Crossbinder
 */

	void afterInitialization(Object target);

/**
 * This method gets invoked at a stage when a singleton is about to be destroyed and removed by
 * Crossbinder. Since a singleton's end-of-life is aligned with that of the containing Crossbinder,
 * this method gets invoked at the time of a clean Crossbinder shutdown.
 *
 * @param	target the singleton managed by Crossbinder
 */

	void beforeDisposal(Object target);

/**
 * This method gets invoked at a stage when a singleton is destroyed and removed from Crossbinder.
 * Since a singleton's end-of-life is aligned with that of the containing Crossbinder, this method
 * gets invoked at the time of a clean Crossbinder shutdown.
 *
 * @param	target the singleton or prototype managed by Crossbinder
 */

	void afterDisposal(Object target);
}
