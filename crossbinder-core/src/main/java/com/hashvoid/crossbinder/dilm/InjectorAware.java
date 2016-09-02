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
 * Interface to be implemented by any managed entity that wishes to have a handle to the injector of
 * the CrossBinder where it is deployed.
 *
 * @author poroshuram
 */

@NonBindable
public interface InjectorAware {

/**
 * Set the injector for the CrossBinder containing this entity. Normally this method will be invoked
 * by CrossBinder on the entity as part of the initialization sequence.
 *
 * @param	injector the injector to be used by this entity.
 */

	void setInjector(Injector injector);
}
