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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.Inject;
import com.hashvoid.crossbinder.dilm.Locator;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class InjectProcessor {

	private static final Logger LOGGER = Logger.getLogger(InjectProcessor.class.getName());

	public Set<Dependency> extractDependencies(Class<?> targetCls) {
		Set<Dependency> dependencies = new HashSet<>();
		collectFieldDependencies(targetCls, dependencies);
		collectMethodDependencies(targetCls, dependencies);
		return dependencies;
	}

	public void injectDependencies(Object target, Locator locator) {
		injectFields(target.getClass(), target, locator);
		injectMethods(target.getClass(), target, locator);
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void collectFieldDependencies(Class<?> targetCls, Set<Dependency> dependencies) {
		Field[] fields = targetCls.getDeclaredFields();
		for(Field field : fields) {
			Inject ann = field.getAnnotation(Inject.class);
			if(ann == null) {
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())) {
				// no injection allowed on static fields.
				LOGGER.warning(String.format("fqcn = %s, field = %s (skip_injection: static field)",
						targetCls.getName(), field.getName()));
				continue;
			}
			Class<?> fieldType = field.getType();
			if(!fieldType.isInterface()) {
				// injection only allowed on fields of interface types.
				LOGGER.warning(String.format("fqcn = %s, field = %s (skip_injection: not interface type)",
						targetCls.getName(), field.getName()));
				continue;
			}
			String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();
			Dependency dep = new Dependency(name, fieldType, !ann.optional());
			dependencies.add(dep);
		}

		Class<?> superCls = targetCls.getSuperclass();
		if(superCls != null) {
			collectFieldDependencies(superCls, dependencies);
		}
	}

	private void collectMethodDependencies(Class<?> targetCls, Set<Dependency> dependencies) {
		Method[] methods = targetCls.getMethods();
		for(Method method : methods) {
			collectMethodDependency(method, dependencies);
		}
	}

	private void collectMethodDependency(Method method, Set<Dependency> dependencies) {
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

		ArrayList<Dependency> subDeps = new ArrayList<>();
		for(Parameter param : params) {
			if(!param.getType().isInterface()) {
				return;
			}
			Inject ann = param.getAnnotation(Inject.class);
			if(ann == null) {
				return;
			}
			String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();
			Dependency dep = new Dependency(name, param.getType(), !ann.optional());
			subDeps.add(dep);
		}
		if(subDeps.size() != params.length) {
			return; //useless and redundant check? why take the risk.
		}
		dependencies.addAll(subDeps);
	}

	private void injectFields(Class<?> targetCls, Object target, Locator locator) {
		Field[] fields = targetCls.getDeclaredFields();
		for(Field field : fields) {
			Inject ann = field.getAnnotation(Inject.class);
			if(ann == null) {
				continue;
			}
			if(Modifier.isStatic(field.getModifiers())) {
				// no injection allowed on static fields.
				LOGGER.warning(String.format("fqcn = %s, field = %s (skip_injection: static field)",
						targetCls.getName(), field.getName()));
				continue;
			}
			Class<?> fieldType = field.getType();
			if(!fieldType.isInterface()) {
				// injection only allowed on fields of interface types.
				LOGGER.warning(String.format("fqcn = %s, field = %s (skip_injection: not interface type)",
						targetCls.getName(), field.getName()));
				continue;
			}
			String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();
			Object fieldValue = null;
			if(name.length() == 0) {
				fieldValue = locator.get(fieldType);
			}
			else {
				fieldValue = locator.get(name, fieldType);
			}
			if(fieldValue == null && !ann.optional()) {
				throw new CrossbinderException("unresolved dependency for field "
						+ field.getDeclaringClass().getName() + "#" + field.getName()
						+ ": target not found");
			}
			if(fieldValue != null) {
				boolean accessible = field.isAccessible();
				if(!accessible) {
					field.setAccessible(true);
				}
				try {
					field.set(target, fieldValue);
				}
				catch (IllegalArgumentException | IllegalAccessException exep) {
					throw new CrossbinderException("unresolved dependency for field "
							+ field.getDeclaringClass().getName() + "#" + field.getName()
							+ ": unable to set value", exep);
				}
				if(!accessible) {
					field.setAccessible(false);
				}
			}
		}

		Class<?> superCls = targetCls.getSuperclass();
		if(superCls != null) {
			injectFields(superCls, target, locator);
		}
	}

	private void injectMethods(Class<?> targetCls, Object target, Locator locator) {
		Method[] methods = targetCls.getMethods();
		for(Method method : methods) {
			injectMethod(method, target, locator);
		}
	}

	private void injectMethod(Method method, Object target, Locator locator) {
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
		for(int i=0; i<params.length; i++) {
			if(!params[i].getType().isInterface()) {
				return;
			}
			Inject ann = params[i].getAnnotation(Inject.class);
			if(ann == null) {
				return;
			}
			String name = ann.name().trim().isEmpty() ? ann.value().trim() : ann.name().trim();

			Object paramValue = null;
			if(name.length() == 0) {
				paramValue = locator.get(params[i].getType());
			}
			else {
				paramValue = locator.get(name, params[i].getType());
			}

			if(paramValue == null && !ann.optional()) {
				throw new CrossbinderException("unresolved dependency for method parameter " + method.getName()
						+ "->" + params[i].getName() + ": target not found");
			}

			paramValues[i] = paramValue;
		}

		try {
			method.invoke(target, paramValues);
		}
		catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException exep) {
			throw new CrossbinderException("unable to execute method for dependency injection "
				+ method.getDeclaringClass().getName() + ":" + method.getName(), exep);
		}
	}
}
