package com.jrestless.core.container;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessRequestContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ApplicationHandler.class)
public class JRestlessHandlerContainerTest {

	private ApplicationHandler appHandler;
	private JRestlessHandlerContainer<JRestlessRequestContext> container;

	@Before
	public void setup() {
		appHandler = PowerMockito.mock(ApplicationHandler.class);
		container = spy(new JRestlessHandlerContainer<JRestlessRequestContext>(appHandler));
	}

	@Test
	public void getConfiguration_ShouldReturnAppHandlerConfiguration() {
		ResourceConfig config = mock(ResourceConfig.class);
		when(appHandler.getConfiguration()).thenReturn(config);
		assertSame(config, container.getConfiguration());
	}

	@Test
	public void onStartup_ShouldStartAppHandler() {
		container.onStartup();
		verify(appHandler, times(1)).onStartup(container);
	}

	@Test
	public void onShutdown_ShouldShutdownAppHandler() {
		container.onShutdown();
		verify(appHandler, times(1)).onShutdown(container);
	}

	@Test
	public void reload_ConfigGiven_ShouldShutdownOldAppHandler() {
		ResourceConfig config = new ApplicationHandler().getConfiguration();
		container.reload(config);
		verify(appHandler, times(1)).onShutdown(container);
	}

	@Test
	public void reload_ConfigGiven_ShouldReloadNewAppHandler() {
		ResourceConfig config = new ApplicationHandler().getConfiguration();
		ApplicationHandler newAppHandler = PowerMockito.mock(ApplicationHandler.class);
		doReturn(newAppHandler).when(container).createNewApplicationHandler(any());
		container.reload(config);
		verify(newAppHandler, times(1)).onReload(container);
	}

	@Test
	public void reload_ConfigGiven_ShouldStartNewAppHandler() {
		ResourceConfig config = new ApplicationHandler().getConfiguration();
		ApplicationHandler newAppHandler = PowerMockito.mock(ApplicationHandler.class);
		doReturn(newAppHandler).when(container).createNewApplicationHandler(any());
		container.reload(config);
		verify(newAppHandler, times(1)).onStartup(container);
	}

	@Test
	public void reload_ConfigGiven_ShouldResetAppHandler() {
		ResourceConfig config = new ApplicationHandler().getConfiguration();
		ApplicationHandler newAppHandler = PowerMockito.mock(ApplicationHandler.class);
		doReturn(newAppHandler).when(container).createNewApplicationHandler(any());
		container.reload(config);
		assertSame(newAppHandler, container.getApplicationHandler());
	}

	@Test
	public void reload_ConfigGiven_ShouldCreateAppHandlerUsingConfiguration() {
		ResourceConfig config = new ApplicationHandler().getConfiguration();
		container.reload(config);
		verify(container, times(1)).createNewApplicationHandler(config);
	}

	@Test
	public void handleRequest_ContainerRequestGiven_ShouldInvokeAppHandler() {
		ContainerRequest request = mock(ContainerRequest.class);
		container.handleRequest(request);
		verify(container, times(1)).handleRequest(eq(request));
	}

	@Test
	public void reload_NoConfigGiven_ShouldCreateAppHandlerUsingCurrentConfig() {
		doNothing().when(container).reload(any());
		ResourceConfig config = container.getConfiguration();
		container.reload();
		verify(container, times(1)).reload(config);
	}

	@Test(expected = NullPointerException.class)
	public void constructor_NoAppHandlerGiven_ShouldThrowNpe() {
		new JRestlessHandlerContainer<JRestlessRequestContext>((ApplicationHandler) null);
	}

	@Test(expected = NullPointerException.class)
	public void handleRequest_NoContainerRequestGiven_ShouldThrowNpe() {
		container.handleRequest(null);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestGiven_ShouldThrowNpe() {
		container.createContainerRequest(null, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoContainerResponseWriterGiven_ShouldThrowNpe() {
		container.createContainerRequest(createAnyRequest(), null, mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoSecurityContext_ShouldThrowNpe() {
		container.createContainerRequest(createAnyRequest(), mock(ContainerResponseWriter.class), null);
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestBaseUriGiven_ShouldThrowNpe() {
		JRestlessRequestContext request = createAnyRequest();
		when(request.getBaseUri()).thenReturn(null);
		container.createContainerRequest(request, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestRequestUriGiven_ShouldThrowNpe() {
		JRestlessRequestContext request = createAnyRequest();
		when(request.getRequestUri()).thenReturn(null);
		container.createContainerRequest(request, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestHttpMethodGiven_ShouldThrowNpe() {
		JRestlessRequestContext request = createAnyRequest();
		when(request.getHttpMethod()).thenReturn(null);
		container.createContainerRequest(request, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestEntityStreamGiven_ShouldThrowNpe() {
		JRestlessRequestContext request = createAnyRequest();
		when(request.getEntityStream()).thenReturn(null);
		container.createContainerRequest(request, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test(expected = NullPointerException.class)
	public void createContainerRequest_NoRequestHeadersGiven_ShouldThrowNpe() {
		JRestlessRequestContext request = createAnyRequest();
		when(request.getHeaders()).thenReturn(null);
		container.createContainerRequest(request, mock(ContainerResponseWriter.class), mock(SecurityContext.class));
	}

	@Test
	public void createContainerRequest_RequestAndContainerResponseWriterAndSecurityContextGiven_ShouldCreateContainerRequestUsingValues() {
		URI baseUri = URI.create("/");
		URI requestUri = URI.create("/entity");
		String httpMethod = "POST";
		ByteArrayInputStream entityStream = new ByteArrayInputStream(new byte[0]);
		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		headers.add("hk0", "hv0_0");
		headers.add("hk0", "hv0_1");
		headers.add("hk1", "hv1_0");
		JRestlessRequestContext request = createRequest(baseUri, requestUri, httpMethod, entityStream, headers);

		ContainerResponseWriter containerResponseWriter = mock(ContainerResponseWriter.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		ContainerRequest containerRequest = container.createContainerRequest(request, containerResponseWriter, securityContext);

		assertEquals(baseUri, containerRequest.getBaseUri());
		assertEquals(requestUri, containerRequest.getRequestUri());
		assertEquals(httpMethod, containerRequest.getMethod());
		assertSame(entityStream, containerRequest.getEntityStream());
		assertEquals(headers, containerRequest.getHeaders());
		assertSame(containerResponseWriter, containerRequest.getResponseWriter());
		assertSame(securityContext, containerRequest.getSecurityContext());
	}

	private JRestlessRequestContext createAnyRequest() {
		return createRequest("/", "/entity", "GET", new ByteArrayInputStream(new byte[0]), new MultivaluedHashMap<>());
	}

	private JRestlessRequestContext createRequest(String baseUri, String requestUri, String httpMethod, InputStream entityStream, MultivaluedMap<String, String> headers) {
		return createRequest(URI.create(baseUri), URI.create(requestUri), httpMethod, entityStream, headers);
	}

	private JRestlessRequestContext createRequest(URI baseUri, URI requestUri, String httpMethod, InputStream entityStream, MultivaluedMap<String, String> headers) {
		JRestlessRequestContext request = mock(JRestlessRequestContext.class);
		when(request.getBaseUri()).thenReturn(baseUri);
		when(request.getRequestUri()).thenReturn(requestUri);
		when(request.getHttpMethod()).thenReturn(httpMethod);
		when(request.getEntityStream()).thenReturn(entityStream);
		when(request.getHeaders()).thenReturn(headers);
		return request;
	}
}
