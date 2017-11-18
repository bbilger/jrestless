package com.jrestless.core.container.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.handler.SimpleRequestHandler.SimpleResponseWriter;
import com.jrestless.core.container.io.DefaultJRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequest;

public class SimpleRequestHandlerTest {

	private JRestlessHandlerContainer<JRestlessContainerRequest> container;
	private SimpleRequestHandler<JRestlessContainerRequest, SimpleContainerResponse> handler;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		handler = spy(new SimpleRequestHandlerImpl());
		handler.init(container);
		handler.start();
	}

	@AfterEach
	public void tearDown() {
		handler.stop();
	}

	@Test
	public void init0_NullAppGiven_ShouldThrowNpe() {
		assertThrows(NullPointerException.class, () -> new SimpleRequestHandlerImpl().init((Application) null));
	}

	@Test
	public void init1_NullAppGiven_ShouldThrowNpe() {
		assertThrows(NullPointerException.class, () -> new SimpleRequestHandlerImpl().init((Application) null, ""));
	}

	@Test
	public void init1_NullServiceLocatorGiven_ShouldNotThrowNpe() {
		new SimpleRequestHandlerImpl().init(mock(Application.class), null);
	}

	@Test
	public void init2_NullHandlerContainerGiven_ShouldThrowNpe() {
		assertThrows(NullPointerException.class, () -> new SimpleRequestHandlerImpl().init((JRestlessHandlerContainer<JRestlessContainerRequest>) null));
	}

	@Test
	public void init2_MultiInit_ShouldThrowIse() {
		assertThrows(IllegalStateException.class, () -> handler.init(container));
	}

	@Test
	public void start_NotInitialized_ShouldThrowIse() {
		assertThrows(IllegalStateException.class, () -> new SimpleRequestHandlerImpl().start());
	}

	@Test
	public void start_MultiStart_ShouldThrowIse() {
		assertThrows(IllegalStateException.class, handler::start);
	}

	@Test
	public void start_Initialized_ShouldDelegateStartToContainer() {
		@SuppressWarnings("unchecked")
		JRestlessHandlerContainer<JRestlessContainerRequest> customContainer = mock(JRestlessHandlerContainer.class);
		SimpleRequestHandlerImpl customHandler = new SimpleRequestHandlerImpl();
		customHandler.init(customContainer);
		customHandler.start();
		verify(customContainer).onStartup();
	}

	@Test
	public void stop_NotInitialized_ShouldThrowIse() {
		assertThrows(IllegalStateException.class, () -> new SimpleRequestHandlerImpl().stop());
	}

	@Test
	public void stop_NotStarted_ShouldThrowIse() {
		SimpleRequestHandlerImpl customHandler = null;
		try {
			customHandler = new SimpleRequestHandlerImpl();
			customHandler.init(container);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertThrows(IllegalStateException.class, customHandler::stop);
	}

	@Test
	public void stop_MultiStop_ShouldThrowIse() {
		SimpleRequestHandlerImpl customHandler = null;
		try {
			customHandler = new SimpleRequestHandlerImpl();
			customHandler.init(container);
			customHandler.start();
			customHandler.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertThrows(IllegalStateException.class, customHandler::stop);
	}

	@Test
	public void stop_Running_ShouldDelegateStopToContainer() {
		@SuppressWarnings("unchecked")
		JRestlessHandlerContainer<JRestlessContainerRequest> customContainer = mock(JRestlessHandlerContainer.class);
		SimpleRequestHandlerImpl customHandler = new SimpleRequestHandlerImpl();
		customHandler.init(customContainer);
		customHandler.start();
		customHandler.stop();
		verify(customContainer).onShutdown();
	}

	@Test
	public void delegateRequest_NotStarted_ShouldReturnInternalServerError() {
		SimpleContainerResponse response = new SimpleRequestHandlerImpl().delegateRequest(mock(JRestlessContainerRequest.class));
		assertEquals(500, response.getStatusType().getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
	}

	@Test
	public void delegateRequest_NullGiven_ShouldNotProcessRequest() {
		SimpleContainerResponse response = handler.delegateRequest(null);
		assertEquals(500, response.getStatusType().getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
		verify(handler, times(0)).beforeHandleRequest(any(), any());
	}

	@Test
	public void delegateRequest_ContainerException_ShouldInvokeCallbacks() {
		JRestlessContainerRequest request = createMinimalRequest();
		RuntimeException containerException = new RuntimeException();
		doThrow(containerException).when(container).handleRequest(any(), any(), any(), any());
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(500, response.getStatusType().getStatusCode());
		assertTrue(response.getHeaders().isEmpty());
		assertEquals(null, response.getBody());
		verify(handler, times(1)).beforeHandleRequest(eq(request), any());
		verify(handler, times(1)).onRequestFailure(same(containerException), eq(request), any());
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldReturnGatewayDefaultResponse() {
		JRestlessContainerRequest request = createMinimalRequest();
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		SimpleContainerResponse containerResponse = new SimpleContainerResponse(Status.OK, "testBody", headers);
		@SuppressWarnings("unchecked")
		SimpleResponseWriter<SimpleContainerResponse> responseWriter = mock(SimpleResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(handler).createResponseWriter(request);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(new SimpleContainerResponse(Status.OK, "testBody", headers), response);
	}

	@Test
	public void delegateRequest_NonDefaultContainerResponseGiven_ShouldThrowResponse() {
		JRestlessContainerRequest request = createMinimalRequest();
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		SimpleContainerResponse containerResponse = new SimpleContainerResponse(Status.MOVED_PERMANENTLY, "testBody", headers);
		@SuppressWarnings("unchecked")
		SimpleResponseWriter<SimpleContainerResponse> responseWriter = mock(SimpleResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(handler).createResponseWriter(request);
		SimpleContainerResponse response = handler.delegateRequest(request);
		assertEquals(301, response.getStatusType().getStatusCode());
		assertEquals(headers, response.getHeaders());
		assertEquals("testBody", response.getBody());
	}

	@Test
	public void delegateRequest_DefaultContainerResponseGiven_ShouldInvokeCallbacks() {
		JRestlessContainerRequest request = createMinimalRequest();
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("k", "v"));
		SimpleContainerResponse containerResponse = new SimpleContainerResponse(Status.OK, "testBody", headers);
		@SuppressWarnings("unchecked")
		SimpleResponseWriter<SimpleContainerResponse> responseWriter = mock(SimpleResponseWriter.class);
		when(responseWriter.getResponse()).thenReturn(containerResponse);
		doReturn(responseWriter).when(handler).createResponseWriter(request);
		handler.delegateRequest(request);
		verify(handler).beforeHandleRequest(eq(request), any());
		verify(handler).onRequestSuccess(eq(containerResponse), eq(request), any());
	}

	private JRestlessContainerRequest createMinimalRequest() {
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/"), URI.create("/"), "GET",
				new ByteArrayInputStream(new byte[0]), new HashMap<>());
		return request;
	}

	private static class SimpleRequestHandlerImpl extends SimpleRequestHandler<JRestlessContainerRequest, SimpleContainerResponse> {

		@Override
		public JRestlessContainerRequest createContainerRequest(JRestlessContainerRequest request) {
			return request;
		}

		@SuppressWarnings("unchecked")
		@Override
		public SimpleResponseWriter<SimpleContainerResponse> createResponseWriter(JRestlessContainerRequest request) {
			return mock(SimpleResponseWriter.class);
		}

		@Override
		public SimpleContainerResponse onRequestFailure(Exception e, JRestlessContainerRequest request,
				JRestlessContainerRequest containerRequest) {
			return new SimpleContainerResponse(Status.INTERNAL_SERVER_ERROR, null, new HashMap<>());
		}
	}
}
