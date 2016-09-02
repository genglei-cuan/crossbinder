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
 * Interface to be implemented by singletons that need to react at the time of being released by
 * Crossbinder and therefore becoming eligible for garbage collection.
 *
 * @author poroshuram
 */

@NonBindable
public interface Disposable {

/**
 * Invoked by the containing Crossbinder on the instance of this class that is being released from
 * internal cache.
 */

	void dispose();
}
