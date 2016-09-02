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

/**
 * Exception that is thrown during startup, shutdown and other Crossbinder-specific operations (e.g.
 * interception, injection, etc.)
 *
 * @author poroshuram
 */

@SuppressWarnings("serial")
public class CrossbinderException extends RuntimeException {

	public CrossbinderException() {
		super();
	}

	public CrossbinderException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CrossbinderException(String message, Throwable cause) {
		super(message, cause);
	}

	public CrossbinderException(String message) {
		super(message);
	}

	public CrossbinderException(Throwable cause) {
		super(cause);
	}
}
