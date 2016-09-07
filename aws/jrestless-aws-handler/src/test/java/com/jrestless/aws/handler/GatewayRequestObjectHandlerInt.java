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
import static org.junit.Assert.fail;
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
import com.jrestless.aws.GatewayRequestContext;
import com.jrestless.aws.GatewayResourceConfig;
import com.jrestless.aws.io.GatewayAdditionalResponseException;
import com.jrestless.aws.io.GatewayDefaultResponse;
import com.jrestless.aws.io.GatewayRequest;
import com.jrestless.aws.io.GatewayRequestContextImpl;
import com.jrestless.core.container.dpi.InstanceBinder;

import io.swagger.annotations.ApiOperation;

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
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("DELETE");
		requestContext.setResourcePath("/");
		handler.handleRequest(request, context);
		verify(testService).injectLambdaContext(context);
	}

	@Test
	public void testGatewayContextInjection() {
		Context context = mock(Context.class);
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("PUT");
		requestContext.setResourcePath("/");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayContext(same(requestContext));
	}

	@Test
	public void testDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("GET");
		requestContext.setResourcePath("/default");
		request.setHeaderParams(ImmutableMap.of("Accept", "application/json"));
		GatewayDefaultResponse response = handler.handleRequest(request, context);
		Map<String, String> headers = ImmutableMap.of("X-Is-Default-Response", "1", "Content-Type", "application/json");
		assertEquals(new GatewayDefaultResponse("{\"value\":\"default\"}", headers, Status.OK), response);
	}

	@Test
	public void testNonDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("GET");
		requestContext.setResourcePath("/nondefault");
		request.setHeaderParams(ImmutableMap.of("Accept", "application/json"));
		try {
			handler.handleRequest(request, context);
			fail("expected response to be returned as exception");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"301\",\"body\":\"{\\\"value\\\":\\\"nonDefault\\\"}\"}", gare.getMessage());
		}
	}

	@Test
	public void testCustomNonDefaultResponse() {
		Context context = mock(Context.class);
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("GET");
		requestContext.setResourcePath("/customnondefault");
		request.setHeaderParams(ImmutableMap.of("Accept", "application/json"));
		try {
			handler.handleRequest(request, context);
			fail("expected response to be returned as exception");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"200\",\"body\":\"{\\\"value\\\":\\\"nonCustomDefault\\\"}\"}", gare.getMessage());
		}
	}

	@Test
	public void testRequestBodyPassed() throws JsonProcessingException {
		Context context = mock(Context.class);
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		request.setContext(requestContext);
		requestContext.setHttpMethod("POST");
		requestContext.setResourcePath("/requestbody");
		request.setHeaderParams(ImmutableMap.of("Content-Type", "application/json"));
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

		@PUT
		public Response injectGatewayContext(@javax.ws.rs.core.Context GatewayRequestContext context) {
			service.injectGatewayContext(context);
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
		@ApiOperation(value = "", code = 301)
		public Response customNonDefaultResponse() {
			return Response.ok(new Entity("nonCustomDefault")).build();
		}

		@Path("/requestbody")
		@POST
		@Consumes("application/json")
		@ApiOperation(value = "", code = 204)
		public void requestBody(Entity entity) {
			service.requestBody(entity);
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectGatewayContext(GatewayRequestContext context);
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
