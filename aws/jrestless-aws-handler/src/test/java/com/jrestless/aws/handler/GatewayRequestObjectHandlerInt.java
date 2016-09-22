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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
import com.jrestless.aws.GatewayIdentity;
import com.jrestless.aws.GatewayRequest;
import com.jrestless.aws.GatewayRequestContext;
import com.jrestless.aws.GatewayResourceConfig;
import com.jrestless.aws.io.GatewayIdentityImpl;
import com.jrestless.aws.io.GatewayRequestContextImpl;
import com.jrestless.aws.io.GatewayRequestImpl;
import com.jrestless.aws.io.GatewayResponse;
import com.jrestless.core.container.dpi.InstanceBinder;

public class GatewayRequestObjectHandlerInt {

	private GatewayRequestObjectHandler handler;
	private TestService testService;

	@Before
	public void setup() {
		ResourceConfig config = new GatewayResourceConfig();
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		handler = new GatewayRequestObjectHandlerImpl();
		handler.init(config);
		handler.start();
	}

	@Test
	public void testLambdaContextInjection() {
		Context context = mock(Context.class);
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
		Context context = mock(Context.class);
		GatewayRequestImpl request = new GatewayRequestImpl();
		request.setHttpMethod("PUT");
		request.setPath("/inject-gateway-request");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayRequest(same(request));
	}

	@Test
	public void testGatewayRequestContextInjection() {
		Context context = mock(Context.class);
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
		Context context = mock(Context.class);
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
	public void testDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("GET");
		request.setPath("/default");
		request.setHeaders(ImmutableMap.of("Accept", "application/json"));
		GatewayResponse response = handler.handleRequest(request, context);
		Map<String, String> headers = ImmutableMap.of("Content-Type", "application/json");
		assertEquals(new GatewayResponse("{\"value\":\"default\"}", headers, Status.OK), response);
	}

	@Test
	public void testNonDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("GET");
		request.setPath("/nondefault");
		request.setHeaders(ImmutableMap.of("Accept", "application/json"));
		GatewayResponse response = handler.handleRequest(request, context);
		assertEquals(301, response.getStatusCode());
		assertEquals(ImmutableMap.of("Content-Type", "application/json"), response.getHeaders());
		assertEquals("{\"value\":\"nonDefault\"}", response.getBody());
	}

	@Test
	public void testCustomNonDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("GET");
		request.setPath("/customnondefault");
		request.setHeaders(ImmutableMap.of("Accept", "application/json"));
		GatewayResponse response = handler.handleRequest(request, context);
		assertEquals(200, response.getStatusCode());
		assertEquals(ImmutableMap.of("Content-Type", "application/json"), response.getHeaders());
		assertEquals("{\"value\":\"nonCustomDefault\"}", response.getBody());
	}

	@Test
	public void testRequestBodyPassed() throws JsonProcessingException {
		Context context = mock(Context.class);
		GatewayRequestImpl request = new GatewayRequestImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setRequestContext(requestContext);
		request.setHttpMethod("POST");
		request.setPath("/requestbody");
		request.setHeaders(ImmutableMap.of("Content-Type", "application/json"));
		request.setBody(new ObjectMapper().writeValueAsString(new Entity("requestBody")));
		handler.handleRequest(request, context);
		verify(testService).requestBody(new Entity("requestBody"));
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

		@Path("/default")
		@GET
		public Response defaultResponse() {
			return Response.ok(new Entity("default")).build();
		}

		@Path("/nondefault")
		@GET
		public Response nonDefaultResponse() {
			return Response.status(301).entity(new Entity("nonDefault")).build();
		}

		@Path("/customnondefault")
		@GET
		public Response customNonDefaultResponse() {
			return Response.ok(new Entity("nonCustomDefault")).build();
		}

		@Path("/requestbody")
		@POST
		@Consumes("application/json")
		public void requestBody(Entity entity) {
			service.requestBody(entity);
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectGatewayRequest(GatewayRequest request);
		void injectGatewayRequestContext(GatewayRequestContext requestContext);
		void injectGatewayIdentity(GatewayIdentity identity);
		void requestBody(Entity entity);
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
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Entity other = (Entity) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
	}

	public static class GatewayRequestObjectHandlerImpl extends GatewayRequestObjectHandler {
	}
}
