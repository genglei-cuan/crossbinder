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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.hashvoid.crossbinder.dilm.CrossbinderException;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.Dependency;

/**
 * @author poroshuram
 *
 */

public class BinderOrdering {

	private LocatorImpl        locator;
	private LinkedList<Binder> stage1Binders;
	private LinkedList<Binder> stage2Binders;

	public BinderOrdering() {
		stage1Binders = new LinkedList<>();
		stage2Binders = new LinkedList<>();
	}

	public void setLocator(LocatorImpl locator) {
		this.locator = locator;
	}

	public void resolve() {
		List<Binder> globalBinders = new ArrayList<>();
		globalBinders.addAll(locator.getLifecycleInterceptors());
		globalBinders.addAll(locator.getMethodInterceptors());
		globalBinders.addAll(locator.getProviders());
		globalBinders.addAll(locator.getExternals());
		for(Binder binder : globalBinders) {
			if(stage1Binders.contains(binder)) {
				//skip
				continue;
			}
			stage1Binders.add(binder);
			Set<Dependency> dependencies = binder.getDependencies();
			for(Dependency dep : dependencies) {
				resolveStage1Dependencies(binder, dep);
			}
		}

		Collection<Binder> spBinders = locator.getSingletonsPrototypes();
		for(Binder binder : spBinders) {
			if(stage1Binders.contains(binder) || stage2Binders.contains(binder)) {
				//skip
				continue;
			}
			stage2Binders.add(binder);
			Set<Dependency> dependencies = binder.getDependencies();
			for(Dependency dep : dependencies) {
				resolveStage2Dependencies(binder, dep);
			}
		}
	}

	public List<Binder> getStage1Binders() {
		return stage1Binders;
	}

	public List<Binder> getStage2Binders() {
		return stage2Binders;
	}

	////////////////////////////////////////////////////////////////////////////
	// Helper methods

	private void resolveStage1Dependencies(Binder owner, Dependency dep) {
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
				//TODO: Log error
				throw new CrossbinderException("cannot resolve dependency: on ...");
			}
			else {
				return;
			}
		}

		stage1Binders.remove(depBinder);
		stage1Binders.addFirst(depBinder);
		Set<Dependency> dependencies = depBinder.getDependencies();
		for(Dependency dep1 : dependencies) {
			resolveStage1Dependencies(depBinder, dep1);
		}
	}

	private void resolveStage2Dependencies(Binder owner, Dependency dep) {
		String binderName = dep.getName();
		Class<?> binderType = dep.getType();
		Binder depBinder = null;
		if(binderName == null || binderName.length() == 0) {
			depBinder = locator.getBinder(binderType);
		}
		else {
			depBinder = locator.getBinder(binderName, binderType);
		}
		if(stage1Binders.contains(depBinder)) {
			return;
		}
		if(depBinder == null) {
			if(dep.isRequired() && !locator.fromChainedLocator(binderType)) {
				//TODO: Log error
				throw new CrossbinderException("cannot resolve dependency: on ...");
			}
			else {
				return;
			}
		}

		stage2Binders.remove(depBinder);
		stage2Binders.addFirst(depBinder);
		Set<Dependency> dependencies = depBinder.getDependencies();
		for(Dependency dep1 : dependencies) {
			resolveStage2Dependencies(depBinder, dep1);
		}
	}
}
