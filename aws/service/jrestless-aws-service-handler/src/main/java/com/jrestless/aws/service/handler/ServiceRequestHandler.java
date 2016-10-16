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
package com.jrestless.aws.service.handler;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.server.ContainerRequest;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.service.dpi.LambdaContextFactory;
import com.jrestless.aws.service.dpi.ServiceRequestContextFactory;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceResponse;
import com.jrestless.aws.service.io.ServiceResponseImpl;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequestImpl;

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
public abstract class ServiceRequestHandler
		extends SimpleRequestHandler<ServiceRequestAndLambdaContext, ServiceResponse> {

	private static final URI ROOT_URI = URI.create("/");

	@Override
	public JRestlessContainerRequest createContainerRequest(ServiceRequestAndLambdaContext requestAndLambdaContext) {
		requireNonNull(requestAndLambdaContext);
		ServiceRequest request = requireNonNull(requestAndLambdaContext.getServiceRequest());
		URI requestUri = requireNonNull(request.getRequestUri());
		String body = request.getBody();
		InputStream entityStream;
		if (body != null) {
			entityStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		} else {
			entityStream = new ByteArrayInputStream(new byte[0]);
		}
		return new JRestlessContainerRequestImpl(ROOT_URI, requestUri, request.getHttpMethod(), entityStream,
				request.getHeaders());
	}

	@Override
	public void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, ServiceRequestAndLambdaContext requestAndLambdaContext) {
		ServiceRequest request = requestAndLambdaContext.getServiceRequest();
		Context lamdaContext = requestAndLambdaContext.getLambdaContext();
		actualContainerRequest.setProperty(LambdaContextFactory.PROPERTY_NAME, lamdaContext);
		actualContainerRequest.setProperty(ServiceRequestContextFactory.PROPERTY_NAME, request);
	}

	@Override
	public SimpleResponseWriter<ServiceResponse> createResponseWriter() {
		return new SimpleResponseWriter<ServiceResponse>() {
			private ServiceResponse response;

			@Override
			public OutputStream getEntityOutputStream() {
				return new ByteArrayOutputStream();
			}

			@Override
			public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
					OutputStream entityOutputStream) throws IOException {
				String body = ((ByteArrayOutputStream) entityOutputStream).toString(StandardCharsets.UTF_8.name());
				response = new ServiceResponseImpl(body, headers, statusType.getStatusCode(),
						statusType.getReasonPhrase());
			}

			@Override
			public ServiceResponse getResponse() {
				return response;
			}
		};
	}

	@Override
	public ServiceResponse createInternalServerErrorResponse() {
		return new ServiceResponseImpl(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
	}
}
