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
 * Interface to be implemented by managed entities that need to react once all their properties have
 * been set by Crossbinder: for example, to perform custom initialization. Such managed entities
 * include singletons, prototypes, providers, lifecycle and method interceptors.
 * <p>
 *
 * As an alternative to implementing this interface, you can also also decorate a method on a
 * managed entity with the {@link Initialize} annotation. This method will be invoked by Crossbinder
 * at the time of entity creation, and after injection of all dependencies and configuration.
 *
 * @author poroshuram
 */

@NonBindable
public interface Initializable {

/**
 * Invoked by the containing Crossbinder after it has injected all dependencies (and satisfied
 * InjectorAware and LocatorAware).
 */

	void initialize();
}
