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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.core.container.dpi.InstanceBinder;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequestImpl;

public class SimpleRequestHandlerIntTest {

	private static final URI BASE_URI = URI.create("/");
	private static final ByteArrayInputStream EMPTY_ENTITY_STREAM = new ByteArrayInputStream(new byte[0]);
	private static final List<String> APPLICATION_JSON_HEADER = Collections.singletonList("application/json");
	private static final Map<String, List<String>> JSON_CONTENT_HEADER = ImmutableMap.of("Content-Type", APPLICATION_JSON_HEADER);
	private SimpleRequestHandler<JRestlessContainerRequest, SimpleContainerResponse> handler;
	private TestService testService;

	@Before
	public void setup() {
		ResourceConfig config = new ResourceConfig();
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		handler = new SimpleRequestHandlerImpl();
		handler.init(config);
		handler.start();
	}

	private JRestlessContainerRequest createRequest(String requestUri, String httpMethod, String body, Map<String, List<String>> headers) {
		InputStream is = EMPTY_ENTITY_STREAM;
		if (body != null) {
			is = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		}
		if (headers == null) {
			headers = new HashMap<>();
		}
		return new JRestlessContainerRequestImpl(BASE_URI, URI.create(requestUri), httpMethod, is, headers);
	}

	private JRestlessContainerRequest createJsonRequest(String requestUri, String httpMethod, String body) {
		return createRequest(requestUri, httpMethod, body, ImmutableMap.of("Accept", APPLICATION_JSON_HEADER));
	}

	@Test
	public void testDefaultResponse() {
		JRestlessContainerRequest request = createJsonRequest("/default", "GET", null);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(new SimpleContainerResponse(Status.OK, "{\"value\":\"default\"}", JSON_CONTENT_HEADER), response);
	}

	@Test
	public void testResponseStatus() {
		JRestlessContainerRequest request = createJsonRequest("/moved", "GET", null);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(Status.MOVED_PERMANENTLY, response.getStatusType());
	}

	@Test
	public void testResponseType() {
		JRestlessContainerRequest request = createJsonRequest("/responsetype", "GET", null);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals("{\"value\":\"value\"}", response.getBody());

		request = createRequest("/responsetype", "GET", null, ImmutableMap.of("Accept", Collections.singletonList("application/xml")));
		response = handler.delegateRequest(request);
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><entity><value>value</value></entity>",
				response.getBody());
	}

	@Test
	public void testHttpStatus() {
		JRestlessContainerRequest request = createJsonRequest("/httpmethod", "GET", null);
		handler.delegateRequest(request);
		verify(testService).httpMethod("GET");

		request = createJsonRequest("/httpmethod", "DELETE", null);
		handler.delegateRequest(request);
		verify(testService).httpMethod("DELETE");
	}

	@Test
	public void testResponseHeaders() {
		JRestlessContainerRequest request = createJsonRequest("/responseheaders", "GET", null);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(ImmutableMap.of("hk0", Collections.singletonList("hv0_0"),
				"hk1", ImmutableList.of("hv0_1", "hv1_1")),
				response.getHeaders());
	}

	@Test
	public void testRequestBodyInjection() throws JsonProcessingException {
		Map<String, List<String>> headers = ImmutableMap.of("Accept", APPLICATION_JSON_HEADER, "Content-Type", APPLICATION_JSON_HEADER);
		JRestlessContainerRequest request = createRequest("/requestbody", "POST", new ObjectMapper().writeValueAsString(new Entity("requestBody")), headers);
		handler.delegateRequest(request);
		verify(testService).requestBody(new Entity("requestBody"));
	}

	@Test
	public void testRequestParamInjection() {
		JRestlessContainerRequest request = createJsonRequest("/requestparam/someparam", "GET", null);
		handler.delegateRequest(request);
		verify(testService).requestParam("someparam");
	}

	@Test
	public void testQueryParamInjection() {
		JRestlessContainerRequest request = createJsonRequest("/queryparams?q1=v1&q0=v0", "GET", null);
		handler.delegateRequest(request);
		verify(testService).queryParams("v0", "v1");
	}

	@Test
	public void testRequestHeadersInjection() {
		Map<String, List<String>> headers = ImmutableMap.of("hk0", Collections.singletonList("hv0"));
		JRestlessContainerRequest request = createRequest("/requestheaders", "GET", null, headers);
		handler.delegateRequest(request);
		verify(testService).requestHeaders(headers);
	}

	@Path("/")
	public static class TestResource {

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
		}

		@Path("/responsetype")
		@GET
		@Consumes({ "application/json", "application/xml" })
		public Response responseType() {
			return Response.ok(new Entity("value")).build();
		}

		@Path("/httpmethod")
		@GET
		public void getRequest() {
			service.httpMethod("GET");
		}

		@Path("/httpmethod")
		@DELETE
		public void deleteRequest() {
			service.httpMethod("DELETE");
		}

		@Path("/default")
		@GET
		public Response defaultResponse() {
			return Response.ok(new Entity("default")).build();
		}

		@Path("/moved")
		@GET
		public Response nonDefaultResponse() {
			return Response.status(301).entity(new Entity("nonDefault")).build();
		}

		@Path("/responseheaders")
		@GET
		public Response headers() {
			return Response.noContent()
					.header("hk0", "hv0_0")
					.header("hk1", "hv0_1")
					.header("hk1", "hv1_1")
					.build();
		}

		@Path("/requestbody")
		@POST
		@Consumes("application/json")
		public void requestBody(Entity entity) {
			service.requestBody(entity);
		}

		@Path("/requestparam/{param}")
		@GET
		public void requestParam(@PathParam("param") String param) {
			service.requestParam(param);
		}

		@Path("/requestheaders")
		@GET
		public void requestHeaders(@Context HttpHeaders headers) {
			service.requestHeaders(headers.getRequestHeaders());
		}

		@Path("/queryparams")
		@GET
		public void requestHeaders(@QueryParam("q0") String q0, @QueryParam("q1") String q1) {
			service.queryParams(q0, q1);
		}
	}

	public static interface TestService {
		void queryParams(String q0, String q1);
		void httpMethod(String httpMethod);
		void requestBody(Entity entity);
		void requestParam(String param);
		void requestHeaders(Map<String, List<String>> headers);
	}

	private static class SimpleRequestHandlerImpl extends SimpleRequestHandler<JRestlessContainerRequest, SimpleContainerResponse> {

		@Override
		public SimpleResponseWriter<SimpleContainerResponse> createResponseWriter() {
			return new SimpleResponseWriter<SimpleContainerResponse>() {

				private SimpleContainerResponse response = createInternalServerErrorResponse();

				@Override
				public OutputStream getEntityOutputStream() {
					return new ByteArrayOutputStream();
				}

				@Override
				public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
						OutputStream entityOutputStream) throws IOException {
					response = new SimpleContainerResponse(statusType, entityOutputStream.toString(), headers);
				}

				@Override
				public SimpleContainerResponse getResponse() {
					return response;
				}
			};
		}

		@Override
		public JRestlessContainerRequest createContainerRequest(JRestlessContainerRequest request) {
			return request;
		}

		@Override
		public SimpleContainerResponse createInternalServerErrorResponse() {
			return new SimpleContainerResponse(Status.INTERNAL_SERVER_ERROR, null, new HashMap<>());
		}

	}

	@XmlRootElement
	public static class Entity {
		private String value;

		Entity() {
		}

		@JsonCreator
		public Entity(@JsonProperty("value") String value) {
			this.value = value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@XmlElement
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
