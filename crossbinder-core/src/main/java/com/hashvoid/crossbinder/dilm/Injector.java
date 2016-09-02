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
 * This is a Crossbinder facet that is used to populate any object with managed entities that are
 * available in the corresponding Crossbinder instance. The semantics for injector are identical to
 * the ones that are employed for managed entities. Injectors are used for objects whose
 * lifecycles are not maintained by Crossbinder, but needs to have access to managed entities
 * without making use of service lookups.
 *
 * @author poroshuram
 *
 */

public interface Injector {

/**
 * Injects a given object with singletons, prototypes and provided entities that are managed by the
 * corresponding CrossBinder.
 *
 * @param	target the object to be injected.
 */

	void inject(Object target);
}
