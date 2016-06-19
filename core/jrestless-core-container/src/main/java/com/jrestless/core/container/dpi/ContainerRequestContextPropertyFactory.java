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
