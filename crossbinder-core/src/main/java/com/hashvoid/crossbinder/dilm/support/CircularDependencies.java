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

package com.hashvoid.crossbinder.dilm.support;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class CircularDependencies {

	private static final Logger LOGGER = Logger.getLogger(CircularDependencies.class.getName());

	private LocatorImpl locator;

	public void setLocator(LocatorImpl locator) {
		this.locator = locator;
	}

	public void check() {
		LinkedList<Binder> allBinders = new LinkedList<>();
		allBinders.addAll(locator.getLifecycleInterceptors());
		allBinders.addAll(locator.getMethodInterceptors());
		allBinders.addAll(locator.getSingletonsPrototypes());
		//TODO: add providers to this list.

		/*
		LinkedList<Binder> binderChain = new LinkedList<>();
		for(Binder binder : allBinders) {
			Set<Dependency> dependencies = binder.getDependencies();
			for(Dependency dep : dependencies) {
				binderChain.clear();
				binderChain.add(binder);
				checkCircularDependency(binderChain, dep);
			}
		}
		*/

		for(Binder binder : allBinders) {
			Set<Binder> binderSet = new HashSet<>();
			Set<Dependency> dependencies = binder.getDependencies();
			for(Dependency dep : dependencies) {
				loadDependencyTree(binderSet, dep);
			}
			if(binderSet.contains(binder)) {
				LOGGER.severe(String.format("circular_dependency starting {%s}", binder));
				throw new CrossbinderException("circular dependency. see log for details");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void loadDependencyTree(Set<Binder> binderSet, Dependency dep) {
		String binderName = dep.getName();
		Class<?> binderType = dep.getType();
		Binder depBinder = null;
		if(binderName == null || binderName.length() == 0) {
			depBinder = locator.getBinder(binderType);
		}
		else {
			depBinder = locator.getBinder(binderName, binderType);
		}
		if(depBinder == null) {
			if(dep.isRequired() && !locator.fromChainedLocator(binderType)) {
				LOGGER.severe(String.format("unresolved_dependency for {%s}", dep));
				throw new CrossbinderException("unresolve dependency. see log for details");
			}
			else {
				return;
			}
		}
		if(binderSet.add(depBinder)) {
			Set<Dependency> dependencies = depBinder.getDependencies();
			for(Dependency dep1 : dependencies) {
				loadDependencyTree(binderSet, dep1);
			}
		}
	}

	private void checkCircularDependency(List<Binder> binderPath, Dependency dep) {
		String binderName = dep.getName();
		Class<?> binderType = dep.getType();
		Binder depBinder = null;
		if(binderName == null || binderName.length() == 0) {
			depBinder = locator.getBinder(binderType);
		}
		else {
			depBinder = locator.getBinder(binderName, binderType);
		}
		if(depBinder == null) {
			if(dep.isRequired() && !locator.fromChainedLocator(binderType)) {
				LOGGER.severe(String.format("unresolved_dependency = {%s} on {%s}",
						dep, binderPath.get(binderPath.size() - 1)));
				throw new CrossbinderException("unresolve dependency. see log for details");
			}
			else {
				return;
			}
		}
		if(binderPath.contains(depBinder)) {
			LOGGER.severe(String.format("circular_dependency = {%s} starting_from = {%s}",
					depBinder, binderPath.get(0)));
			throw new CrossbinderException("circular dependency. see log for details");
		}
		else {
			binderPath.add(depBinder);
		}
		Set<Dependency> dependencies = depBinder.getDependencies();
		for(Dependency dep1 : dependencies) {
			checkCircularDependency(binderPath, dep1);
		}
	}
}
