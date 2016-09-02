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

package com.hashvoid.crossbinder.dilm.support.binder;

/**
 * Encapsulates information about each dependency of a given singleton,
 * prototype, interceptor or provider on other singletons and prototypes.
 * <p>
 *
 * @author poroshuram
 */

public class Dependency {

	private String   name;
	private Class<?> type;
	private boolean  required;
	private int      savedHash;

/**
 *
 * @param name will be set to null if an empty string, or a string with only
 * whitespaces is provided.
 *
 * @param	name a unique name of this dependency, or <tt>null</tt> if no name
 * 			is provided.
 * @param	type the type of object on which the dependency exists. This is the
 * 			interface to which an object is bound.
 * @param	required <tt>true</tt> if the corresponding binding is mandatory,
 * 			<tt>false</tt> otherwise.
 */

	public Dependency(String name, Class<?> type, boolean required) {
		if(name != null && name.trim().length() > 0) {
			this.name = name.trim();
		}
		this.type = type;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of base class Object

	@Override
	public int hashCode() {
		if(savedHash >= 0) {
			return savedHash;
		}
		if(type == null) {
			return 0;
		}
		StringBuilder hashable = new StringBuilder();
		if(name != null) {
			hashable.append(name);
		}
		else {
			hashable.append('?');
		}
		hashable.append('\n').append(type.getName());
		savedHash = hashable.toString().hashCode();
		return savedHash;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Dependency)) {
			return false;
		}
		Dependency dep = (Dependency) obj;
		if(!dep.type.equals(type)) {
			return false;
		}
		if(dep.name != null && name != null && !dep.name.equals(name)) {
			return false;
		}
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(name != null && name.trim().length() > 0) {
			sb.append(name).append(" -> ");
		}
		sb.append(type.getName());
		if(required) {
			sb.append(", required");
		}
		return sb.toString();
	}
}
