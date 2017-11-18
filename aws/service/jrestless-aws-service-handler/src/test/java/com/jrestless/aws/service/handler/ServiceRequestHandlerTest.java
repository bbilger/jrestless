package com.jrestless.aws.service.handler;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.AbstractLambdaContextReferencingBinder;
import com.jrestless.aws.service.io.DefaultServiceRequest;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessContainerRequest;

public class ServiceRequestHandlerTest {

	private static final Type SERVICE_REQUEST_TYPE = (new GenericType<Ref<ServiceRequest>>() { }).getType();

	private JRestlessHandlerContainer<JRestlessContainerRequest> container;
	private ServiceRequestHandlerImpl serviceHandler;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		serviceHandler = spy(new ServiceRequestHandlerImpl());
		serviceHandler.init(container);
		serviceHandler.start();
	}

	@AfterEach
	public void tearDown() {
		serviceHandler.stop();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void delegateRequest_ValidRequestAndReferencesGiven_ShouldSetReferencesOnRequestInitialization() {
		Context context = mock(Context.class);
		DefaultServiceRequest request = new DefaultServiceRequest(null, new HashMap<>(), URI.create("/"), "GET");

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, request);

		Ref<ServiceRequest> serviceRequestRef = mock(Ref.class);
		Ref<Context> contextRef = mock(Ref.class);

		InjectionManager injectionManager = mock(InjectionManager.class);
		when(injectionManager.getInstance(SERVICE_REQUEST_TYPE)).thenReturn(serviceRequestRef);
		when(injectionManager.getInstance(AbstractLambdaContextReferencingBinder.LAMBDA_CONTEXT_TYPE)).thenReturn(contextRef);

		requestScopedInitializer.initialize(injectionManager);

		verify(serviceRequestRef).set(request);
		verify(contextRef).set(context);
	}

	@Test
	public void delegateRequest_ValidRequestAndNoReferencesGiven_ShouldNotFailOnRequestInitialization() {
		Context context = mock(Context.class);
		DefaultServiceRequest request = new DefaultServiceRequest(null, new HashMap<>(), URI.create("/"), "GET");

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, request);

		InjectionManager injectionManager = mock(InjectionManager.class);
		requestScopedInitializer.initialize(injectionManager);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RequestScopedInitializer getSetRequestScopedInitializer(Context context, ServiceRequest request) {
		ServiceRequestAndLambdaContext reqAndContext = new ServiceRequestAndLambdaContext(request, context);
		ArgumentCaptor<Consumer> containerEnhancerCaptor = ArgumentCaptor.forClass(Consumer.class);
		serviceHandler.delegateRequest(reqAndContext);
		verify(container).handleRequest(any(), any(), any(), containerEnhancerCaptor.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCaptor.getValue().accept(containerRequest);

		ArgumentCaptor<RequestScopedInitializer> requestScopedInitializerCaptor = ArgumentCaptor.forClass(RequestScopedInitializer.class);

		verify(containerRequest).setRequestScopedInitializer(requestScopedInitializerCaptor.capture());

		return requestScopedInitializerCaptor.getValue();
	}

	@Test
	public void createContainerRequest_NoBodyGiven_ShouldUseEmptyBaos() {
		ServiceRequestAndLambdaContext request = createMinimalRequest();
		JRestlessContainerRequest containerRequest = serviceHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertArrayEquals(new byte[0], toBytes((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_BodyGiven_ShouldUseBody() {
		ServiceRequestAndLambdaContext request = createMinimalRequest();
		((DefaultServiceRequest) request.getServiceRequest()).setBody("abc");
		JRestlessContainerRequest containerRequest = serviceHandler.createContainerRequest(request);
		InputStream is = containerRequest.getEntityStream();
		assertEquals(ByteArrayInputStream.class, is.getClass());
		assertArrayEquals("abc".getBytes(), toBytes((ByteArrayInputStream) is));
	}

	@Test
	public void createContainerRequest_HttpMethodGiven_ShouldUseHttpMethod() {
		ServiceRequestAndLambdaContext request = createMinimalRequest();
		((DefaultServiceRequest) request.getServiceRequest()).setHttpMethod("X");
		JRestlessContainerRequest containerRequest = serviceHandler.createContainerRequest(request);
		assertEquals("X", containerRequest.getHttpMethod());
	}

	@Test
	public void createContainerRequest_PathGiven_ShouldUsePath() {
		ServiceRequestAndLambdaContext request = createMinimalRequest();
		((DefaultServiceRequest) request.getServiceRequest()).setRequestUri(URI.create("/a?b=c&d=e"));
		JRestlessContainerRequest containerRequest = serviceHandler.createContainerRequest(request);
		assertEquals(URI.create("/a?b=c&d=e"), containerRequest.getRequestUri());
	}

	@Test
	public void createContainerRequest_HeadersGiven_ShouldUseHeaders() {
		ServiceRequestAndLambdaContext request = createMinimalRequest();
		Map<String, List<String>> headers = ImmutableMap.of("0", emptyList(), "1", singletonList("a"), "2", ImmutableList.of("b", "c"));
		((DefaultServiceRequest) request.getServiceRequest()).setHeaders(headers);
		JRestlessContainerRequest containerRequest = serviceHandler.createContainerRequest(request);
		assertEquals(headers, containerRequest.getHeaders());
	}

	private ServiceRequestAndLambdaContext createMinimalRequest() {
		ServiceRequest request = new DefaultServiceRequest(null, new HashMap<>(), URI.create("/"), "GET");
		return new ServiceRequestAndLambdaContext(request, null);
	}

	public static byte[] toBytes(ByteArrayInputStream bais) {
		byte[] array = new byte[bais.available()];
		try {
			bais.read(array);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return array;
	}

	private static class ServiceRequestHandlerImpl extends ServiceRequestHandler {
	}
}
