package com.jrestless.core.filter.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.ImmutableSet;
import com.jrestless.test.ConstructorPreconditionsTester;

public class CorsFilterTest {

	private static final String DEFAULT_ORIGIN = "https://page.example.com";
	private static final String DEFAULT_HOST = "https://api.example.com";

	@Test
	public void requestFilter_NoOriginGiven_SkipsFiltering() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		filter.filter(request);
		verify(request, never()).abortWith(any());
	}

	@Test
	public void requestFilter_SameOriginGiven_SkipsFiltering() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_HOST, HttpMethod.GET);
		filter.filter(request);
		verify(request, never()).abortWith(any());
	}

	@Test
	public void requestFilter_CustomAlwaysSameOriginPolicyGiven_SkipsFiltering() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.sameOriginPolicy((containerRequestContext, origin) -> true)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		filter.filter(request);
		verify(request, never()).abortWith(any());
	}

	@Test
	public void requestFilter_OriginWithNoHostGiven_CorsFails() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("/a");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithNoSchemeGiven_CorsFails() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("http://user:password@/1");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithPathGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("http://example.com/ab");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithQueryGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("http://example.com?a=b");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithFragmentGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("http://example.com#abc");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithUserInfpGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("http://user:password@example.com");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_InvalidOriginUriGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn("%://");
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void requestFilter_OriginWithBlankAccessControlRequestMethodGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn(DEFAULT_ORIGIN);
		when(request.getHeaderString("Access-Control-Request-Method")).thenReturn(" ");
		UriInfo uriInfo = mockUriInfo(DEFAULT_HOST);
		when(request.getUriInfo()).thenReturn(uriInfo);
		filterAndVerifyCorsFailure(filter, request);
	}

	@Test
	public void preflightRequestFilter_AnyOriginAndCredentialsAllowdGiven_ReturnsAnyOrigingWithoutVary() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowAnyOrigin()
				.disallowCredentials()
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals("*", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertNull(response.getHeaderString(HttpHeaders.VARY));
	}

	@Test
	public void preflightRequestFilter_AnyOriginAndCredentialsDisallowedGiven_ReturnsOriginWithVary() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowAnyOrigin()
				.allowCredentials()
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals(DEFAULT_ORIGIN, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, response.getHeaderString(HttpHeaders.VARY));
	}

	@Test
	public void preflightRequestFilter_OriginAndCredentialsDisallowedGiven_ReturnsOriginWithVary() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_ORIGIN)
				.allowCredentials(false)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals(DEFAULT_ORIGIN, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, response.getHeaderString(HttpHeaders.VARY));
	}

	@Test
	public void preflightRequestFilter_OriginAndCredentialsAllowedGiven_ReturnsOriginWithVary() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_ORIGIN)
				.allowCredentials()
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals(DEFAULT_ORIGIN, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, response.getHeaderString(HttpHeaders.VARY));
	}

	@Test
	public void preflightRequestFilter_MaxAgeGiven_ReturnsMaxAge() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.maxAge(10)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals("10", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_MAX_AGE));
	}

	@Test
	public void preflightRequestFilter_NoMaxAgeGiven_ReturnsMaxAge() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.maxAge(0)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertNull(response.getHeaderString(CorsHeaders.ACCESS_CONTROL_MAX_AGE));
	}

	@Test
	public void preflightRequestFilter_AccessControlRequestMethodGiven_ReturnsTheRequestMethodOnly() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowMethod(HttpMethod.DELETE)
				.allowMethod(HttpMethod.GET)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals(HttpMethod.DELETE, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS));
	}

	@Test
	public void preflightRequestFilter_NoAccessControlRequestHeadersGiven_DoesNotReturnAllowHeader() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowHeader("h1")
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertNull(response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
	}

	@Test
	public void preflightRequestFilter_AccessControlRequestHeadersGiven_ReturnsPassedHeadersOnly() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowHeader("h1")
				.allowHeader("h2")
				.allowHeader("h3")
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE, " h1, h2 ");
		filter.filter(request);
		Response response = getAbortResponse(request);
		assertEquals(" h1, h2 ", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS));
	}

	@Test
	public void preflightRequestFilter_InvalidMethodGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowMethod(HttpMethod.GET)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.DELETE);
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void preflightRequestFilter_BlankMethodGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowMethod(HttpMethod.GET)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, " ");
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void preflightRequestFilter_InvalidOriginGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_HOST)
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void preflightRequestFilter_InvalidHeaderGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowHeaders(Collections.singleton("h1"))
				.build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET, "h2");
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void actualRequestFilter_InvalidOriginGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_HOST)
				.build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void actualRequestFilter_InvalidMethodGiven_CorsFails() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowMethod(HttpMethod.DELETE)
				.build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		assertThrows(ForbiddenException.class, () -> filter.filter(request));
	}

	@Test
	public void actualRequestFilter_ValidGiven_PassesWithoutCorsHeadersRequestHeaders() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		filter.filter(request);
		verify(request, never()).abortWith(any());
	}

	@Test
	public void actualRequestFilter_OptionsWithNoAccessControlRequestMethodGiven_PassesRequestWithoutCorsHeaders() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.OPTIONS);
		filter.filter(request);
		verify(request, never()).abortWith(any());
	}

	@Test
	public void responseFilter_NoOriginGiven_NoCorsHeadersAdded() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		filter.filter(request, response);
		verifyZeroInteractions(response);
	}

	@Test
	public void requestFilter_PreflightRequestGiven_NoCorsHeadersAdded() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = createPreflightRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, "...");
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		filter.filter(request, response);
		verifyZeroInteractions(response);
	}

	@Test
	public void responseFilter_RequestFilterFailureGiven_NoCorsHeadersAdded() throws IOException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn(DEFAULT_ORIGIN);
		when(request.getProperty("jrestless.cors.filter.failure")).thenReturn(true);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		filter.filter(request, response);
		verifyZeroInteractions(response);
	}

	@Test
	public void responseFilter_SameOriginGiven_NoCorsHeadersAdded() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder().build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_HOST, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		filter.filter(request, response);
		verifyZeroInteractions(response);
	}

	@Test
	public void responseFilter_CustomAlwaysSameOriginPolicyGiven_NoCorsHeadersAdded() throws IOException, URISyntaxException {
		CorsFilter filter = new CorsFilter.Builder()
				.sameOriginPolicy((requestContext, origin) -> true)
				.build();
		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		filter.filter(request, response);
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_AnyOriginAndDisallowCredentialsGiven_AddsAnyOriginAndNoVaryHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.disallowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals("*", headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertNull(headers.getFirst(HttpHeaders.VARY));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_AnyOriginAndAllowCredentialsGiven_AddsPassedOriginAndVaryHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals(DEFAULT_ORIGIN, headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, headers.getFirst(HttpHeaders.VARY));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_OriginAndAllowCredentialsGiven_AddsPassedOriginAndVaryHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_ORIGIN)
				.allowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals(DEFAULT_ORIGIN, headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, headers.getFirst(HttpHeaders.VARY));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_OriginAndDisallowCredentialsGiven_AddsPassedOriginAndVaryHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowOrigin(DEFAULT_ORIGIN)
				.disallowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals(DEFAULT_ORIGIN, headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(CorsHeaders.ORIGIN, headers.getFirst(HttpHeaders.VARY));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_AllowCredentialsGiven_AddsAllowCredentialHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.allowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals("true", headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
		assertEquals(CorsHeaders.ORIGIN, headers.getFirst(HttpHeaders.VARY));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_DisallowCredentialsGiven_DoesNotAddAllowCredentialHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.disallowCredentials()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertNull(headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_NoExposedHeadersGiven_DoesNotAddExposeHeadersHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertNull(headers.getFirst(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_EmptyExposedHeadersGiven_DoesNotAddExposeHeadersHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.exposeHeaders(Collections.emptySet())
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertNull(headers.getFirst(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	@Test
	public void corsResponseFilter_ExposedHeadersGiven_AddsExposeHeadersHeader() throws IOException {
		CorsFilter filter = new CorsFilter.Builder()
				.exposeHeader("h1")
				.exposeHeader("h2")
				.build();

		ContainerRequestContext request = createActualRequestMock(DEFAULT_HOST, DEFAULT_ORIGIN, HttpMethod.GET);
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(response.getHeaders()).thenReturn(headers);
		filter.filter(request, response);

		assertEquals("h1,h2", headers.getFirst(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));

		verify(response).getHeaders();
		verifyZeroInteractions(response);
	}

	private static Response getAbortResponse(ContainerRequestContext request) {
		ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
		verify(request).abortWith(responseCaptor.capture());
		return responseCaptor.getValue();
	}

	private static ContainerRequestContext createPreflightRequestMock(String host, String origin,
			String accessControlRequestMethod) {
		return createPreflightRequestMock(host, origin, accessControlRequestMethod, null);
	}

	private static ContainerRequestContext createPreflightRequestMock(String host, String origin,
			String accessControlRequestMethod, String accessControlRequestHeaders) {
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn(origin);
		when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
		when(request.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn(accessControlRequestMethod);
		when(request.getHeaderString(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn(accessControlRequestHeaders);
		UriInfo uriInfo = mockUriInfo(host);
		when(request.getUriInfo()).thenReturn(uriInfo);
		return request;
	}

	private static ContainerRequestContext createActualRequestMock(String host, String origin, String method) {
		ContainerRequestContext request = mock(ContainerRequestContext.class);
		when(request.getHeaderString(CorsHeaders.ORIGIN)).thenReturn(origin);
		when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
		when(request.getMethod()).thenReturn(method);
		UriInfo uriInfo = mockUriInfo(host);
		when(request.getUriInfo()).thenReturn(uriInfo);
		return request;
	}

	private static UriInfo mockUriInfo(String requestUri) {
		UriInfo uriInfo = mock(UriInfo.class);
		try {
			when(uriInfo.getRequestUri()).thenReturn(new URI(requestUri));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return uriInfo;
	}

	private static void filterAndVerifyCorsFailure(CorsFilter filter, ContainerRequestContext requestMock) throws IOException {
		try {
			filter.filter(requestMock);
			fail("expected a ForbiddenException");
		} catch (ForbiddenException fe) {
			verify(requestMock).setProperty("jrestless.cors.filter.failure", true);
		}
	}

	@Test
	public void testConstructorPreconditions() {
		new ConstructorPreconditionsTester(getConstructor())
			// allowedOrigins
			.addValidArgs(0, Collections.singleton(DEFAULT_ORIGIN), Collections.singleton("*"))
			.addInvalidArgs(0, IllegalArgumentException.class, Collections.emptySet(), ImmutableSet.of("*", DEFAULT_ORIGIN))
			.addInvalidNpeArg(0)
			// allowedMethods
			.addValidArgs(1, Collections.singleton(DEFAULT_ORIGIN))
			.addInvalidArgs(1, IllegalArgumentException.class, Collections.emptySet())
			.addInvalidNpeArg(1)
			// allowCredentials
			.addValidArgs(2, true)
			// exposedHeaders
			.addValidArgs(3, Collections.emptySet(), Collections.singleton(DEFAULT_ORIGIN))
			.addInvalidNpeArg(3)
			// allowedHeaders
			.addValidArgs(4, Collections.emptySet(), Collections.singleton(DEFAULT_ORIGIN))
			.addInvalidNpeArg(4)
			// maxAge
			.addValidArgs(5, -10L, 0L, 1L)
			// sameOriginPolicy
			.addValidArgs(6, mock(SameOriginPolicy.class))
			.addInvalidNpeArg(6)
			.testPreconditionsAndValidCombinations();
	}

	private Constructor<CorsFilter> getConstructor() {
		try {
			return CorsFilter.class.getDeclaredConstructor(Set.class, Set.class, boolean.class, Set.class, Set.class,
					long.class, SameOriginPolicy.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
