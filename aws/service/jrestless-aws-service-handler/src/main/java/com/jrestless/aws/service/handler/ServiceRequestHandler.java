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
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.AbstractLambdaContextReferencingBinder;
import com.jrestless.aws.service.io.DefaultServiceResponse;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceResponse;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.DefaultJRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.RequestAndBaseUri;

/**
 * Base request handler.
 * <p>
 * Note: we don't implement
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler RequestHandler}
 * in case we need
 * {@link com.amazonaws.services.lambda.runtime.RequestStreamHandler
 * RequestStreamHandler} at some point.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class ServiceRequestHandler
		extends SimpleRequestHandler<ServiceRequestAndLambdaContext, ServiceResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestHandler.class);

	private static final URI BASE_ROOT_URI = URI.create("/");

	private static final Type SERVICE_REQUEST_TYPE = (new TypeLiteral<Ref<ServiceRequest>>() { }).getType();

	protected ServiceRequestHandler() {
	}

	@Override
	protected JRestlessContainerRequest createContainerRequest(ServiceRequestAndLambdaContext requestAndLambdaContext) {
		requireNonNull(requestAndLambdaContext);
		ServiceRequest request = requireNonNull(requestAndLambdaContext.getServiceRequest());
		String body = request.getBody();
		InputStream entityStream;
		if (body != null) {
			entityStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		} else {
			entityStream = new ByteArrayInputStream(new byte[0]);
		}
		RequestAndBaseUri requestAndBaseUri = getRequestAndBaseUri(requestAndLambdaContext);
		return new DefaultJRestlessContainerRequest(requestAndBaseUri, request.getHttpMethod(), entityStream,
				request.getHeaders());
	}

	@Override
	protected void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, ServiceRequestAndLambdaContext requestAndLambdaContext) {
		ServiceRequest request = requestAndLambdaContext.getServiceRequest();
		Context lambdaContext = requestAndLambdaContext.getLambdaContext();
		actualContainerRequest.setRequestScopedInitializer(locator -> {
			Ref<ServiceRequest> serviceRequestRef = locator
					.<Ref<ServiceRequest>>getService(SERVICE_REQUEST_TYPE);
			if (serviceRequestRef != null) {
				serviceRequestRef.set(request);
			} else {
				LOG.error("ServiceFeature has not been registered. ServiceRequest injection won't work.");
			}
			Ref<Context> contextRef = locator
					.<Ref<Context>>getService(AbstractLambdaContextReferencingBinder.LAMBDA_CONTEXT_TYPE);
			if (contextRef != null) {
				contextRef.set(lambdaContext);
			} else {
				LOG.error("AwsFeature has not been registered. Context injection won't work.");
			}
		});
	}

	@Nonnull
	protected RequestAndBaseUri getRequestAndBaseUri(@Nonnull ServiceRequestAndLambdaContext requestAndLambdaContext) {
		ServiceRequest request = requestAndLambdaContext.getServiceRequest();
		URI requestUri = requireNonNull(request.getRequestUri());
		return new RequestAndBaseUri(BASE_ROOT_URI, requestUri);
	}

	@Override
	protected SimpleResponseWriter<ServiceResponse> createResponseWriter(
			ServiceRequestAndLambdaContext requestAndContext) {
		return new ResponseWriter();
	}

	@Override
	protected ServiceResponse onRequestFailure(Exception e, ServiceRequestAndLambdaContext request,
			JRestlessContainerRequest containerRequest) {
		LOG.error("request failed", e);
		return new DefaultServiceResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
	}

	@Override
	protected final Binder createBinder() {
		return new ServiceRequestBinder();
	}

	protected static class ResponseWriter implements SimpleResponseWriter<ServiceResponse> {
		private ServiceResponse response;

		public ResponseWriter() {
			// allow usage by ServiceRequestHandler subclasses
		}

		@Override
		public OutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			String body = ((ByteArrayOutputStream) entityOutputStream).toString(StandardCharsets.UTF_8.name());
			response = new DefaultServiceResponse(body, headers, statusType.getStatusCode(),
					statusType.getReasonPhrase());
		}

		@Override
		public ServiceResponse getResponse() {
			return response;
		}
	}

	private static class ServiceRequestBinder extends AbstractLambdaContextReferencingBinder {
		@Override
		protected void configure() {
			bindReferencingLambdaContextFactory();
			bindReferencingFactory(ServiceRequest.class, ReferencingServiceRequestFactory.class,
					new TypeLiteral<Ref<ServiceRequest>>() { });
		}
	}

	private static class ReferencingServiceRequestFactory extends ReferencingFactory<ServiceRequest> {
		@Inject
		ReferencingServiceRequestFactory(final Provider<Ref<ServiceRequest>> referenceFactory) {
			super(referenceFactory);
		}
	}
}
