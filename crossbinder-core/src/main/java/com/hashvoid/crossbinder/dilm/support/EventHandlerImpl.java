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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hashvoid.crossbinder.dilm.LifecycleInterceptor;
import com.hashvoid.crossbinder.dilm.MethodInterceptor;
import com.hashvoid.crossbinder.dilm.support.binder.Binder;
import com.hashvoid.crossbinder.dilm.support.binder.EventHandler;

/**
 * @author poroshuram
 *
 */

public class EventHandlerImpl implements EventHandler {

	private static final Logger LOGGER = Logger.getLogger(EventHandlerImpl.class.getName());

	private LocatorImpl locator;
	private boolean     readyFlag;

	EventHandlerImpl(LocatorImpl locator) {
		this.locator = locator;
		readyFlag = false;
	}

	void getReady() {
		readyFlag = true;
	}

	////////////////////////////////////////////////////////////////////////////
	// Methods of interface EventHandler

	@Override
	public void instanceCreated(Object inst) {
		if(!readyFlag) {
			return;
		}

		for(Binder binder : locator.getLifecycleInterceptors()) {
			LifecycleInterceptor lci = binder.getInstance(LifecycleInterceptor.class);
			if(lci != null) {
				lci.afterCreation(inst);
			}
		}
	}

	@Override
	public void instanceInjected(Object inst) {
		if(!readyFlag) {
			return;
		}

		for(Binder binder : locator.getLifecycleInterceptors()) {
			LifecycleInterceptor lci = binder.getInstance(LifecycleInterceptor.class);
			if(lci != null) {
				lci.afterInjection(inst);
			}
		}
	}

	@Override
	public void instanceInitialized(Object inst) {
		if(!readyFlag) {
			return;
		}

		for(Binder binder : locator.getLifecycleInterceptors()) {
			LifecycleInterceptor lci = binder.getInstance(LifecycleInterceptor.class);
			if(lci != null) {
				lci.afterInitialization(inst);
			}
		}
	}

	@Override
	public void instanceDisposing(Object inst) {
		if(!readyFlag) {
			return;
		}

		for(Binder binder : locator.getLifecycleInterceptors()) {
			LifecycleInterceptor lci = binder.getInstance(LifecycleInterceptor.class);
			if(lci != null) {
				lci.beforeDisposal(inst);
			}
		}
	}

	@Override
	public void instanceDisposed(Object inst) {
		if(!readyFlag) {
			return;
		}

		for(Binder binder : locator.getLifecycleInterceptors()) {
			LifecycleInterceptor lci = binder.getInstance(LifecycleInterceptor.class);
			if(lci != null) {
				lci.afterDisposal(inst);
			}
		}
	}

	@Override
	public void beforeMethod(Object inst, Method method, Object[] args) {
		if(!readyFlag) {
			return;
		}
		for(Binder binder : locator.getMethodInterceptors()) {
			MethodInterceptor mthdi = binder.getInstance(MethodInterceptor.class);
			if(mthdi != null) {
				mthdi.before(method, inst, args);
			}
		}
	}

	@Override
	public Object wrapMethod(Object inst, Method method, Object[] args) throws Throwable {
		if(!readyFlag) {
			try {
				return method.invoke(inst, args);
			}
			catch(InvocationTargetException exep) {
				throw exep.getCause();
			}
			catch(Exception exep) {
				LOGGER.log(Level.SEVERE, "direct method execution failed", exep);
				return null;
			}
		}
		for(Binder binder : locator.getMethodInterceptors()) {
			MethodInterceptor mthdi = binder.getInstance(MethodInterceptor.class);
			if(mthdi != null && mthdi.isWrapped(method, inst)) {
				return mthdi.wrap(method, inst, args);
			}
		}
		try {
			return method.invoke(inst, args);
		}
		catch(InvocationTargetException exep) {
			throw exep.getCause();
		}
		catch(Exception exep) {
			LOGGER.log(Level.SEVERE, "direct method execution failed", exep);
			return null;
		}
	}

	@Override
	public void afterMethodSuccess(Object inst, Method method, Object result) {
		if(!readyFlag) {
			return;
		}
		for(Binder binder : locator.getMethodInterceptors()) {
			MethodInterceptor mthdi = binder.getInstance(MethodInterceptor.class);
			if(mthdi != null) {
				mthdi.after(method, inst, result);
			}
		}
	}

	@Override
	public void afterMethodFail(Object inst, Method method, Throwable error) {
		if(!readyFlag) {
			return;
		}
		for(Binder binder : locator.getMethodInterceptors()) {
			MethodInterceptor mthdi = binder.getInstance(MethodInterceptor.class);
			if(mthdi != null) {
				mthdi.onError(method, inst, error);
			}
		}
	}
}
