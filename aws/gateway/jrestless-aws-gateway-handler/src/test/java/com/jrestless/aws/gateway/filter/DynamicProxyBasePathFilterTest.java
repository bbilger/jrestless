package com.jrestless.aws.gateway.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import com.jrestless.aws.gateway.io.GatewayRequest;

public class DynamicProxyBasePathFilterTest {

	@Test
	public void filter_NoProxyResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter("/a");
		filter.filter(null);
		ContainerRequestContext containerRequest = createContainerRequest("/", "/a");
		filter.filter(containerRequest);
		verifyZeroInteractions(containerRequest);
	}

	@Test
	public void filter_NullResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter(null);
		filter.filter(null);
		ContainerRequestContext containerRequest = createContainerRequest("/", "/a");
		filter.filter(containerRequest);
		verifyZeroInteractions(containerRequest);
	}

	@Test
	public void filter_EmptyResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter("");
		filter.filter(null);
		ContainerRequestContext containerRequest = createContainerRequest("/", "");
		filter.filter(containerRequest);
		verifyZeroInteractions(containerRequest);
	}

	@Test
	public void filter_InvalidResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter("/proxy+}");
		filter.filter(null);
		ContainerRequestContext containerRequest = createContainerRequest("/", "/whatever");
		filter.filter(containerRequest);
		verifyZeroInteractions(containerRequest);
	}

	@Test
	public void filter_RootProxyResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter("/{proxy+}");
		filter.filter(null);
		ContainerRequestContext containerRequest = createContainerRequest("/", "/whatever");
		filter.filter(containerRequest);
		verifyZeroInteractions(containerRequest);
	}

	@Test
	public void filter_PrefixedProxyResourceGiven_ShouldNotUpdateBaseUri() throws IOException {
		DynamicProxyBasePathFilter filter = createFilter("/base-path/{proxy+}");
		ContainerRequestContext containerRequest = createContainerRequest("/", "/whatever");
		filter.filter(containerRequest);
		verify(containerRequest).setRequestUri(URI.create("/base-path/"), URI.create("/whatever"));
	}

	private ContainerRequestContext createContainerRequest(String baseUri, String requestUri) {
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getRequestUri()).thenReturn(URI.create(requestUri));
		when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(baseUri));
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getUriInfo()).thenReturn(uriInfo);
		return request;
	}

	private DynamicProxyBasePathFilter createFilter(String resource) {
		return new DynamicProxyBasePathFilter(createGatewayRequest(resource));
	}

	private static GatewayRequest createGatewayRequest(String resource) {
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getResource()).thenReturn(resource);
		return request;
	}
}
