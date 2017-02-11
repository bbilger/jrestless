package com.jrestless.core.filter.cors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

public class DefaultSameOriginPolicyTest {

	@Test
	public void requestUriWithoutSchemeGiven_Fails() {
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("api.example.com"), "https://api.example.com"));
	}

	@Test
	public void requestUriWithoutHostGiven_Fails() {
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://user:pw@/a"), "https://api.example.com"));
	}

	@Test
	public void requestUriWithHttpSchemeAndPort80Given_IgnoresPort() {
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com:80"), "http://api.example.com"));
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com:80"), "https://api.example.com:80"));
	}

	@Test
	public void requestUriWithHttpSchemeAndNoPortGiven_IgnoresPort() {
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com"), "http://api.example.com"));
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com"), "https://api.example.com:80"));
	}

	@Test
	public void requestUriWithHttpSchemeAndNonDefaultPortGiven_ConsidersPort() {
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com:1337"), "http://api.example.com"));
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("http://api.example.com:1337"), "http://api.example.com:1337"));
	}

	@Test
	public void requestUriWithHttpsSchemeAndDefaultPortGiven_IgnoresPort() {
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com:443"), "https://api.example.com"));
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com:443"), "https://api.example.com:433"));
	}

	@Test
	public void requestUriWithHttpsSchemeAndNoPortGiven_IgnoresPort() {
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com"), "https://api.example.com"));
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com"), "https://api.example.com:433"));
	}

	@Test
	public void requestUriWithHttpsSchemeAndNonDefaultPortGiven_ConsidersPort() {
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com:1337"), "https://api.example.com"));
		assertTrue(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com:1337"), "https://api.example.com:1337"));
	}

	@Test
	public void subdomainGiven_NoMatch() {
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://example.com"), "https://api.example.com"));
		assertFalse(new DefaultSameOriginPolicy().isSameOrigin(createContainerRequestContext("https://api.example.com"), "https://example.com"));
	}

	private static ContainerRequestContext createContainerRequestContext(String requestUri) {
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getRequestUri()).thenReturn(new URI(requestUri));
		} catch (URISyntaxException e) {
			throw new RuntimeException();
		}
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getUriInfo()).thenReturn(uriInfo);
		return request;
	}
}
