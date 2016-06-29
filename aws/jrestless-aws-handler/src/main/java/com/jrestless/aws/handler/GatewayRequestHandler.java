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
package com.jrestless.aws.handler;

import static java.util.Objects.requireNonNull;
import static jersey.repackaged.com.google.common.base.Preconditions.checkState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.dpi.ApiGatewayContextFactory;
import com.jrestless.aws.dpi.LambdaContextFactory;
import com.jrestless.aws.filter.IsDefaultResponseFilter;
import com.jrestless.aws.io.GatewayAdditionalResponseException;
import com.jrestless.aws.io.GatewayDefaultResponse;
import com.jrestless.aws.io.GatewayRequest;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessResponseWriter;
import com.jrestless.core.security.AnonSecurityContext;

/**
 * Base request handler.
 * <p>
 * Note: we don't implement
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler RequestHandler}
 * in case s.o. needs
 * {@link com.amazonaws.services.lambda.runtime.RequestStreamHandler
 * RequestStreamHandler}.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class GatewayRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GatewayRequestHandler.class);

	private JRestlessHandlerContainer<GatewayRequest> container;

	private boolean initialized = false;
	private boolean started = false;

	/**
	 * Initializes the container using the given application.
	 * <p>
	 * May be called once, only.
	 *
	 * @param application
	 */
	protected final void init(@Nonnull Application application) {
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
	protected final void init(@Nonnull Application application, @Nullable Binder customBinder,
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
	protected final void init(@Nonnull JRestlessHandlerContainer<GatewayRequest> handlerContainer) {
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
	protected final void start() {
		checkState(initialized, "handler has not been initialized");
		checkState(!started, "container has already been started");
		container.onStartup();
		started = true;
	}

	/**
	 * Handles the request from the API gateway by passing it to the container
	 * and so Jersey.
	 *
	 * @param request
	 * @param context
	 * @throws GatewayAdditionalResponseException
	 *             Non-default responses are passed back to the API gateway as
	 *             an exception.
	 * @return
	 */
	protected final GatewayDefaultResponse delegateRequest(@Nonnull GatewayRequest request, @Nonnull Context context) {
		GatewayContainerResponse containerResponse;
		try {
			checkState(started, "handler has not been started");
			requireNonNull(request);
			requireNonNull(context);
			beforeHandleRequest(request, context);
			GatewayContainerResponseWriter responseWriter = createResponseWriter();
			container.handleRequest(request, responseWriter, createSecurityContext(request, context), (cReq) -> {
				extendContainerRequest(cReq, request, context);
			});
			containerResponse = responseWriter.getResponse();
			requireNonNull(containerResponse);
			containerResponse = onRequestSuccess(containerResponse, request, context);
		} catch (Exception e) {
			LOG.error("request failed", e);
			try {
				containerResponse = onRequestFailure(e, request, context);
			} catch (Exception requestFailureException) {
				LOG.error("onRequestFailure hook failed", requestFailureException);
				containerResponse = GatewayContainerResponse.createInternalServerErrorResponse();
			}
		}
		if (containerResponse == null) {
			LOG.error("no containerResponse set => responding with internal server error");
			containerResponse = GatewayContainerResponse.createInternalServerErrorResponse();
		}
		return createGatewayResponse(containerResponse);
	}

	// for unit testing
	GatewayContainerResponseWriter createResponseWriter() {
		return new GatewayContainerResponseWriter();
	}

	/**
	 * Creates the actual response for the API gateway from the container
	 * response.
	 *
	 * @param containerResponse
	 * @throws GatewayAdditionalResponseException
	 *             Non-default responses are passed back to the API gateway as
	 *             exceptions.
	 * @return
	 */
	protected GatewayDefaultResponse createGatewayResponse(GatewayContainerResponse containerResponse) {
		String body = containerResponse.getBody();
		Map<String, List<String>> headers = containerResponse.getHeaders();
		StatusType statusType = containerResponse.getStatusType();
		if (isDefaultResponse(containerResponse)) {
			return new GatewayDefaultResponse(body, headers, statusType);
		} else {
			throw new GatewayAdditionalResponseException(body, statusType);
		}
	}
	/**
	 * Checks if the container response is the default response or not.
	 *
	 * @param containerResponse
	 * @return
	 */
	protected boolean isDefaultResponse(GatewayContainerResponse containerResponse) {
		List<String> isDefaultHeader = containerResponse.getHeaders()
				.get(IsDefaultResponseFilter.IS_DEFAULT_RESPONSE_HEADER_NAME);
		if (isDefaultHeader == null || isDefaultHeader.isEmpty()) {
			return Status.OK.equals(containerResponse.getStatusType());
		} else {
			return "1".equals(isDefaultHeader.get(0));
		}
	}

	/**
	 * Hook that is invoked before the request is handled by the container.
	 *
	 * @param context
	 */
	protected void beforeHandleRequest(GatewayRequest request, Context context) {
	}

	/**
	 * Hook that is invoked when the container was able to handle the incoming
	 * request.
	 *
	 * @param responseStatusCode
	 * @param request
	 * @param responseBody
	 * @param responseHeaders
	 * @param context
	 * @return the container response
	 */
	protected GatewayContainerResponse onRequestSuccess(GatewayContainerResponse response, GatewayRequest request,
			Context context) {
		return response;
	}

	/**
	 * Hook that is invoked when the request couldn't be handled by the
	 * container.
	 * <p>
	 * By default an internal server error is created.
	 *
	 * @param e
	 * @param context
	 * @return the error container response
	 */
	protected GatewayContainerResponse onRequestFailure(Exception e, GatewayRequest request, Context context) {
		return GatewayContainerResponse.createInternalServerErrorResponse();
	}

	/**
	 * Hook that allows to extend the containerRequest.
	 * <p>
	 * By default {@link Context} is registered as property 'awsLambdaContext'
	 * and {@link com.jrestless.aws.GatewayRequestContext} is registered as
	 * property 'awsApiGatewayContext'.
	 *
	 *
	 * @param containerRequest
	 * @param request
	 * @param context
	 */
	protected void extendContainerRequest(ContainerRequest containerRequest, GatewayRequest request, Context context) {
		containerRequest.setProperty(LambdaContextFactory.AWS_LAMBDA_CONTEXT_PROPERTY_NAME, context);
		containerRequest.setProperty(ApiGatewayContextFactory.AWS_API_GATEWAY_CONTEXT_PROPERTY_NAME,
				request.getContext());
	}

	/**
	 * Hook that allows creation of a {@link SecurityContext}.
	 * <p>
	 * By default an anon security context is generated.
	 *
	 * @param request
	 * @param context
	 * @return
	 */
	@Nonnull
	protected SecurityContext createSecurityContext(GatewayRequest request, Context context) {
		return new AnonSecurityContext();
	}

	/**
	 * Response writer that captures the response values.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	static class GatewayContainerResponseWriter implements JRestlessResponseWriter {

		private GatewayContainerResponse response;

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			response = new GatewayContainerResponse(statusType, entityOutputStream.toString(), headers);
		}

		GatewayContainerResponse getResponse() {
			return response;
		}

		@Override
		public ByteArrayOutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}
	}

	/**
	 * The response values as returned by the {@link JRestlessResponseWriter}.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class GatewayContainerResponse {

		private final StatusType statusType;
		private final String body;
		private final Map<String, List<String>> headers = new HashMap<>();

		protected GatewayContainerResponse(StatusType statusType, String body, Map<String, List<String>> headers) {
			this.statusType = statusType;
			this.body = body;
			if (headers != null) {
				this.headers.putAll(headers);
			}
		}

		protected static GatewayContainerResponse createInternalServerErrorResponse() {
			return new GatewayContainerResponse(Status.INTERNAL_SERVER_ERROR, null, new HashMap<>());
		}

		protected StatusType getStatusType() {
			return statusType;
		}

		protected String getBody() {
			return body;
		}

		protected Map<String, List<String>> getHeaders() {
			return headers;
		}
	}
}
