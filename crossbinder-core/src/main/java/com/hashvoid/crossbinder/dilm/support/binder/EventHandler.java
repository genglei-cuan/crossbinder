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

package com.hashvoid.crossbinder.dilm.support.binder;

import java.lang.reflect.Method;

/**
 * @author poroshuram
 *
 */

public interface EventHandler {

	void instanceCreated(Object inst);

	void instanceInjected(Object inst);

	void instanceInitialized(Object inst);

	void instanceDisposing(Object inst);

	void instanceDisposed(Object inst);

	void beforeMethod(Object inst, Method method, Object[] args);

	Object wrapMethod(Object inst, Method method, Object[] args) throws Throwable;

	void afterMethodSuccess(Object inst, Method method, Object result);

	void afterMethodFail(Object inst, Method method, Throwable error);
}
