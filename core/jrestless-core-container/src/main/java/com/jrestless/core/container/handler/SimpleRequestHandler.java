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
import static jersey.repackaged.com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(SimpleRequestHandler.class);

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
		init(new JRestlessHandlerContainer<>(application));
	}

	/**
	 * Initializes the container using the given application, binder and locator.
	 * <p>
	 * May be called once, only.
	 *
	 * @param application
	 */
	public final void init(@Nonnull Application application, @Nullable Binder customBinder,
			@Nonnull ServiceLocator parent) {
		requireNonNull(application);
		requireNonNull(parent);
		init(new JRestlessHandlerContainer<>(application, customBinder, parent));
	}


	/**
	 * Initializes the container using the given application, binder and locator.
	 *
	 * @param application
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
			SimpleResponseWriter<ResponseT> responseWriter = createResponseWriter();
			container.handleRequest(containerRequest, responseWriter,
					createSecurityContext(request, containerRequest), cReq -> {
						extendActualJerseyContainerRequest(cReq, containerRequestFinal, request);
					});
			containerResponse = responseWriter.getResponse();
			requireNonNull(containerResponse);
			containerResponse = onRequestSuccess(containerResponse, request, containerRequest);
		} catch (Exception e) {
			LOG.error("request failed", e);
			try {
				containerResponse = onRequestFailure(e, request, containerRequest);
			} catch (Exception requestFailureException) {
				LOG.error("onRequestFailure hook failed", requestFailureException);
				containerResponse = createInternalServerErrorResponse();
			}
		}
		if (containerResponse == null) {
			LOG.error("no containerResponse set => responding with internal server error");
			containerResponse = createInternalServerErrorResponse();
		}
		return containerResponse;
	}

	public abstract SimpleResponseWriter<ResponseT> createResponseWriter();

	public abstract JRestlessContainerRequest createContainerRequest(RequestT request);

	public abstract ResponseT createInternalServerErrorResponse();

	/**
	 * Hook that is invoked before the request is handled by the container.
	 *
	 * @param request
	 * @param containerRequest
	 */
	public void beforeHandleRequest(RequestT request, JRestlessContainerRequest containerRequest) {
	}

	/**
	 * Hook that is invoked when the container was able to handle the incoming
	 * request.
	 *
	 * @param responseStatusCode
	 * @param request
	 * @param containerRequest
	 * @param responseBody
	 * @param responseHeaders
	 * @return the container response
	 */
	public ResponseT onRequestSuccess(ResponseT response, RequestT request,
			JRestlessContainerRequest containerRequest) {
		return response;
	}

	/**
	 * Hook that is invoked when the request couldn't be handled by the
	 * container.
	 * <p>
	 * By default an internal server error is created.
	 *
	 * @param e
	 * @param request
	 * @param containerRequest
	 * @return the error container response
	 */
	public ResponseT onRequestFailure(Exception e, RequestT request,
			@Nullable JRestlessContainerRequest containerRequest) {
		return createInternalServerErrorResponse();
	}

	/**
	 * Hook that allows to extend the actual containerRequest passed to the Jersey container.
	 *
	 * @param actualContainerRequest
	 * @param containerRequest
	 * @param request
	 */
	public void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
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
	public SecurityContext createSecurityContext(RequestT request, JRestlessContainerRequest containerRequest) {
		return new AnonSecurityContext();
	}

	public interface SimpleResponseWriter<ResponseT> extends JRestlessResponseWriter {
		ResponseT getResponse();
	}
}
