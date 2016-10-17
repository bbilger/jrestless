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
package com.jrestless.aws.gateway.handler;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.server.ContainerRequest;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.gateway.dpi.GatewayIdentityContextFactory;
import com.jrestless.aws.gateway.dpi.GatewayRequestContextContextFactory;
import com.jrestless.aws.gateway.dpi.GatewayRequestContextFactory;
import com.jrestless.aws.gateway.dpi.LambdaContextFactory;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequestImpl;
import com.jrestless.core.util.HeaderUtils;

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
public abstract class GatewayRequestHandler
		extends SimpleRequestHandler<GatewayRequestAndLambdaContext, GatewayResponse> {

	private static final URI ROOT_URI = URI.create("/");

	@Override
	public JRestlessContainerRequest createContainerRequest(GatewayRequestAndLambdaContext requestAndLambdaContext) {
		requireNonNull(requestAndLambdaContext);
		GatewayRequest request = requestAndLambdaContext.getGatewayRequest();
		requireNonNull(request);
		requireNonNull(request.getPath());
		String body = request.getBody();
		InputStream entityStream;
		if (body != null) {
			entityStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		} else {
			entityStream = new ByteArrayInputStream(new byte[0]);
		}
		URI requestUri = URI.create(appendQueryParams(request.getPath(), request.getQueryStringParameters()));
		return new JRestlessContainerRequestImpl(ROOT_URI, requestUri, request.getHttpMethod(), entityStream,
				HeaderUtils.expandHeaders(request.getHeaders()));
	}

	private String appendQueryParams(String requestUri, Map<String, String> queryParameters) {
		if (!queryParameters.isEmpty()) {
			StringBuilder requestUriBuilder = new StringBuilder(requestUri);
			requestUriBuilder.append("?");
			boolean first = true;
			for (Map.Entry<String, String> queryParam : queryParameters.entrySet()) {
				if (!first) {
					requestUriBuilder.append("&");
				}
				requestUriBuilder.append(encodeQueryParam(queryParam.getKey()));
				requestUriBuilder.append("=");
				requestUriBuilder.append(encodeQueryParam(queryParam.getValue()));
				first = false;
			}
			return requestUriBuilder.toString();
		} else {
			return requestUri;
		}
	}

	private String encodeQueryParam(String param) {
		try {
			return URLEncoder.encode(param, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new QueryParameterEncodingException(e);
		}
	}

	@Override
	public void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, GatewayRequestAndLambdaContext requestAndLambdaContext) {
		GatewayRequest request = requestAndLambdaContext.getGatewayRequest();
		Context lamdaContext = requestAndLambdaContext.getLambdaContext();
		actualContainerRequest.setProperty(LambdaContextFactory.PROPERTY_NAME, lamdaContext);
		actualContainerRequest.setProperty(GatewayRequestContextFactory.PROPERTY_NAME, request);
		GatewayRequestContext requestContext = request.getRequestContext();
		actualContainerRequest.setProperty(GatewayRequestContextContextFactory.PROPERTY_NAME, requestContext);
		if (requestContext != null) {
			actualContainerRequest.setProperty(GatewayIdentityContextFactory.PROPERTY_NAME,
					requestContext.getIdentity());
		}
	}

	@Override
	public SimpleResponseWriter<GatewayResponse> createResponseWriter() {
		return new ResponseWriter();
	}

	@Override
	public GatewayResponse createInternalServerErrorResponse() {
		return new GatewayResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR);
	}

	public static class QueryParameterEncodingException extends RuntimeException {

		private static final long serialVersionUID = -7545175514996382745L;

		QueryParameterEncodingException(Exception cause) {
			super(cause);
		}
	}

	protected static class ResponseWriter implements SimpleResponseWriter<GatewayResponse> {
		private GatewayResponse response;

		public ResponseWriter() {
		}

		@Override
		public OutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			Map<String, String> flattenedHeaders = HeaderUtils.flattenHeaders(headers);
			String body = ((ByteArrayOutputStream) entityOutputStream).toString(StandardCharsets.UTF_8.name());
			response = new GatewayResponse(body, flattenedHeaders, statusType);
		}

		@Override
		public GatewayResponse getResponse() {
			return response;
		}
	}
}
