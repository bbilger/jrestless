/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.openwhisk.webaction.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonObject;
import com.jrestless.core.container.dpi.InstanceBinder;
import com.jrestless.openwhisk.webaction.WebActionHttpConfig;
import com.jrestless.openwhisk.webaction.io.WebActionBase64ReadInterceptor;
import com.jrestless.openwhisk.webaction.io.WebActionHttpResponseBuilder;
import com.jrestless.openwhisk.webaction.io.WebActionRequest;
import com.jrestless.openwhisk.webaction.io.WebActionRequestBuilder;

public class WebActionHttpRequestHandlerIntTest {

	private WebActionHttpRequestHandler handler;
	private TestService testService;

	@Before
	public void setup() {
		testService = mock(TestService.class);
		handler = new WebActionHttpRequestHandler();
		ResourceConfig config = new WebActionHttpConfig();
		config.register(WebActionBase64ReadInterceptor.class);
		config.register(new InstanceBinder.Builder().addInstance(testService, TestService.class).build());
		config.register(TestResource.class);
		handler.init(config);
		handler.start();
	}

	@After
	public void tearDown() {
		handler.stop();
	}

	@Test
	public void testGetText() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("get-text")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBody("test") // NOT base64 encoded!
				.setContentType(MediaType.TEXT_PLAIN_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void testGetJson() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("get-json")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBodyBase64Encoded("{\"value\":\"test\"}") // base64 encoded
				.setContentType(MediaType.APPLICATION_JSON_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void testGetBinary() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("get-binary")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBodyBase64Encoded("binary") // base64 encoded
				.setContentType("image/png")
				.build();
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void testPostStringAndGetString() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.POST)
				.setPath("post-string-get-string")
				.setBodyBase64Encoded("someBody")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBody("someBody")
				.setContentType(MediaType.TEXT_PLAIN_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
		verify(testService).injectedStringArg("someBody");
	}

	@Test
	public void testPostStringAndGetJson() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.POST)
				.setPath("post-string-get-json")
				.setBodyBase64Encoded("someBody")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBodyBase64Encoded("{\"value\":\"someBody\"}")
				.setContentType(MediaType.APPLICATION_JSON_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
		verify(testService).injectedStringArg("someBody");
	}

	@Test
	public void testPostJsonAndGetJson() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.POST)
				.setPath("post-json-get-json")
				.setBodyBase64Encoded("{\"value\":\"123\"}")
				.setContentType(MediaType.APPLICATION_JSON)
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBodyBase64Encoded("{\"value\":\"123\"}")
				.setContentType(MediaType.APPLICATION_JSON)
				.build();
		assertEquals(expectedResponse, actualResponse);
		verify(testService).injectedEntity(new Entity("123"));
	}

	@Test
	public void testPostJsonAndGetString() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.POST)
				.setPath("post-json-get-string")
				.setBodyBase64Encoded("{\"value\": \"123\"}")
				.setContentType(MediaType.APPLICATION_JSON)
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBody("123")
				.setContentType(MediaType.TEXT_PLAIN_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
		verify(testService).injectedEntity(new Entity("123"));
	}

	@Test
	public void testPostBinary() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.POST)
				.setPath("post-binary")
				.setBodyBase64Encoded("binary")
				.setContentType("image/png")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		JsonObject expectedResponse = new WebActionHttpResponseBuilder()
				.setBody("binary")
				.setContentType(MediaType.TEXT_PLAIN_TYPE)
				.build();
		assertEquals(expectedResponse, actualResponse);
		verify(testService).injectedStringArg("binary");
	}

	@Test
	public void testWebActionRequestInjection() {
		WebActionRequestBuilder requestBuilder = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("inject-webaction-request");
		JsonObject request = requestBuilder.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		assertEquals(WebActionHttpResponseBuilder.noContent(), actualResponse);
		verify(testService).injectedWebActionRequest(requestBuilder.build());
	}

	@Test
	public void testWebActionRequestMemberInjection() {
		WebActionRequestBuilder requestBuilder0 = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("inject-webaction-request-member0");
		WebActionRequestBuilder requestBuilder1 = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("inject-webaction-request-member1");

		JsonObject request0 = requestBuilder0.buildJson();
		JsonObject request1 = requestBuilder1.buildJson();
		JsonObject actualResponse0 = handler.delegateJsonRequest(request0);
		JsonObject actualResponse1 = handler.delegateJsonRequest(request1);
		assertEquals(WebActionHttpResponseBuilder.noContent(), actualResponse0);
		assertEquals(WebActionHttpResponseBuilder.noContent(), actualResponse1);

		InOrder inOrder = Mockito.inOrder(testService);
		// we cannot use the object since the proxy is "Not inside a request scope", anymore
		inOrder.verify(testService).injectedStringArg(requestBuilder0.build().getPath());
		inOrder.verify(testService).injectedStringArg(requestBuilder1.build().getPath());
	}

	@Test
	public void testQueryParameters() {
		JsonObject request = new WebActionRequestBuilder()
				.setHttpMethod(HttpMethod.GET)
				.setPath("/inject-query-params")
				.setQuery("q1=1&q2=2")
				.buildJson();
		JsonObject actualResponse = handler.delegateJsonRequest(request);
		assertEquals(WebActionHttpResponseBuilder.noContent(), actualResponse);
		InOrder inOrder = Mockito.inOrder(testService);
		inOrder.verify(testService).injectedStringArg("1");
		inOrder.verify(testService).injectedStringArg("2");
	}

	@Path("/")
	@Singleton // singleton in order to test proxies
	public static class TestResource {

		@Context
		private WebActionRequest webActionRequestMember;

		private final TestService testService;

		@Inject
		public TestResource(TestService testService) {
			this.testService = testService;
		}

		@GET
		@Path("/get-text")
		@Produces(MediaType.TEXT_PLAIN)
		public Response getText() {
			return Response.ok("test").build();
		}

		@GET
		@Path("/get-json")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getJson() {
			return Response.ok(new Entity("test")).build();
		}

		@GET
		@Path("/get-binary")
		@Produces("image/png")
		public byte[] getBinary() {
			return "binary".getBytes();
		}

		@POST
		@Path("/post-string-get-string")
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.TEXT_PLAIN)
		public Response postStringGetString(String body) {
			testService.injectedStringArg(body);
			return Response.ok(body).build();
		}

		@POST
		@Path("/post-string-get-json")
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.APPLICATION_JSON)
		public Response postStringGetJson(String body) {
			testService.injectedStringArg(body);
			return Response.ok(new Entity(body)).build();
		}

		@POST
		@Path("/post-json-get-json")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON)
		public Response postJsonGetJson(Entity entity) {
			testService.injectedEntity(entity);
			return Response.ok(entity).build();
		}

		@POST
		@Path("/post-json-get-string")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		public Response postJsonGetString(Entity entity) {
			testService.injectedEntity(entity);
			return Response.ok(entity.getValue()).build();
		}

		@POST
		@Path("/post-binary")
		@Consumes("image/png")
		@Produces(MediaType.TEXT_PLAIN)
		public Response postJsonGetString(byte[] binary) {
			testService.injectedStringArg(new String(binary));
			return Response.ok(new String(binary)).build();
		}

		@GET
		@Path("/inject-webaction-request")
		public void injectWebActionRequest(@Context WebActionRequest request) {
			testService.injectedWebActionRequest(request);
		}

		@GET
		@Path("/inject-webaction-request-member0")
		public void injectWebActionRequestAsMember0() {
			testService.injectedStringArg(webActionRequestMember.getPath());
		}

		@GET
		@Path("/inject-webaction-request-member1")
		public void injectWebActionRequestAsMember1() {
			testService.injectedStringArg(webActionRequestMember.getPath());
		}

		@GET
		@Path("/inject-query-params")
		public void injectQuerieParams(@QueryParam("q1") String q1, @QueryParam("q2") String q2) {
			testService.injectedStringArg(q1);
			testService.injectedStringArg(q2);
		}
	}

	public static interface TestService {
		void injectedWebActionRequest(WebActionRequest webActionRequest);
		void injectedStringArg(String arg);
		void injectedEntity(Entity entity);
	}

	private static class Entity {
		private final String value;
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
}
