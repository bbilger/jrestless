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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.gateway.GatewayResourceConfig;
import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayIdentityImpl;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayRequestContextImpl;
import com.jrestless.aws.gateway.io.GatewayRequestImpl;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.core.container.dpi.InstanceBinder;

public class GatewayRequestObjectHandlerIntTest {

	private GatewayRequestObjectHandler handler;
	private TestService testService;
	private Context context = mock(Context.class);

	@Before
	public void setup() {
		ResourceConfig config = new GatewayResourceConfig();
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		handler = spy(new GatewayRequestObjectHandlerImpl());
		handler.init(config);
		handler.start();
	}

	@Test
	public void testLambdaContextInjection() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("DELETE");
		request.setPath("/");
		handler.handleRequest(request, context);
		verify(testService).injectLambdaContext(context);
	}

	@Test
	public void testGatewayRequestInjection() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		request.setHttpMethod("PUT");
		request.setPath("/inject-gateway-request");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayRequest(same(request));
	}

	@Test
	public void testGatewayRequestContextInjection() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("PUT");
		request.setPath("/inject-gateway-request-context");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayRequestContext(same(requestContext));
	}

	@Test
	public void testGatewayRequestIdentityInjection() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		GatewayIdentityImpl identity = new GatewayIdentityImpl();
		requestContext.setIdentity(identity);
		request.setRequestContext(requestContext);
		request.setHttpMethod("PUT");
		request.setPath("/inject-gateway-identity");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayIdentity(same(identity));
	}

	@Test
	public void testContainerFailureCreates500() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		doThrow(new RuntimeException()).when(handler).createContainerRequest(any());
		GatewayResponse response = handler.handleRequest(request, context);
		assertEquals(new GatewayResponse(null, new HashMap<>(), Status.INTERNAL_SERVER_ERROR), response);
	}

	@Test
	public void testRoundTrip() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> requestHeaders = ImmutableMap.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON,
				HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		String requestBody = mapper.writeValueAsString(new Entity("123"));
		GatewayRequestImpl request = new GatewayRequestImpl();
		request.setHttpMethod("POST");
		request.setBody(requestBody);
		request.setPath("/round-trip");
		request.setHeaders(requestHeaders);
		GatewayResponse response = handler.handleRequest(request, context);
		Map<String, String> responseHeaders = ImmutableMap.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		String responseBody = mapper.writeValueAsString(new Entity("123"));
		assertEquals(new GatewayResponse(responseBody, responseHeaders, Status.OK), response);
	}

	@Path("/")
	public static class TestResource {

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
		}

		@DELETE
		public Response injectLambdaContext(@javax.ws.rs.core.Context Context context) {
			service.injectLambdaContext(context);
			return Response.ok().build();
		}

		@Path("/inject-gateway-request")
		@PUT
		public Response injectGatewayRequest(@javax.ws.rs.core.Context GatewayRequest request) {
			service.injectGatewayRequest(request);
			return Response.ok().build();
		}

		@Path("/inject-gateway-request-context")
		@PUT
		public Response injectGatewayRequestContext(@javax.ws.rs.core.Context GatewayRequestContext requestContext) {
			service.injectGatewayRequestContext(requestContext);
			return Response.ok().build();
		}

		@Path("/inject-gateway-identity")
		@PUT
		public Response injectGatewayRequestContext(@javax.ws.rs.core.Context GatewayIdentity identity) {
			service.injectGatewayIdentity(identity);
			return Response.ok().build();
		}

		@Path("/round-trip")
		@POST
		public Response putSomething(Entity entity) {
			return Response.ok(entity).build();
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectGatewayRequest(GatewayRequest request);
		void injectGatewayRequestContext(GatewayRequestContext requestContext);
		void injectGatewayIdentity(GatewayIdentity identity);
	}

	public static class Entity {
		private String value;

		@JsonCreator
		public Entity(@JsonProperty("value") String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (!getClass().equals(other.getClass())) {
				return false;
			}
			Entity castOther = (Entity) other;
			return Objects.equals(value, castOther.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class GatewayRequestObjectHandlerImpl extends GatewayRequestObjectHandler {
	}
}
