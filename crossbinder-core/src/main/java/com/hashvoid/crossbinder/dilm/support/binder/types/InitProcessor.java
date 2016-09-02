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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.Initializable;
import com.hashvoid.crossbinder.dilm.Initialize;

/**
 * @author poroshuram
 *
 */

public class InitProcessor {

	private static final Logger LOGGER = Logger.getLogger(InitProcessor.class.getName());

	public void resolve(Class<?> targetCls) {
		List<Method> initMethods = findInitMethods(targetCls);
		if(Initializable.class.isAssignableFrom(targetCls) && !initMethods.isEmpty()) {
			LOGGER.warning(String.format("conflict_init = %s (both initializable and has init method)",
					targetCls.getName()));
			throw new CrossbinderException("conflict in initialization. Class is both initializable and has init methods.");
		}
		if(initMethods.size() > 1) {
			LOGGER.warning(String.format("conflict_init = %s (too many init methods)", targetCls.getName()));
			throw new CrossbinderException("conflict in initialization. Too many init methods.");
		}
	}

	public void execute(Object target) {
		if(target instanceof Initializable) {
			((Initializable) target).initialize();
		}
		else {
			List<Method> initMethods = findInitMethods(target.getClass());
			if(!initMethods.isEmpty()) {
				try {
					initMethods.get(0).invoke(target);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exep) {
					throw new CrossbinderException("error executing init method",
							exep.getCause() != null ? exep.getCause() : exep);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private List<Method> findInitMethods(Class<?> cls) {
		Method[] methods = cls.getMethods();
		ArrayList<Method> result = new ArrayList<>(methods.length);
		for(Method method : methods) {
			if(method.getAnnotation(Initialize.class) != null) {
				Class<?>[] paramTypes = method.getParameterTypes();
				if(paramTypes.length == 0 && method.getReturnType() == Void.TYPE) {
					result.add(method);
				}
				else {
					LOGGER.warning(String.format("bad_init_method = %s (should have zero args and no return type)",
							method.getName()));
				}
			}
		}
		return result;
	}
}
