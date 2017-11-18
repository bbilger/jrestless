package com.jrestless.core.filter.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.ws.rs.DELETE;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CorsFilterIntTest extends JerseyTest {

	private static final String DEFAULT_ORIGIN = "http://example.com";
	private static final String SAME_ORGIN = "http://localhost:9998";

	@BeforeEach
	@Override
	// JerseyTest#setUp is annotated with @Before and as such not invoked by JUnit 5 => invoke
	public void setUp() throws Exception {
		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
		super.setUp();
	}

	@AfterEach
	@Override
	// JerseyTest#tearDown is annotated with @After and as such not invoked by JUnit 5 => invoke
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Override
	protected Application configure() {
		CorsFilter corsFilter = new CorsFilter.Builder()
				.allowMethod(HttpMethod.DELETE)
				.allowMethod(HttpMethod.OPTIONS)
				.allowHeader("ah0")
				.allowHeader("ah1")
				.allowOrigin(DEFAULT_ORIGIN)
				.allowOrigin("http://test.com")
				.exposeHeader("eh0")
				.exposeHeader("eh1")
				.build();
		ResourceConfig application = new ResourceConfig();
		application.register(corsFilter);
		application.register(TestResource.class);
		return application;
	}

	@Test
	public void testNoCorsRequest() {
		Response response = target()
				.path("delete")
				.request()
				.delete();
		assertEquals(200, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Test
	public void testValidCorsPreflightRequest() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE)
				.header(CorsHeaders.ORIGIN, DEFAULT_ORIGIN)
				.options();
		assertEquals(200, response.getStatus());
		MultivaluedMap<String, Object> headers = response.getHeaders();
		assertEquals(DEFAULT_ORIGIN, headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals(HttpMethod.DELETE, headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS));
		assertEquals("true", headers.getFirst(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
		assertEquals("3600", headers.getFirst(CorsHeaders.ACCESS_CONTROL_MAX_AGE));
		assertEquals(CorsHeaders.ORIGIN, headers.getFirst(HttpHeaders.VARY));
	}

	@Test
	public void testInvalidCorsPreflightRequestByMethod() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
				.header(CorsHeaders.ORIGIN, DEFAULT_ORIGIN)
				.options();
		assertEquals(403, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Test
	public void testInvalidCorsPreflightRequestByOrigin() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE)
				.header(CorsHeaders.ORIGIN, "http://bad.com")
				.options();
		assertEquals(403, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Test
	public void testInvalidCorsPreflightRequestByHeader() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE)
				.header(CorsHeaders.ORIGIN, DEFAULT_ORIGIN)
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "blub")
				.options();
		assertEquals(403, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Test
	public void testActualCorsRequest() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ORIGIN, DEFAULT_ORIGIN)
				.delete();
		assertEquals(200, response.getStatus());
		assertEquals(DEFAULT_ORIGIN, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals("true", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
		assertEquals(CorsHeaders.ORIGIN, response.getHeaderString(HttpHeaders.VARY));
		assertEquals("eh0,eh1", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
	}

	@Test
	public void testActualOptionsCorsRequest() {
		Response response = target()
				.path("options")
				.request()
				.header(CorsHeaders.ORIGIN, DEFAULT_ORIGIN)
				.options();
		assertEquals(200, response.getStatus());
		assertEquals(DEFAULT_ORIGIN, response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
		assertEquals("true", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
		assertEquals(CorsHeaders.ORIGIN, response.getHeaderString(HttpHeaders.VARY));
		assertEquals("eh0,eh1", response.getHeaderString(CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));

		assertNull(response.getHeaderString(CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS));
		assertNull(response.getHeaderString(CorsHeaders.ACCESS_CONTROL_MAX_AGE));
	}

	@Test
	public void testSameOriginPreflight() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ORIGIN, SAME_ORGIN)
				.header(CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.DELETE)
				.options();
		assertEquals(200, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Test
	public void testSameOriginActual() {
		Response response = target()
				.path("delete")
				.request()
				.header(CorsHeaders.ORIGIN, SAME_ORGIN)
				.delete();
		assertEquals(200, response.getStatus());
		assertNoCorsHeaders(response.getHeaders());
	}

	@Path("/")
	public static class TestResource {

		@Path("/delete")
		@DELETE
		public Response delete() {
			return Response.ok().build();
		}

		@Path("/options")
		@OPTIONS
		public Response options() {
			return Response.ok().build();
		}
	}

	private static void assertNoCorsHeaders(MultivaluedMap<String, Object> headers) {
		assertFalse(headers.keySet().stream().anyMatch(h -> !h.equals(HttpHeaders.CONTENT_LENGTH)
				&& !h.equals(HttpHeaders.DATE) && !h.equals(HttpHeaders.CONTENT_TYPE) && !h.equals(HttpHeaders.ALLOW)));
	}
}
