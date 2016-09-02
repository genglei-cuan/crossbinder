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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.Configuration;
import com.hashvoid.crossbinder.dilm.ConfigurationProvider;
import com.hashvoid.crossbinder.dilm.CrossbinderException;

/**
 * @author poroshuram
 *
 */

public class ConfigProcessor {

	private static final Logger LOGGER = Logger.getLogger(ConfigProcessor.class.getName());

	private List<ConfigurationProvider> configProviders;

	public ConfigProcessor(List<ConfigurationProvider> providers) {
		configProviders = providers;
	}

	public void configure(Object target) {
		injectFields(target.getClass(), target);
		injectMethods(target.getClass(), target);
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void injectFields(Class<?> targetCls, Object target) {
		Field[] fields = targetCls.getDeclaredFields();
		for(Field field : fields) {
			injectField(field, targetCls, target);
		}
		Class<?> superCls = targetCls.getSuperclass();
		if(superCls != null) {
			injectFields(superCls, target);
		}
	}

	private void injectField(Field field, Class<?> targetCls, Object target) {
		Configuration ann = field.getAnnotation(Configuration.class);
		if(ann == null) { //field is not annotated
			return;
		}

		if(Modifier.isStatic(field.getModifiers())) {
			// no configuration allowed on static fields.
			LOGGER.warning(String.format("fqcn = %s, field = %s (skip_configuration: static field)",
					targetCls.getName(), field.getName()));
			return;
		}

		String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();
		if(name.length() == 0) {
			if(ann.required()) {
				throw new CrossbinderException("configuration injection failed on field "
						+ target.getClass().getName() + "#" + field.getName());
			}
			LOGGER.warning(String.format("@Configuration on: %s#%s does not have a name",
					field.getDeclaringClass().getName(), field.getName()));
			return;
		}

		Object fieldValue = retrieveConfig(name, field.getType());
		if(fieldValue == null) {
			if(ann.required()) {
				throw new CrossbinderException("configuration injection failed on field "
						+ target.getClass().getName() + "#" + field.getName());
			}
			LOGGER.warning(String.format("configuration %s not found or not of required type", name));
			return;
		}

		boolean accessible = field.isAccessible();
		if(!accessible) {
			field.setAccessible(true);
		}
		try {
			field.set(target, fieldValue);
		}
		catch (IllegalArgumentException | IllegalAccessException exep) {
			if(ann.required()) {
				throw new CrossbinderException("configuration injection failed on field " + field.getName()
						+ ": unable to set value", exep);
			}
			LOGGER.warning(String.format("configuration: %s error injecting on field %s#%s",
					name, field.getDeclaringClass().getName(), field.getName()));
		}
		finally {
			if(!accessible) {
				field.setAccessible(false);
			}
		}
	}

	private void injectMethods(Class<?> targetCls, Object target) {
		Method[] methods = targetCls.getMethods();
		for(Method method : methods) {
			injectMethod(method, target);
		}
	}

	private void injectMethod(Method method, Object target) {
		int mod = method.getModifiers();
		if(Modifier.isAbstract(mod) || Modifier.isStatic(mod)) {
			return;
		}
		if(method.getReturnType() != Void.TYPE) {
			return;
		}
		Parameter[] params = method.getParameters();
		if(params.length == 0) {
			return;
		}

		Object[] paramValues = new Object[params.length];
		for(int i = 0; i < params.length; i++) {
			Configuration ann = params[i].getAnnotation(Configuration.class);
			if(ann == null) {
				return;
			}
			String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();

			Object paramValue = null;
			if(name.length() == 0) {
				if(ann.required()) {
					throw new CrossbinderException("configuration injection failed on method "
							+ target.getClass().getName() + "#" + method.getName());
				}
				LOGGER.warning(String.format("@Configuration on: %s#%s parameter does not have a name",
						target.getClass().getName(), method.getName()));
			}
			else {
				paramValue = retrieveConfig(name, params[i].getType());
			}

			if(paramValue == null && ann.required()) {
				throw new CrossbinderException("unresolved configuration for method parameter " + method.getName()
						+ "->" + params[i].getName() + ": value not found");
			}

			paramValues[i] = paramValue;
		}

		try {
			method.invoke(target, paramValues);
		}
		catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException exep) {
			throw new CrossbinderException("unable to execute method for configuration injection "
				+ method.getDeclaringClass().getName() + ":" + method.getName(), exep);
		}
	}

	private Object retrieveConfig(String name, Class<?> type) {

		for(ConfigurationProvider provider : configProviders) {
			if(!provider.contains(name)) {
				continue;
			}
			Object value = provider.getValue(name, type);
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
