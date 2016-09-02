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
 * Identifies the methods within a provider that can be used to obtain instances of specific
 * provided entities. This implies that the annotation is only relevant for methods whose enclosing
 * class type is decorated with the {@link Provider} annotation. The annotated method is invoked by
 * Crossbinder as and when there is a need for provided entities for injection in other singletons
 * and prototypes.
 *
 * @author poroshuram
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Provides {

/**
 * Assigns a name to identify the provided entity. This is necessary in case where multiple methods
 * return provided entities that can be bound to the same interface type for injection and lookup.
 * Names should be unique across <tt>Singleton</tt>, <tt>Prototype</tt> and <tt>Provides</tt>
 * annotations.
 * <p>
 *
 * @return	a unique name for this provided entity.
 */

	String name() default "";

	String scope() default "";
}
