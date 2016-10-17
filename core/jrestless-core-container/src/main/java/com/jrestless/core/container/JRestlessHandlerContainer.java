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
package com.jrestless.core.container;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessResponseWriter;


/**
 * Jersey container for any kind of serverless requests.
 * <p>
 * An implementation of {@link Container} that allows handling
 * serverless requests ({@link #handleRequest(JRestlessContainerRequest, JRestlessResponseWriter, SecurityContext)}).
 *
 * @author Bjoern Bilger
 *
 */
public class JRestlessHandlerContainer<RequestT extends JRestlessContainerRequest> implements Container {

	private static final Logger LOG = LoggerFactory.getLogger(JRestlessHandlerContainer.class);

	private volatile ApplicationHandler appHandler;

	/**
	 * Creates a new JRestless container.
	 *
	 * @param application
	 *            JAX-RS / Jersey application to be deployed on the JRestless
	 *            container.
	 */
	public JRestlessHandlerContainer(Application application) {
		this(new ApplicationHandler(application));
	}

	/**
	 * Creates a new JRestless container.
	 *
	 * @param application
	 *            JAX-RS / Jersey application to be deployed on the JRestless
	 *            container.
	 * @param customBinder
	 *            additional custom bindings used during {@link ServiceLocator}
	 *            creation.
	 * @param parentLocator
	 *            parent HK2 service locator.
	 */
	public JRestlessHandlerContainer(Application application, Binder customBinder, ServiceLocator parentLocator) {
		this(new ApplicationHandler(application, customBinder, parentLocator));
	}

	protected JRestlessHandlerContainer(@Nonnull ApplicationHandler applicationHandler) {
		requireNonNull(applicationHandler, "applicationHandler may not be null");
		this.appHandler = applicationHandler;
	}

	/**
	 * Shortcut for
	 * {@link #handleRequest(JRestlessContainerRequest, JRestlessResponseWriter, SecurityContext, Consumer)}
	 * with noop containerRequestEnhancer.
	 *
	 * @param request
	 * @param responseWriter
	 * @param securityContext
	 */
	public void handleRequest(@Nonnull RequestT request, @Nonnull JRestlessResponseWriter responseWriter,
			@Nonnull SecurityContext securityContext) {
		handleRequest(request, responseWriter, securityContext, req -> { });
	}

	/**
	 * Creates a container request from the given input and delegates it to the
	 * application.
	 *
	 * @param request
	 * @param responseWriter
	 *            the response writer.
	 * @param securityContext
	 *            the security context of the request.
	 * @param containerRequestEnhancer
	 *            additional container request customizer.
	 */
	public void handleRequest(@Nonnull RequestT request, @Nonnull JRestlessResponseWriter responseWriter,
			@Nonnull SecurityContext securityContext, @Nonnull Consumer<ContainerRequest> containerRequestEnhancer) {
		requireNonNull(responseWriter, "responseWriter may not be null");
		requireNonNull(containerRequestEnhancer, "containerRequestExtender may not be null");
		ContainerRequest containerRequest = createContainerRequest(request,
				new JRestlessContainerResponseWriter(responseWriter), securityContext);
		containerRequestEnhancer.accept(containerRequest);
		handleRequest(containerRequest);
	}

	/**
	 * Creates a new {@link ContainerRequest} for the given input.
	 *
	 * @param request
	 * @param containerResponseWriter
	 * @param securityContext
	 * @return
	 */
	@Nonnull
	protected ContainerRequest createContainerRequest(@Nonnull RequestT request,
			@Nonnull ContainerResponseWriter containerResponseWriter, @Nonnull SecurityContext securityContext) {
		requireNonNull(request, "request may not be null");
		URI baseUri = requireNonNull(request.getBaseUri(), "request.getBaseUri() may not be null");
		URI requestUri = requireNonNull(request.getRequestUri(), "request.getRequestUri() may not be null");
		String httpMethod = requireNonNull(request.getHttpMethod(), "request.getHttpMethod() may not be null");
		InputStream entityStream = requireNonNull(request.getEntityStream(),
				"request.getEntityStream() may not be null");
		Map<String, List<String>> headers = requireNonNull(request.getHeaders(),
				"request.getHeaderParams() may not be null");
		requireNonNull(containerResponseWriter, "containerResponseWriter may not be null");
		requireNonNull(securityContext, "securityContext may not be null");

		ContainerRequest requestContext = new ContainerRequest(baseUri, requestUri, httpMethod, securityContext,
				new MapPropertiesDelegate());
		requestContext.setEntityStream(entityStream);
		requestContext.getHeaders().putAll(headers);
		requestContext.setWriter(containerResponseWriter);

		return requestContext;
	}

	/**
	 * Delegates the container request to the application.
	 *
	 * @param request
	 *            container request.
	 */
	protected void handleRequest(@Nonnull ContainerRequest request) {
		requireNonNull(request, "request may not be null");
		try {
			appHandler.handle(request);
		} catch (Exception e) {
			LOG.error("failed to handle request", e);
			throw e;
		}
	}

	@Override
	public ResourceConfig getConfiguration() {
		return appHandler.getConfiguration();
	}

	@Override
	public ApplicationHandler getApplicationHandler() {
		return appHandler;
	}

	@Override
	public void reload() {
		reload(getConfiguration());
	}

	@Override
	public void reload(ResourceConfig configuration) {
		LOG.info("reloading container...");
		appHandler.onShutdown(this);

		appHandler = createNewApplicationHandler(configuration);
		appHandler.onReload(this);
		appHandler.onStartup(this);
		LOG.info("reloaded container");
	}

	// JUnit
	ApplicationHandler createNewApplicationHandler(ResourceConfig configuration) {
		return new ApplicationHandler(configuration);
	}

	/**
	 * Inform this container that the server has been started.
	 *
	 * This method must be implicitly called after the server containing this container is started.
	 */
	public void onStartup() {
		LOG.info("starting container...");
		appHandler.onStartup(this);
		LOG.info("started container");
	}

	/**
	 * Inform this container that the server is being stopped.
	 *
	 * This method must be implicitly called before the server containing this container is stopped.
	 */
	public void onShutdown() {
		LOG.info("stopping container...");
		appHandler.onShutdown(this);
		LOG.info("stopped container");
	}

	/**
	 * Mutable container response used by {@link JRestlessContainerResponseWriter}.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class JRestlessContainerResponse {

		private static final Logger LOG = LoggerFactory.getLogger(JRestlessContainerResponse.class);

		private StatusType statusType;
		private final OutputStream entityOutputStream;

		private final Map<String, List<String>> headers;
		private final AtomicBoolean closed;
		private final JRestlessResponseWriter responseWriter;

		public JRestlessContainerResponse(@Nonnull JRestlessResponseWriter responseWriter) {
			requireNonNull(responseWriter, "responseWriter may not be null");

			this.entityOutputStream = requireNonNull(responseWriter.getEntityOutputStream());
			this.closed = new AtomicBoolean(false);
			this.responseWriter = responseWriter;
			this.headers = new MultivaluedHashMap<>();
			this.statusType = Status.OK;
		}

		public void close() {
			if (closed.compareAndSet(false, true)) {
				try {
					responseWriter.writeResponse(statusType, headers, entityOutputStream);
				} catch (IOException e) {
					LOG.error("failed to write response", e);
					throw new ContainerException(e);
				}
			} else {
				LOG.warn("request has already been closed");
			}
		}

		public boolean isClosed() {
			return closed.get();
		}

		@Nonnull
		public StatusType getStatusType() {
			return statusType;
		}

		public void setStatusType(@Nonnull StatusType statusType) {
			requireNonNull(statusType, "statusType may not be null");
			this.statusType = statusType;
		}

		@Nonnull
		public OutputStream getEntityOutputStream() {
			return entityOutputStream;
		}

		@Nonnull
		public Map<String, List<String>> getHeaders() {
			return headers;
		}
	}

	/**
	 * A {@link ContainerResponseWriter} writing a
	 * {@link JRestlessContainerResponse}. The actual response writing gets
	 * delegated to {@link JRestlessContainerResponse}'s
	 * {@link JRestlessResponseWriter}.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class JRestlessContainerResponseWriter implements ContainerResponseWriter {

		private static final Logger LOG = LoggerFactory.getLogger(JRestlessHandlerContainer.class);

		private final JRestlessContainerResponse response;

		protected JRestlessContainerResponseWriter(@Nonnull JRestlessContainerResponse response) {
			this.response = requireNonNull(response, "response may not be null");
		}

		public JRestlessContainerResponseWriter(@Nonnull JRestlessResponseWriter response) {
			this(new JRestlessContainerResponse(requireNonNull(response, "response may not be null")));
		}

		@Override
		public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse context) {
			response.setStatusType(context.getStatusInfo());
			response.getHeaders().putAll(context.getStringHeaders());
			return response.getEntityOutputStream();
		}

		@Override
		public void commit() {
			response.close();
		}

		@Override
		public void failure(Throwable error) {
			LOG.error("container failure", error);
			response.setStatusType(Status.INTERNAL_SERVER_ERROR);
			try {
				commit();
			} catch (RuntimeException e) {
				LOG.error("failed to commit failure", e);
				throw e;
			}
		}

		@Override
		public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
			throw new UnsupportedOperationException("#suspend is not supported by the container.");
		}

		@Override
		public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) {
			throw new UnsupportedOperationException("#setSuspendTimeout is not supported by the container.");
		}

		@Override
		public boolean enableResponseBuffering() {
			return false;
		}
	}
}
