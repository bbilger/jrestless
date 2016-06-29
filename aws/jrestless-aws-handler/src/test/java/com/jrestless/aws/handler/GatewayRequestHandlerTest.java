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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.jrestless.aws.GatewayRequestContext;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponse;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponseWriter;
import com.jrestless.aws.io.GatewayAdditionalResponseException;
import com.jrestless.aws.io.GatewayDefaultResponse;
import com.jrestless.aws.io.GatewayRequest;
import com.jrestless.core.container.JRestlessHandlerContainer;

public class GatewayRequestHandlerTest {

	private JRestlessHandlerContainer<GatewayRequest> container;
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
		new GatewayRequestHandlerImpl().init((JRestlessHandlerContainer<GatewayRequest>) null);
	}

	@Test(expected = IllegalStateException.class)
	public void sinit2_MultiInit_ShouldThrowIse() {
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
		try {
			new GatewayRequestHandlerImpl().delegateRequest(mock(GatewayRequest.class), mock(Context.class));
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
	}

	@Test
	public void delegateRequest_NullRequestGiven_ShouldNotProcessRequest() {
		try {
			gatewayHandler.delegateRequest(null, mock(Context.class));
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
		verify(gatewayHandler, times(0)).beforeHandleRequest(any(), any());
	}

	@Test
	public void delegateRequest_NullContextGiven_ShouldNotProcessRequest() {
		try {
			gatewayHandler.delegateRequest(mock(GatewayRequest.class), null);
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
		verify(gatewayHandler, times(0)).beforeHandleRequest(any(), any());
	}

	@Test
	public void delegateRequest_ContainerException_ShouldInvokeCallbacks() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		doThrow(new RuntimeException()).when(container).handleRequest(eq(request), any(), any());
		try {
			gatewayHandler.delegateRequest(request, context);
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
		verify(gatewayHandler, times(1)).beforeHandleRequest(request, context);
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldReturnGatewayDefaultResponse() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		GatewayDefaultResponse response = gatewayHandler.delegateRequest(request, context);
		assertEquals(new GatewayDefaultResponse("testBody", headers, Status.OK), response);
	}

	@Test
	public void delegateRequest_NonDefaultContainerResponseGiven_ShouldThrowResponse() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.MOVED_PERMANENTLY, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		try {
			gatewayHandler.delegateRequest(request, context);
			fail("expected non-default response to be returned as GatewayAdditionalResponseException");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"301\",\"body\":\"testBody\"}", gare.getMessage());
		}
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldInvokeCallbacks() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		gatewayHandler.delegateRequest(request, context);
		verify(gatewayHandler).beforeHandleRequest(request, context);
		verify(gatewayHandler).onRequestSuccess(containerResponse, request, context);
	}

	@Test
	public void delegateRequest_ExceptionInOnRequestFailure_ShouldResultInAnInternalServerError() {
		GatewayRequest request = mock(GatewayRequest.class);
		Context context = mock(Context.class);
		doThrow(new RuntimeException()).when(container).handleRequest(eq(request), any(), any());
		doThrow(new RuntimeException()).when(gatewayHandler).onRequestFailure(any(), eq(request), eq(context));
		try {
			gatewayHandler.delegateRequest(request, context);
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
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
		doReturn(null).when(gatewayHandler).onRequestSuccess(containerResponse, request, context);
		try {
			gatewayHandler.delegateRequest(request, context);
			fail("expected internal server error");
		} catch (GatewayAdditionalResponseException gare) {
			assertEquals("{\"statusCode\":\"500\"}", gare.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void delegateRequest_ValidRequestGiven_ShouldRegisterContextsOnRequest() {
		GatewayRequest request = mock(GatewayRequest.class);
		GatewayRequestContext requestContext = mock(GatewayRequestContext.class);
		when(request.getContext()).thenReturn(requestContext);
		Context context = mock(Context.class);
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		GatewayContainerResponse containerResponse = new GatewayContainerResponse(Status.OK, "testBody", headers);
		GatewayContainerResponseWriter responseWriter = mock(GatewayContainerResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(gatewayHandler).createResponseWriter();
		ArgumentCaptor<Consumer> containerEnhancerCapure = ArgumentCaptor.forClass(Consumer.class);
		gatewayHandler.delegateRequest(request, context);
		verify(container).handleRequest(eq(request), eq(responseWriter), any(), containerEnhancerCapure.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCapure.getValue().accept(containerRequest);
		verify(containerRequest).setProperty("awsLambdaContext", context);
		verify(containerRequest).setProperty("awsApiGatewayContext", requestContext);
	}

	private static class GatewayRequestHandlerImpl extends GatewayRequestHandler {

	}
}
