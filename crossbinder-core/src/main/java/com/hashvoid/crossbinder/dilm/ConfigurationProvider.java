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
 * Interface to be implemented by any object that wish to serve as a source of configuration
 * information at runtime. Configuration providers are separately instantiated during application
 * startup, and associated with a Crossbinder instance via the
 * {@link Crossbinder#configure(ConfigurationProvider)} method.
 *
 * @author poroshuram
 */

public interface ConfigurationProvider {

/**
 * Checks to see if configuration information is available with this provider for a given key name.
 *
 * @param	path the key part of the configuration tuple.
 * @return	<tt>true</tt> if a value is available for the key name, <tt>false</tt> otherwise.
 */

	boolean contains(String path);

/**
 * Retrieves the configuration information that is available against a given key name.
 *
 * @param	path the key part of the configuration tuple.
 * @param	type the runtime type of the configuration value.
 * @return	the configuration value. Can be <tt>null</tt> if a value is not available against the
 * 			given key name of the specified runtime type.
 */

	Object getValue(String path, Class<?> type);
}
