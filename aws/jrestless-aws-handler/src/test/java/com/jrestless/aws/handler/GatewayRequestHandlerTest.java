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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response.Status;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.GatewayRequest;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponse;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponseWriter;
import com.jrestless.aws.io.GatewayRequestImpl;
import com.jrestless.aws.io.GatewayResponse;
import com.jrestless.core.container.JRestlessHandlerContainer;

public class GatewayRequestHandlerTest {

	private JRestlessHandlerContainer<GatewayContainerRequest> container;
	private GatewayRequestHandler gatewayHandler;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		gatewayHandler = spy(new GatewayRequestHandlerImpl());
		gatewayHandler.init(container);
		gatewayHandler.start();
	}

	@Test(expected = NullPointerException.class)
	public void init0_NullAppGiven_ShouldThrowNpe() {
		new GatewayRequestHandlerImpl().init((Application) null);
	}

	@Test(expected = NullPointerException.class)
	public void init1_NullAppGiven_ShouldThrowNpe() {
		new GatewayRequestHandlerImpl().init((Application) null, null, mock(ServiceLocator.class));
	}

	@Test(expected = NullPointerException.class)
	public void init1_NullServiceLocatorGiven_ShouldThrowNpe() {
		new GatewayRequestHandlerImpl().init(mock(Application.class), null, null);
	}

	@Test(expected = NullPointerException.class)
	public void init2_NullHandlerContainerGiven_ShouldThrowNpe() {
		new GatewayRequestHandlerImpl().init((JRestlessHandlerContainer<GatewayContainerRequest>) null);
	}

	@Test(expected = IllegalStateException.class)
	public void init2_MultiInit_ShouldThrowIse() {
		gatewayHandler.init(container);
	}

	@Test(expected = IllegalStateException.class)
	public void start_NotInitialized_ShouldThrowIse() {
		new GatewayRequestHandlerImpl().start();
	}

	@Test(expected = IllegalStateException.class)
	public void start_MultiStart_ShouldThrowIse() {
		gatewayHandler.start();
	}

	@Test
	public void delegateRequest_NotStarted_ShouldReturnInternalServerError() {
		GatewayResponse response = new GatewayRequestHandlerImpl().delegateRequest(mock(GatewayRequestImpl.class), mock(Context.class));
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
	}

	@Test
	public void delegateRequest_NullRequestGiven_ShouldNotProcessRequest() {
		GatewayResponse response = gatewayHandler.delegateRequest(null, mock(Context.class));
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
		verify(gatewayHandler, times(0)).beforeHandleRequest(any(), any(), any());
	}

	@Test
	public void delegateRequest_NullContextGiven_ShouldNotProcessRequest() {
		GatewayResponse response = gatewayHandler.delegateRequest(mock(GatewayRequestImpl.class), null);
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
		verify(gatewayHandler, times(0)).beforeHandleRequest(any(), any(), any());
	}

	@Test
	public void delegateRequest_ContainerException_ShouldInvokeCallbacks() {
		GatewayRequest request = createMinimalRequest();
		Context context = mock(Context.class);
		doThrow(new RuntimeException()).when(container).handleRequest(any(), any(), any());
		GatewayResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
		verify(gatewayHandler, times(1)).beforeHandleRequest(eq(request), any(), eq(context));
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldReturnGatewayDefaultResponse() {
		GatewayRequestImpl request = createMinimalRequest();
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		GatewayResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(new GatewayResponse("testBody", ImmutableMap.of("k", "k,v"), Status.OK), response);
	}

	@Test
	public void delegateRequest_NonDefaultContainerResponseGiven_ShouldThrowResponse() {
		GatewayRequestImpl request = createMinimalRequest();
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.MOVED_PERMANENTLY, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		GatewayResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(301, response.getStatusCode());
		assertEquals(ImmutableMap.of("k", "k,v"), response.getHeaders());
		assertEquals("testBody", response.getBody());
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldInvokeCallbacks() {
		GatewayRequestImpl request = createMinimalRequest();
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		gatewayHandler.delegateRequest(request, context);
		verify(gatewayHandler).beforeHandleRequest(eq(request), any(), eq(context));
		verify(gatewayHandler).onRequestSuccess(eq(containerResponse), eq(request), any(), eq(context));
	}

	@Test
	public void delegateRequest_ExceptionInOnRequestFailure_ShouldResultInAnInternalServerError() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		doThrow(new RuntimeException()).when(container).handleRequest(any(), any(), any());
		doThrow(new RuntimeException()).when(gatewayHandler).onRequestFailure(any(), eq(request), any(), eq(context));
		GatewayResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
	}

	@Test
	public void delegateRequest_EmptyResponseFromCallback_ShouldResultInInternalServerError() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		doReturn(null).when(gatewayHandler).onRequestSuccess(eq(containerResponse), eq(request), any(), eq(context));
		GatewayResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(500, response.getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void delegateRequest_ValidRequestGiven_ShouldRegisterContextsOnRequest() {
		GatewayRequest request = createMinimalRequest();
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		ArgumentCaptor<Consumer> containerEnhancerCapure = ArgumentCaptor.forClass(Consumer.class);
		gatewayHandler.delegateRequest(request, context);
		verify(container).handleRequest(any(), eq(responseWriter), any(), containerEnhancerCapure.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCapure.getValue().accept(containerRequest);
		verify(containerRequest).setProperty("awsLambdaContext", context);
		verify(containerRequest).setProperty("awsApiGatewayRequest", request);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoPathGiven_ShouldThrowNpe() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setPath(null);
		gatewayHandler.createContainerRequest(request);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoHttpMethodGiven_ShouldThrowNpe() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setHttpMethod(null);
		gatewayHandler.createContainerRequest(request);
	}

	@Test
	public void createContainerRequest_NoBodyGiven_ShouldUseEmptyBaos() {
		GatewayRequestImpl request = createMinimalRequest();
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertEquals("", toString((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_BodyGiven_ShouldUseBody() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setBody("abc");
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertEquals("abc", toString((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_HttpMethodGiven_ShouldUseHttpMethod() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setHttpMethod("POST");
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals("POST", containerRequest.getHttpMethod());
	}

	@Test
	public void createContainerRequest_PathWithNoQueryParamsGiven_ShouldUsePathAsRequestUri() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setPath("/abc");
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_PathWithOneQueryParamsGiven_ShouldUseQueryParamsInRequestUri() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setPath("/abc");
		request.setQueryStringParameters(ImmutableMap.of("a_k", "a_v"));
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc?a_k=a_v"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_PathWithMultipleQueryParamsGiven_ShouldUseQueryParamsInRequestUri() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setPath("/abc");
		request.setQueryStringParameters(ImmutableMap.of("a_k", "a_v", "b_k", "b_v"));
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(URI.create("/abc?a_k=a_v&b_k=b_v"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_HeadersGiven_ShouldUseHeaders() {
		GatewayRequestImpl request = createMinimalRequest();
		request.setHeaders(ImmutableMap.of("a_k", "a_v", "b_k", "b_v"));
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("a_k", singletonList("a_v"), "b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullHeaderKeyGiven_ShouldFilterHeader() {
		GatewayRequestImpl request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put(null, "a_v");
		headers.put("b_k", "b_v");
		request.setHeaders(headers);
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_NullHeaderValueGiven_ShouldFilterHeader() {
		GatewayRequestImpl request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put("a_k", null);
		headers.put("b_k", "b_v");
		request.setHeaders(headers);
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("b_k", singletonList("b_v")), containerRequest.getHeaders());
	}

	@Test
	public void createContainerRequest_CommaSeparatedHeaderValueGiven_ShouldNotSpreadHeader() {
		GatewayRequestImpl request = createMinimalRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put("a_k", "a_v0,a_v1");
		request.setHeaders(headers);
		GatewayContainerRequest containerRequest = gatewayHandler.createContainerRequest(request);
		assertEquals(ImmutableMap.of("a_k", singletonList("a_v0,a_v1")), containerRequest.getHeaders());
	}

	private GatewayRequestImpl createMinimalRequest() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		request.setPath("/");
		request.setHttpMethod("GET");
		return request;
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
	}
}
