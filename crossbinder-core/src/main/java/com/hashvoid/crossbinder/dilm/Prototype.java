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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type type must be managed (instantiated, deployed, disposed) by
 * Crossbinder as a prototype entity.
 *
 * @author poroshuram
 */

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Prototype {

/**
 * Assigns a name to identify the prototype. This is necessary in case where multiple prototypes
 * are available that can be bound to the same interface type for injection and lookup. Names should
 * be unique across <tt>Singleton</tt>, <tt>Prototype</tt> and <tt>Provides</tt> annotations.
 *
 * @return	a unique name for this prototype.
 */

	String name() default "";

/**
 * For future use. Just ignore this for now.
 *
 * @return	todo
 */

	String scope() default "";
}
