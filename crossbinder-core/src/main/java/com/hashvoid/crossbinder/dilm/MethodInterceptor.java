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

import java.lang.reflect.Method;

/**
 * Allows insertion of custom logic across invocation of methods on singletons, prototypes and
 * provided entities. Note that these methods must be defined on the interfaces through which these
 * managed entities are being referred from within the calling scope. Invocation of methods may also
 * be wrapped: in such cases, Crossbinder will call the <tt>wrap</tt> method on this interceptor
 * instead of the actual method on the managed entity. It is left to the interceptor to internally
 * invoke the method on the managed entity as it deems fit.
 * <p>
 *
 * Multiple such interceptors (one instance per type) may be active for a given Crossbinder
 * instance. All interceptors get an equal opportunity to execute their logic on a managed entity
 * against a given lifecycle stage. The order in which these interceptors are invoked is determined
 * by Crossbinder. The ordering is fixed for a given Crossbinder instance, but may vary
 * unpredictably across Crossbinder lifecycles.
 * <p>
 *
 * A method interceptor can be instantiated by Crossbinder after scanning the classpath, in which
 * case it is injected with dependencies and configuration data like any other managed entity. Such
 * interceptors may also be created externally and then associated with a Crossbinder via the
 * {@link Crossbinder#addInterceptor(MethodInterceptor)} method.
 *
 * @author poroshuram
 */

public interface MethodInterceptor {

/**
 * Called by Crossbinder prior to every invocation of a method on a singleton, prototype or
 * provided entity. Allows for custom operations before every invocation of methods on managed
 * objects.
 * <p>
 *
 * @param	method the method being invoked on the managed entity.
 * @param	target the managed entity whose method is being invoked.
 * @param	parameters the set of parameter values with which the method is being invoked.
 */

	void before(Method method, Object target, Object[] parameters);

/**
 * Called by Crossbinder after every invocation of a method on a singleton, prototype or provided
 * entity. Allows for custom operations after every invocation of methods on managed objects.
 * <p>
 *
 * @param	method the method being invoked on the managed entity.
 * @param	target the managed entity whose method is being invoked.
 * @param	retVal the value returned by the invoked method.
 */

	void after(Method method, Object target, Object retVal);

/**
 * Checks to see if the actual call to method on the managed entity is wrapped by this interceptor.
 * This check is performed for every invocation of a method on a singleton, prototype or provided
 * entity.
 * <p>
 *
 * @param	method the method being invoked on the managed entity.
 * @param	target the managed entity whose method is being invoked.
 * @return	<tt>true</tt> wich will result in the {@link #wrap(Method, Object, Object[])} method
 * 			being called on this interceptor, instead of the method on the managed entity,
 * 			<tt>false</tt> otherwise.
 */

	boolean isWrapped(Method method, Object target);

/**
 * Called by Crossbinder on this interceptor for each method invocation on the managed entity, if
 * the {@link #isWrapped(Method, Object)} returns <tt>true</tt>. In such cases, the actual methods
 * on the managed entity are never invoked directly from the calling context.
 * <p>
 *
 * @param	method the method being invoked on the managed entity.
 * @param	target the managed entity whose method is being invoked.
 * @param	parameters the set of parameter values with which the method is being invoked.
 * @return	the value to be returned to the calling context. The type of this value must match the
 * 			return type of the wrapped method on the managed entity.
 */

	Object wrap(Method method, Object target, Object[] parameters);

/**
 * Called by Crossbinder if an exception condition is encountered during invocation of a method on
 * a singleton, prototype or provided entity. Allows for custom operations after each exception
 * condition encountered.
 * <p>
 *
 * @param	method the method being invoked on the managed entity.
 * @param	target the managed entity whose method is being invoked.
 * @param	exep the exception encountered during method invocation on the managed entity.
 */

	void onError(Method method, Object target, Throwable exep);
}
