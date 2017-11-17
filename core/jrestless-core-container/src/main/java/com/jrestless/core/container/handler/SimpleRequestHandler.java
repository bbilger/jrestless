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
package com.jrestless.core.container.handler;

import static java.util.Objects.requireNonNull;
import static org.glassfish.jersey.internal.guava.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.server.ContainerRequest;

import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessResponseWriter;
import com.jrestless.core.security.AnonSecurityContext;

/**
 * Usage of this is completely optional but it simplifies
 * invocation of the JRestless container.
 * <p>
 * The main assumption here is that we return an object.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class SimpleRequestHandler<RequestT, ResponseT> {

	private JRestlessHandlerContainer<JRestlessContainerRequest> container;

	private boolean initialized = false;
	private boolean started = false;

	/**
	 * Initializes the container using the given application.
	 * <p>
	 * May be called once, only.
	 *
	 * @param application
	 */
	public final void init(@Nonnull Application application) {
		requireNonNull(application);
		init(new JRestlessHandlerContainer<>(application, createBinder()));
	}

	/**
	 * Initializes the container using the given application.
	 * <p>
	 * May be called once, only.
	 *
	 * @param application
	 * @param parentManager
	 */
	public final void init(@Nonnull Application application, @Nullable Object parentManager) {
		requireNonNull(application);
		init(new JRestlessHandlerContainer<>(application, createBinder(), parentManager));
	}

	protected Binder createBinder() {
		return null;
	}

	/**
	 * Initializes the container using the given application, binder and locator.
	 *
	 * @param handlerContainer
	 */
	public final void init(@Nonnull JRestlessHandlerContainer<JRestlessContainerRequest> handlerContainer) {
		requireNonNull(handlerContainer);
		checkState(!initialized, "handler has already been initlialized");
		this.container = handlerContainer;
		initialized = true;
	}

	/**
	 * Starts the container.
	 * <p>
	 * May be called once, only.
	 * <p>
	 * One of the init methods must be called, first.
	 */
	public final void start() {
		checkState(initialized, "handler has not been initialized");
		checkState(!started, "container has already been started");
		container.onStartup();
		started = true;
	}

	/**
	 * Stops the container.
	 * <p>
	 * May be called once, only.
	 * <p>
	 * {@link #start()} must be called, first.
	 */
	public final void stop() {
		checkState(started, "container has already been stopped");
		container.onShutdown();
		started = false;
	}

	/**
	 * Handles the request by passing it to the container and so Jersey.
	 *
	 * @param request
	 * @return
	 */
	public final ResponseT delegateRequest(@Nonnull RequestT request) {
		ResponseT containerResponse;
		JRestlessContainerRequest containerRequest = null;
		try {
			checkState(started, "handler has not been started");
			requireNonNull(request);
			containerRequest = createContainerRequest(request);
			JRestlessContainerRequest containerRequestFinal = containerRequest;
			beforeHandleRequest(request, containerRequest);
			SimpleResponseWriter<ResponseT> responseWriter = createResponseWriter(request);
			container.handleRequest(containerRequest, responseWriter, createSecurityContext(request, containerRequest),
					cReq -> extendActualJerseyContainerRequest(cReq, containerRequestFinal, request));
			containerResponse = responseWriter.getResponse();
			containerResponse = onRequestSuccess(containerResponse, request, containerRequest);
		} catch (Exception e) {
			containerResponse = onRequestFailure(e, request, containerRequest);
		}
		return containerResponse;
	}

	protected abstract SimpleResponseWriter<ResponseT> createResponseWriter(@Nonnull RequestT request);

	protected abstract JRestlessContainerRequest createContainerRequest(RequestT request);

	/**
	 * Hook that is invoked before the request is handled by the container.
	 *
	 * @param request
	 * @param containerRequest
	 */
	protected void beforeHandleRequest(RequestT request, JRestlessContainerRequest containerRequest) {
	}

	/**
	 * Hook that is invoked when the container was able to handle the incoming
	 * request.
	 *
	 * @param response
	 * @param request
	 * @param containerRequest
	 * @return the container response
	 */
	protected ResponseT onRequestSuccess(ResponseT response, RequestT request,
			JRestlessContainerRequest containerRequest) {
		return response;
	}

	/**
	 * Hook that is invoked when the request couldn't be handled by the
	 * container.
	 * <p>
	 * In general an internal server error should be created.
	 *
	 * @param e
	 * @param request
	 * @param containerRequest
	 * @return the error container response
	 */
	protected abstract ResponseT onRequestFailure(Exception e, RequestT request,
			@Nullable JRestlessContainerRequest containerRequest);
	/**
	 * Hook that allows to extend the actual containerRequest passed to the Jersey container.
	 *
	 * @param actualContainerRequest
	 * @param containerRequest
	 * @param request
	 */
	protected void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, RequestT request) {
	}

	/**
	 * Hook that allows creation of a {@link SecurityContext}.
	 * <p>
	 * By default an anon security context is generated.
	 *
	 * @param request
	 * @return
	 */
	@Nonnull
	protected SecurityContext createSecurityContext(RequestT request, JRestlessContainerRequest containerRequest) {
		return new AnonSecurityContext();
	}

	public interface SimpleResponseWriter<ResponseT> extends JRestlessResponseWriter {
		ResponseT getResponse();
	}
}
