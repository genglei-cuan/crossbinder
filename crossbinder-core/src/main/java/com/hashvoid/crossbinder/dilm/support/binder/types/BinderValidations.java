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

package com.hashvoid.crossbinder.dilm.support.binder.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

/**
 * @author poroshuram
 *
 */

final class BinderValidations {

	private static final Logger LOGGER = Logger.getLogger(BinderValidations.class.getName());

	private BinderValidations() {
		//NOOP
	}

	public static boolean checkInstantiable(Class<?> implCls) {
		if(implCls.isAnnotation()) {
			LOGGER.warning(String.format("fqcn = %s (skip: is annotation)", implCls.getName()));
			return false;
		}
		if(implCls.isInterface()) {
			LOGGER.warning(String.format("fqcn = %s (skip: is interface)", implCls.getName()));
			return false;
		}
		int mods = implCls.getModifiers();
		if(Modifier.isAbstract(mods)) {
			LOGGER.warning(String.format("fqcn = %s (skip: is abstract)", implCls.getName()));
			return false;
		}
		return true;
	}

	@SafeVarargs
	public static boolean checkNotAnnotatedWith(Class<?> implCls, Class<? extends Annotation>... annotations) {
		for(Class<? extends Annotation> ann : annotations) {
			if(implCls.getAnnotation(ann) != null) {
				LOGGER.warning(String.format("fqcn = %s (skip: conflicting annotation @%s)",
						implCls.getName(), ann.getSimpleName()));
				return false;
			}
		}
		return true;
	}

	public static boolean checkNotSubtypeOf(Class<?> implCls, Class<?>... types) {
		for(Class<?> type : types) {
			if(type.isAssignableFrom(implCls)) {
				LOGGER.warning(String.format("fqcn = %s (skip: conflicting implementation %s)",
						implCls.getName(), type.getSimpleName()));
				return false;
			}
		}
		return true;
	}
}
