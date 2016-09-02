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

import java.util.List;
import java.util.Map;

/**
 * This is a Crossbinder facet that is used to lookup a managed entity that is available within the
 * scope of the corresponding Crossbinder. Use this facet to access singletons, prototypes and
 * provided entities from outside the container scope of Crossbinder.
 *
 * @author poroshuram
 */

public interface Locator {

/**
 * Retrieves a managed entity of a given runtime type that is available within the corresponding
 * Crossbinder scope.
 *
 * @param	type the runtime type of the entity being accessed.
 * @param	<T> parametrized form of the entity type being acessed.
 * @return	an entity managed by Crossbinder, or <tt>null</tt> if the entity does not exist.
 */

	<T> T get(Class<T> type);

/**
 * Retrieves a named, managed entity of a given runtime type that is available within the
 * corresponding Crossbinder scope.
 *
 * @param	name the name of the managed entity.
 * @param	type the runtime type of the entity being accessed.
 * @param	<T> parametrized form of the entity type being acessed.
 * @return	an entity managed by Crossbinder, or <tt>null</tt> if the entity does not exist.
 */

	<T> T get(String name, Class<T> type);

/**
 * Retrieves all entities of a given runtime type that are available within the corresponding
 * Crossbinder scope.
 *
 * @param	type the runtime type of the entities being retrieved.
 * @param	<T> parametrized form of the entity type being acessed.
 * @return	a collection of entities managed by Crossbinder.
 */

	<T> List<T> getAll(Class<T> type);

/**
 * Retrieves all named entities of a given runtime type that are available within the corresponding
 * Crossbinder scope.
 *
 * @param	type the runtime type of the entities being retrieved.
 * @param	<T> parametrized form of the entity type being acessed.
 * @return	a key-value map of the entities. For each entry, the key is the name of the entity and
 *			value is the entity itself.
 */

	<T> Map<String, T> getAllNamed(Class<T> type);
}
