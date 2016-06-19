/*
 * Copyright 2016 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.core.container.dpi;

import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

/**
 * HK2 factory to provide instances from the request context.
 * <p>
 * Subclasses must inject the {@link RequestContext} via constructor injection.
 *
 * @author Bjoern Bilger
 *
 * @param <T>
 */
public abstract class ContainerRequestContextPropertyFactory<T> implements Factory<T> {

	private final ContainerRequestContext requestContext;

	public ContainerRequestContextPropertyFactory(ContainerRequestContext context) {
		this.requestContext = context;
	}
	// FIXME no need to be abstract
	protected abstract String getPropertyName();

	@Override
	@SuppressWarnings("unchecked")
	public T provide() {
		return (T) requestContext.getProperty(getPropertyName());
	}

	@Override
	public void dispose(Object instance) {
	}

}
