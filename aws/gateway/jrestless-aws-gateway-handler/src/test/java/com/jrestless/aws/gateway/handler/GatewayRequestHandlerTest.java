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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.core.Response.Status;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.AwsFeature;
import com.jrestless.aws.gateway.GatewayFeature;
import com.jrestless.aws.gateway.io.DefaultGatewayRequest;
import com.jrestless.aws.gateway.io.GatewayBinaryResponseCheckFilter;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.handler.SimpleRequestHandler.SimpleResponseWriter;
import com.jrestless.core.container.io.JRestlessContainerRequest;

public class GatewayRequestHandlerTest {

	private JRestlessHandlerContainer<JRestlessContainerRequest> container;
	private GatewayRequestHandlerImpl gatewayHandler;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		gatewayHandler = spy(new GatewayRequestHandlerImpl());
		gatewayHandler.doInit(container);
		gatewayHandler.doStart();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void delegateRequest_ValidRequestAndReferencesGiven_ShouldSetReferencesOnRequestInitialization() {
		Context context = mock(Context.class);
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setPath("/");
		request.setHttpMethod("GET");

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, request);

		Ref<GatewayRequest> gatewayRequestRef = mock(Ref.class);
		Ref<Context> contextRef = mock(Ref.class);

		ServiceLocator serviceLocator = mock(ServiceLocator.class);
		when(serviceLocator.getService(GatewayFeature.GATEWAY_REQUEST_TYPE)).thenReturn(gatewayRequestRef);
		when(serviceLocator.getService(AwsFeature.CONTEXT_TYPE)).thenReturn(contextRef);

		requestScopedInitializer.initialize(serviceLocator);

		verify(gatewayRequestRef).set(request);
		verify(contextRef).set(context);
	}

	@Test
	public void delegateRequest_ValidRequestAndNoReferencesGiven_ShouldNotFailOnRequestInitialization() {

		Context context = mock(Context.class);
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setPath("/");
		request.setHttpMethod("GET");

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, request);

		ServiceLocator serviceLocator = mock(ServiceLocator.class);
		requestScopedInitializer.initialize(serviceLocator);
	}



	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RequestScopedInitializer getSetRequestScopedInitializer(Context context, GatewayRequest request) {
		GatewayRequestAndLambdaContext reqAndContext = new GatewayRequestAndLambdaContext(request, context);
		ArgumentCaptor<Consumer> containerEnhancerCaptor = ArgumentCaptor.forClass(Consumer.class);
		gatewayHandler.doDelegateRequest(reqAndContext);
		verify(container).handleRequest(any(), any(), any(), containerEnhancerCaptor.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCaptor.getValue().accept(containerRequest);

		ArgumentCaptor<RequestScopedInitializer> requestScopedInitializerCaptor = ArgumentCaptor.forClass(RequestScopedInitializer.class);

		verify(containerRequest).setRequestScopedInitializer(requestScopedInitializerCaptor.capture());

		return requestScopedInitializerCaptor.getValue();
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoPathGiven_ShouldThrowNpe() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setPath(null);
		gatewayHandler.createContainerRequest(request);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoHttpMethodGiven_ShouldThrowNpe() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setHttpMethod(null);
		gatewayHandler.createContainerRequest(request);
	}

	@Test
	public void createContainerRequest_NoBodyGiven_ShouldUseEmptyBaos() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertEquals("", toString((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_BodyGiven_ShouldUseBody() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setBody("abc");
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertEquals("abc", toString((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_HttpMethodGiven_ShouldUseHttpMethod() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setHttpMethod("POST");
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals("POST", containerRequest.getHttpMethod());
	}

	@Test
	public void createContainerRequest_PathWithNoQueryParamsGiven_ShouldUsePathAsRequestUri() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setPath("/abc");
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_PathWithOneQueryParamsGiven_ShouldUseQueryParamsInRequestUri() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setPath("/abc");
		((DefaultGatewayRequest) request.getGatewayRequest()).setQueryStringParameters(ImmutableMap.of("a_k", "a_v"));
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc?a_k=a_v"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_PathWithMultipleQueryParamsGiven_ShouldUseQueryParamsInRequestUri() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setPath("/abc");
		((DefaultGatewayRequest) request.getGatewayRequest()).setQueryStringParameters(ImmutableMap.of("a_k", "a_v", "b_k", "b_v"));
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc?a_k=a_v&b_k=b_v"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_HeadersGiven_ShouldUseHeaders() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		((DefaultGatewayRequest) request.getGatewayRequest()).setHeaders(ImmutableMap.of("a_k", "a_v", "b_k", "b_v"));
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("a_k", singletonList("a_v"), "b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullHeaderKeyGiven_ShouldFilterHeader() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put(null, "a_v");
		headers.put("b_k", "b_v");
		((DefaultGatewayRequest) request.getGatewayRequest()).setHeaders(headers);
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullHeaderValueGiven_ShouldFilterHeader() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put("a_k", null);
		headers.put("b_k", "b_v");
		((DefaultGatewayRequest) request.getGatewayRequest()).setHeaders(headers);
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_CommaSeparatedHeaderValueGiven_ShouldNotSpreadHeader() {
		GatewayRequestAndLambdaContext request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put("a_k", "a_v0,a_v1");
		((DefaultGatewayRequest) request.getGatewayRequest()).setHeaders(headers);
		JRestlessContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("a_k", singletonList("a_v0,a_v1")), containerRequest.getHeaders());
	}

	@Test
	public void testResponseWriterFiltersInternalBinaryHeader() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("a_k", Collections.singletonList("a_v"));
		headers.put(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE, Collections.singletonList("true"));
		headers.put("b_k", Collections.singletonList("b_v"));
		SimpleResponseWriter<GatewayResponse> responseWriter = gatewayHandler.createResponseWriter(null);
		responseWriter.writeResponse(Status.OK, headers, new ByteArrayOutputStream());
		assertEquals(ImmutableMap.of("a_k", "a_v", "b_k", "b_v"), responseWriter.getResponse().getHeaders());
	}

	@Test
	public void testResponseWriterSetsBase64EncodedFlagIfExactlyOneBinaryHeaderSetToTrue() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE, Collections.singletonList("true"));
		SimpleResponseWriter<GatewayResponse> responseWriter = gatewayHandler.createResponseWriter(null);
		responseWriter.writeResponse(Status.OK, headers, new ByteArrayOutputStream());
		assertTrue(responseWriter.getResponse().isIsBase64Encoded());
	}

	@Test
	public void testResponseWriterDoesntSetBase64EncodedFlagIfMultipleBinaryHeadersSet() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE, ImmutableList.of("true", "true"));
		SimpleResponseWriter<GatewayResponse> responseWriter = gatewayHandler.createResponseWriter(null);
		responseWriter.writeResponse(Status.OK, headers, new ByteArrayOutputStream());
		assertFalse(responseWriter.getResponse().isIsBase64Encoded());
	}

	@Test
	public void testResponseWriterDoesntSetBase64EncodedFlagIfNoBinaryHeadersSet() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		SimpleResponseWriter<GatewayResponse> responseWriter = gatewayHandler.createResponseWriter(null);
		responseWriter.writeResponse(Status.OK, headers, new ByteArrayOutputStream());
		assertFalse(responseWriter.getResponse().isIsBase64Encoded());
	}

	@Test
	public void testResponseWriterDoesntSetBase64EncodedFlagIfBinaryHeaderSetToFalse() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE, Collections.singletonList("false"));
		SimpleResponseWriter<GatewayResponse> responseWriter = gatewayHandler.createResponseWriter(null);
		responseWriter.writeResponse(Status.OK, headers, new ByteArrayOutputStream());
		assertFalse(responseWriter.getResponse().isIsBase64Encoded());
	}

	private GatewayRequestAndLambdaContext createMinimalRequest() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setPath("/");
		request.setHttpMethod("GET");
		return new GatewayRequestAndLambdaContext(request, null);
	}

	public static String toString(ByteArrayInputStream bais) {
		int size = bais.available();
		char[] chars = new char[size];
		byte[] bytes = new byte[size];

		bais.read(bytes, 0, size);
		for (int i = 0; i < size;)
			chars[i] = (char) (bytes[i++] & 0xff);

		return new String(chars);
	}
	private static class GatewayRequestHandlerImpl extends GatewayRequestHandler {
		void doStart() {
			start();
		}
		void doInit(JRestlessHandlerContainer<JRestlessContainerRequest> container) {
			init(container);
		}
		void doDelegateRequest(GatewayRequestAndLambdaContext reqAndContext) {
			delegateRequest(reqAndContext);
		}
	}
}
