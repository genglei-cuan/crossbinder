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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hashvoid.classpath.ClasspathBrowser;
import com.hashvoid.crossbinder.dilm.GlobalLifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.Prototype;
import com.hashvoid.crossbinder.dilm.Provider;
import com.hashvoid.crossbinder.dilm.Scanner;
import com.hashvoid.crossbinder.dilm.Singleton;

/**
 * @author poroshuram
 *
 */

public class ScannerImpl implements Scanner {

	private static final Logger LOGGER = Logger.getLogger(ScannerImpl.class.getName());

	private ClasspathBrowser cpb;

	ScannerImpl() {
		cpb = new ClasspathBrowser();
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface Scanner

	@Override
	public Scanner addClassPath(String path) {
		try {
			cpb.addClassPath(path);
		}
		catch (Exception exep) {
			LOGGER.log(Level.WARNING,
					String.format("error adding classpath %s for subsequent scanning", path), exep);
		}
		return this;
	}

	@Override
	public Scanner addLibPath(String path) {
		cpb.addLibPath(path);
		return this;
	}

	@Override
	public Scanner includePackage(String name) {
		cpb.includePackage(name);
		return this;
	}

	@Override
	public Scanner excludePackage(String name) {
		cpb.excludePackage(name);
		return this;
	}

	@Override
	public Scanner includeClassesByPattern(String pattern) {
		cpb.includeClassesByPattern(pattern);
		return this;
	}

	@Override
	public Scanner excludeClassesByPattern(String pattern) {
		cpb.excludeClassesByPattern(pattern);
		return this;
	}

	@Override
	public Scanner includeClasses(String... names) {
		cpb.includeClasses(names);
		return this;
	}

	@Override
	public Scanner excludeClasses(String... names) {
		cpb.excludeClasses(names);
		return this;
	}

	////////////////////////////////////////////////////////////////////////////
	// Package private methods

	void scan() {
		cpb.load();
	}

	List<Class<?>> listLifecycleInterceptors() {
		return cpb.listImplementingClasses(LifecycleInterceptor.class);
	}

	List<Class<?>> listGlobalLifecycleInterceptors() {
		return cpb.listImplementingClasses(GlobalLifecycleInterceptor.class);
	}

	List<Class<?>> listMethodInterceptors() {
		return cpb.listImplementingClasses(MethodInterceptor.class);
	}

	List<Class<?>> listSingletons() {
		return cpb.listAnnotatedClasses(Singleton.class);
	}

	List<Class<?>> listPrototypes() {
		return cpb.listAnnotatedClasses(Prototype.class);
	}

	List<Class<?>> listProviders() {
		return cpb.listAnnotatedClasses(Provider.class);
	}
}
