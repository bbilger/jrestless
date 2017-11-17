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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest;
import com.jrestless.openwhisk.webaction.io.WebActionRequest;
import com.jrestless.openwhisk.webaction.io.WebActionRequestBuilder;
import com.jrestless.test.IOUtils;

public class WebActionRequestHandlerTest {

	private static final Type WEB_ACTION_REQUEST_TYPE = (new GenericType<Ref<WebActionRequest>>() { }).getType();
	private static final Gson GSON = new GsonBuilder().create();

	private final WebActionRequestHandler handler = new WebActionRequestHandler() {
		@Override
		protected JsonObject createJsonResponse(String body, Map<String, String> responseHeaders,
				StatusType statusType) {
			return createResponse(body, responseHeaders, statusType);
		}
	};
	private JRestlessHandlerContainer<JRestlessContainerRequest> container;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		handler.init(container);
		handler.start();
	}

	@After
	public void tearDown() {
		handler.stop();
	}

//	@Test
//	public void delegateJsonRequest_NullRequestGiven_ShouldFailWithInternalServerError() {
//		JsonObject actualResponse = handler.delegateJsonRequest(null);
//		JsonObject expectedResponse = createResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR);
//		assertEquals(expectedResponse, actualResponse);
//	}
//
//	@Test
//	public void delegateJsonRequest_RequestWithNoMethodGiven_ShouldFailWithInternalServerError() {
//		JsonObject request = new WebActionRequestBuilder().buildJson();
//		JsonObject actualResponse = handler.delegateJsonRequest(request);
//		JsonObject expectedResponse = createResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR);
//		assertEquals(expectedResponse, actualResponse);
//	}
//
//	@Test
//	public void delegateJsonRequest_MinimalRequestGiven_ShouldDelegateRequest() {
//		JsonObject request = new WebActionRequestBuilder()
//				.setHttpMethod("GET")
//				.buildJson();
//		JsonObject actualResponse = handler.delegateJsonRequest(request);
//		JsonObject expectedResponse = createResponse("", Collections.emptyMap(), Status.NOT_FOUND);
//		assertEquals(expectedResponse, actualResponse);
//	}
//
//	@Test
//	public void delegateJsonRequest_RequestWithNullHeadersGiven_ShouldDelegateRequest() {
//		JsonObject request = new WebActionRequestBuilder()
//				.setHttpMethod("GET")
//				.setHeaders(null)
//				.buildJson();
//		JsonObject actualResponse = handler.delegateJsonRequest(request);
//		JsonObject expectedResponse = createResponse("", Collections.emptyMap(), Status.NOT_FOUND);
//		assertEquals(expectedResponse, actualResponse);
//	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NullRequestGiven_ShouldFailWithNpe() {
		handler.createContainerRequest(null);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NullMethodGiven_ShouldFailWithNpe() {
		WebActionRequest request = minimalRequestBuilder()
				.setHttpMethod(null)
				.build();
		handler.createContainerRequest(request);
	}

	@Test
	public void createContainerRequest_NullHeadersGiven_ShouldMapToEmptyHeaders() {
		WebActionRequest request = minimalRequestBuilder()
				.setHeaders(null)
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(Collections.emptyMap(), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullBodyGiven_ShouldMapToEmptyInputStream() {
		WebActionRequest request = minimalRequestBuilder()
				.setBody(null)
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertArrayEquals(new byte[0], IOUtils.toBytes(containerRequest.getEntityStream()));
	}

	@Test
	public void createContainerRequest_BodyGiven_ShouldMapToInputStream() {
		WebActionRequest request = minimalRequestBuilder()
				.setBody("some body")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertArrayEquals("some body".getBytes(), IOUtils.toBytes(containerRequest.getEntityStream()));
	}

	@Test
	public void createContainerRequest_HeadersGiven_ShouldExpandHeaders() {
		Map<String, String> requestHeaders = ImmutableMap.of("hk0", "hv0", "hk1", "hv1");
		WebActionRequest request = minimalRequestBuilder()
				.setHeaders(requestHeaders)
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("hk0", Collections.singletonList("hv0"), "hk1", Collections.singletonList("hv1")),
				containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullPathGiven_ShouldMapToRootRequestPath() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath(null)
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void createContainerRequest_EmptyPathGiven_ShouldMapToRootRequestPath() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath("")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void createContainerRequest_NonAbsolutePathGiven_ShouldPrependSlashToRequestPath() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath("path")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/path"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void createContainerRequest_AbsolutePathGiven_ShouldMapRequestPathAsIs() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath("/path")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/path"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void createContainerRequest_PathAndEmpyQueryGiven_ShouldNotAppendQuery() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath("/path")
				.setQuery("")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/path"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void createContainerRequest_PathAndQueryGiven_ShouldNotAppendQuery() {
		WebActionRequest request = minimalRequestBuilder()
				.setPath("/path")
				.setQuery("a=b")
				.build();
		JRestlessContainerRequest containerRequest = handler.createContainerRequest(request);
		assertEquals(URI.create("/path?a=b"), containerRequest.getRequestUri());
		assertEquals(URI.create("/"), containerRequest.getBaseUri());
	}

	@Test
	public void onRequestFailure_ShouldCreate500() {
		JsonObject response = handler.onRequestFailure(null, null, null);
		assertEquals(createResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR), response);
	}

	@Test
	public void delegateJsonRequest_InvalidJsonGiven_ShouldFailWith500() {
		JsonObject request = minimalRequestBuilder()
				.buildJson();
		request.addProperty("__ow_headers", true);
		JsonObject response = handler.delegateJsonRequest(request);
		assertEquals(createResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR), response);
	}

	@Test
	public void delegateJsonRequest_ValidRequestAndReferencesGiven_ShouldSetReferencesOnRequestInitialization() {

		WebActionRequestBuilder requestBuilder = new WebActionRequestBuilder()
				.setHttpMethod("GET")
				.setPath("/");

		JsonObject jsonRequest = requestBuilder.buildJson();
		DefaultWebActionRequest request = requestBuilder.build();

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(jsonRequest);

		@SuppressWarnings("unchecked")
		Ref<WebActionRequest> gatewayRequestRef = mock(Ref.class);

		InjectionManager injectionManager = mock(InjectionManager.class);
		when(injectionManager.getInstance(WEB_ACTION_REQUEST_TYPE)).thenReturn(gatewayRequestRef);

		requestScopedInitializer.initialize(injectionManager);

		verify(gatewayRequestRef).set(request);
	}

	@Test
	public void delegateRequest_ValidRequestAndNoReferencesGiven_ShouldNotFailOnRequestInitialization() {
		JsonObject jsonRequest = new WebActionRequestBuilder()
				.setHttpMethod("GET")
				.setPath("/")
				.buildJson();

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(jsonRequest);

		InjectionManager injectionManager = mock(InjectionManager.class);
		requestScopedInitializer.initialize(injectionManager);
	}

	@SuppressWarnings("unchecked")
	private RequestScopedInitializer getSetRequestScopedInitializer(JsonObject request) {
		ArgumentCaptor<Consumer<ContainerRequest>> containerEnhancerCaptor = ArgumentCaptor.forClass(Consumer.class);
		handler.delegateJsonRequest(request);
		verify(container).handleRequest(any(), any(), any(), containerEnhancerCaptor.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCaptor.getValue().accept(containerRequest);

		ArgumentCaptor<RequestScopedInitializer> requestScopedInitializerCaptor = ArgumentCaptor.forClass(RequestScopedInitializer.class);

		verify(containerRequest).setRequestScopedInitializer(requestScopedInitializerCaptor.capture());

		return requestScopedInitializerCaptor.getValue();
	}

	private static JsonObject createResponse(String body, Map<String, String> headers, StatusType status) {
		return createResponse(body, headers, status.getStatusCode());
	}

	private static JsonObject createResponse(String body, Map<String, String> headers, int statusCode) {
		JsonObject response = new JsonObject();
		response.addProperty("body", body);
		response.addProperty("statusCode", statusCode);
		response.add("headers", GSON.toJsonTree(headers, Map.class));
		response.addProperty("someAdditionalTestProperty", true);
		return response;
	}

	private static WebActionRequestBuilder minimalRequestBuilder() {
		return new WebActionRequestBuilder()
				.setHttpMethod("GET");
	}
}
