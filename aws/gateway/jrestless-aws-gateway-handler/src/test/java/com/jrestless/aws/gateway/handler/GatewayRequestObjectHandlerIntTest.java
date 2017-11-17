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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.gateway.GatewayFeature;
import com.jrestless.aws.gateway.filter.DynamicProxyBasePathFilter;
import com.jrestless.aws.gateway.io.DefaultGatewayRequest;
import com.jrestless.aws.gateway.io.GatewayBinaryResponseFilter;
import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.aws.gateway.util.DefaultGatewayRequestBuilder;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;
import com.jrestless.core.container.dpi.InstanceBinder;
import com.jrestless.test.IOUtils;

public class GatewayRequestObjectHandlerIntTest {

	private GatewayRequestObjectHandlerImpl handler;
	private TestService testService;
	private Context context = mock(Context.class);

	@Before
	public void setup() {
		testService = mock(TestService.class);
		handler = createAndStartHandler(new ResourceConfig(), testService);
	}

	@After
	public void tearDown() {
		handler.stop();
	}

	private GatewayRequestObjectHandlerImpl createAndStartHandler(ResourceConfig config, TestService testService) {
		config.register(GatewayFeature.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		config.register(EncodingFilter.class);
		config.register(GZipEncoder.class);
		config.register(SomeCheckedAppExceptionMapper.class);
		config.register(SomeUncheckedAppExceptionMapper.class);
		config.register(GlobalExceptionMapper.class);
		GatewayRequestObjectHandlerImpl handler = new GatewayRequestObjectHandlerImpl();
		handler.init(config);
		handler.start();
		return handler;
	}

	@Test
	public void testLambdaContextInjection() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("DELETE")
				.resource("/")
				.build();
		handler.handleRequest(request, context);
		verify(testService).injectLambdaContext(context);
	}

	@Test
	public void testLambdaContextMemberInjection() {
		when(context.getAwsRequestId()).thenReturn("0", "1");
		for (int i = 0; i <= 1; i++) {
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/inject-lambda-context-member" + i).build();
			handler.handleRequest(request, context);
			verify(testService).injectedStringArg("" + i);
		}
	}

	@Test
	public void testGatewayRequestInjection() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("PUT")
				.resource("/inject-gateway-request")
				.build();
		handler.handleRequest(request, context);
		verify(testService).injectGatewayRequest(same(request));
	}

	@Test
	public void testGatewayRequestMemberInjection() {
		String path0 = "/inject-gateway-request-member0";
		DefaultGatewayRequest request0 = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource(path0)
				.build();
		String path1 = "/inject-gateway-request-member1";
		DefaultGatewayRequest request1 = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource(path1)
				.build();
		testProxy(request0, request1, path0, path1);
	}

	@Test
	public void testContainerFailureCreates500() {
		GatewayRequestObjectHandlerImpl throwingHandler = null;
		try {
			throwingHandler = spy(createAndStartHandler(new ResourceConfig(), testService));
			DefaultGatewayRequest request = new DefaultGatewayRequest();
			doThrow(new RuntimeException()).when(throwingHandler).createContainerRequest(any());
			GatewayResponse response = throwingHandler.handleRequest(request, context);
			assertEquals(new GatewayResponse(null, new HashMap<>(), Status.INTERNAL_SERVER_ERROR, false), response);
		} finally {
			throwingHandler.stop();
		}
	}

	@Test
	public void testRoundTrip() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> requestHeaders = ImmutableMap.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON,
				HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		String requestBody = mapper.writeValueAsString(new Entity("123"));

		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("POST")
				.resource("/round-trip")
				.body(requestBody)
				.headers(requestHeaders)
				.build();
		GatewayResponse response = handler.handleRequest(request, context);
		/*
		 * check for the vary header is only necessary because we registered
		 * org.glassfish.jersey.server.filter.EncodingFilter
		 */
		Map<String, String> responseHeaders = ImmutableMap.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON,
				HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
		String responseBody = mapper.writeValueAsString(new Entity("123"));
		assertEquals(new GatewayResponse(responseBody, responseHeaders, Status.OK, false), response);
	}

	private void testProxy(DefaultGatewayRequest request0, DefaultGatewayRequest request1,
			String expectedArg0, String expectedArg1) {
		handler.handleRequest(request0, context);
		verify(testService).injectedStringArg(expectedArg0);
		handler.handleRequest(request1, context);
		verify(testService).injectedStringArg(expectedArg1);
	}

	@Test
	public void testBase64EncodingOfStreamingOutput() {
		testBase64Encoding("/streaming-output");
	}

	@Test
	public void testBase64EncodingOfInputStream() {
		testBase64Encoding("/input-stream");
	}

	@Test
	public void testBase64EncodingOfByteArray() {
		testBase64Encoding("/byte-array");
	}

	@Test
	public void testBase64EncodingOfFile() {
		testBase64Encoding("/file");
	}

	@Test
	public void testBase64EncodingOfDataSource() {
		testBase64Encoding("/data-source");
	}

	@Test
	public void testBinaryBase64EncodingWithContentEncoding() throws IOException {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/byte-array")
				.headers(ImmutableMap.of(HttpHeaders.ACCEPT_ENCODING, "gzip"))
				.build();

		GatewayResponse response = handler.handleRequest(request, context);
		assertTrue(response.isIsBase64Encoded());
		byte[] bytes = Base64.getDecoder().decode(response.getBody());
		InputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
		assertEquals("test", IOUtils.toString(unzipStream));
		assertNotNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
	}

	@Test
	public void testNonBinaryNonBase64EncodingWithContentEncoding() throws IOException {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/test-string")
				.headers(ImmutableMap.of(HttpHeaders.ACCEPT_ENCODING, "gzip"))
				.build();

		GatewayResponse response = handler.handleRequest(request, context);
		assertFalse(response.isIsBase64Encoded());
		assertEquals("test", new String(response.getBody()));
		assertNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
	}

	private void testBase64Encoding(String resoruce) {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource(resoruce)
				.build();
		GatewayResponse response = handler.handleRequest(request, context);
		assertTrue(response.isIsBase64Encoded());
		assertEquals(Base64.getEncoder().encodeToString("test".getBytes()), response.getBody());
		assertFalse(response.getHeaders().containsKey(GatewayBinaryResponseFilter.HEADER_BINARY_RESPONSE));
	}

	@Test
	public void testBase64Decoding() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("PUT")
				.resource("/binary-data")
				.body(new String(Base64.getEncoder().encode("test".getBytes()), StandardCharsets.UTF_8))
				.base64Encoded(true)
				.build();
		handler.handleRequest(request, context);
		verify(testService).binaryData("test".getBytes());
	}

	@Test
	public void testEncodedBase64Decoding() throws IOException {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("PUT")
				.resource("/binary-data")
				.body(new String(Base64.getEncoder().encode("test".getBytes()), StandardCharsets.UTF_8))
				.base64Encoded(true)
				.headers(Collections.singletonMap(HttpHeaders.CONTENT_ENCODING, "gzip"))
				.build();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream zipOut = new GZIPOutputStream(baos, true)) {
			zipOut.write("test".getBytes());
		} // finish + flush
		request.setBody(Base64.getEncoder().encodeToString(baos.toByteArray()));
		handler.handleRequest(request, context);
		verify(testService).binaryData("test".getBytes());
	}

	@Test
	public void testNoPrincipalWithNullAuthorizer() {
		assertNull(testPrincipal(null));
	}

	@Test
	public void testNoPrincipalWithEmptyAuthorizer() {
		assertNull(testPrincipal(Collections.emptyMap()));
	}

	@Test
	public void testCognitoCustomAuthorizerPrincipal() {
		Map<String, Object> authorizerDate = new HashMap<>();
		authorizerDate.put("principalId", "123");
		authorizerDate.put("custom:value", "blub");
		Principal principal = testPrincipal(authorizerDate);
		assertTrue(principal instanceof CustomAuthorizerPrincipal);
		CustomAuthorizerPrincipal cognitoCustomPrincipal = (CustomAuthorizerPrincipal) principal;
		assertEquals("123", cognitoCustomPrincipal.getName());
		assertEquals("123", cognitoCustomPrincipal.getClaims().getPrincipalId());
		assertEquals("blub", cognitoCustomPrincipal.getClaims().getAllClaims().get("custom:value"));
	}

	@Test
	public void testCognitoUserPoolAuthorizerPrincipal() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", "123");
		Map<String, Object> authorizerData = new HashMap<>();
		authorizerData.put("claims", claims);
		Principal principal = testPrincipal(authorizerData);
		assertTrue(principal instanceof CognitoUserPoolAuthorizerPrincipal);
		CognitoUserPoolAuthorizerPrincipal cognitoUserPoolPrincipal = (CognitoUserPoolAuthorizerPrincipal) principal;
		assertEquals("123", cognitoUserPoolPrincipal.getName());
		assertNotNull(cognitoUserPoolPrincipal.getClaims());
		assertEquals("123", cognitoUserPoolPrincipal.getClaims().getSub());
		assertEquals("123", cognitoUserPoolPrincipal.getClaims().getAllClaims().get("sub"));
	}

	private Principal testPrincipal(Map<String, Object> authorizerData) {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/security-context")
				.authorizerData(authorizerData)
				.build();
		ArgumentCaptor<SecurityContext> securityContextCapture = ArgumentCaptor.forClass(SecurityContext.class);
		handler.handleRequest(request, context);
		verify(testService).injectSecurityContext(securityContextCapture.capture());
		SecurityContext sc = securityContextCapture.getValue();
		return sc.getUserPrincipal();
	}

	@Test
	public void testBaseUriWithoutHost() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/uris")
				.build();
		handler.handleRequest(request, context);
		verify(testService).baseUri(URI.create("/"));
		verify(testService).requestUri(URI.create("/uris"));
	}

	@Test
	public void testBaseUriWithHost() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/uris")
				.domain("api.example.com")
				.build();
		handler.handleRequest(request, context);
		verify(testService).baseUri(URI.create("https://api.example.com/"));
		verify(testService).requestUri(URI.create("https://api.example.com/uris"));
	}

	@Test
	public void testAppPathWithoutHost() {
		GatewayRequestObjectHandlerImpl handlerWithAppPath = null;
		try {
			handlerWithAppPath = createAndStartHandler(new ApiResourceConfig(), testService);
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/api/uris") // = path
					.build();
			handlerWithAppPath.handleRequest(request, context);
			verify(testService).baseUri(URI.create("/api/"));
			verify(testService).requestUri(URI.create("/api/uris"));
		} finally {
			handlerWithAppPath.stop();
		}
	}

	@Test
	public void testAppPathWithHost() {
		GatewayRequestObjectHandlerImpl handlerWithAppPath = null;
		try {
			handlerWithAppPath = createAndStartHandler(new ApiResourceConfig(), testService);
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/api/uris") // = path
					.domain("api.example.com")
					.build();
			handlerWithAppPath.handleRequest(request, context);
			verify(testService).baseUri(URI.create("https://api.example.com/api/"));
			verify(testService).requestUri(URI.create("https://api.example.com/api/uris"));
		} finally {
			handlerWithAppPath.stop();
		}
	}

	@Test
	public void testBasePathWithBasePathWithHost() {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource("/uris")
				.path("/api/uris")
				.domain("api.example.com")
				.build();
		handler.handleRequest(request, context);
		verify(testService).baseUri(URI.create("https://api.example.com/api/"));
		verify(testService).requestUri(URI.create("https://api.example.com/api/uris"));
	}

	@Test
	public void testProxyBasePathingWithoutDomainWithoutPathBasePath() {
		ResourceConfig config = new ResourceConfig();
		config.register(DynamicProxyBasePathFilter.class);
		GatewayRequestObjectHandlerImpl handlerWithProxyFilter = null;
		try {
			handlerWithProxyFilter = createAndStartHandler(config, testService);
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/a/{proxy+}")
					.pathParams(Collections.singletonMap("proxy", "uris"))
					.build();
			handlerWithProxyFilter.handleRequest(request, context);
			verify(testService).baseUri(URI.create("/a/"));
			verify(testService).requestUri(URI.create("/a/uris"));
		} finally {
			handlerWithProxyFilter.stop();
		}
	}

	@Test
	public void testProxyBasePathingWithDomainWithoutPathBasePath() {
		ResourceConfig config = new ResourceConfig();
		config.register(DynamicProxyBasePathFilter.class);
		GatewayRequestObjectHandlerImpl handlerWithProxyFilter = null;
		try {
			handlerWithProxyFilter = createAndStartHandler(config, testService);
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/a/{proxy+}")
					.pathParams(Collections.singletonMap("proxy", "uris"))
					.domain("api.example.com")
					.build();
			handlerWithProxyFilter.handleRequest(request, context);
			verify(testService).baseUri(URI.create("https://api.example.com/a/"));
			verify(testService).requestUri(URI.create("https://api.example.com/a/uris"));
		} finally {
			handlerWithProxyFilter.stop();
		}
	}

	@Test
	public void testProxyBasePathingWithDomainWithPathBasePath() {
		ResourceConfig config = new ResourceConfig();
		config.register(DynamicProxyBasePathFilter.class);
		GatewayRequestObjectHandlerImpl handlerWithProxyFilter = null;
		try {
			handlerWithProxyFilter = createAndStartHandler(config, testService);
			DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
					.httpMethod("GET")
					.resource("/p/{proxy+}")
					.pathParams(Collections.singletonMap("proxy", "uris"))
					.path("/a/p/uris")
					.domain("api.example.com")
					.build();
			handlerWithProxyFilter.handleRequest(request, context);
			verify(testService).baseUri(URI.create("https://api.example.com/a/p/"));
			verify(testService).requestUri(URI.create("https://api.example.com/a/p/uris"));
		} finally {
			handlerWithProxyFilter.stop();
		}
	}

	@Test
	public void testSpecificCheckedException() {
		testException("/specific-checked-exception", SomeCheckedAppExceptionMapper.class);
	}

	@Test
	public void testSpecificUncheckedException() {
		testException("/specific-unchecked-exception", SomeUncheckedAppExceptionMapper.class);
	}

	@Test
	public void testUnspecificCheckedException() {
		testException("/unspecific-checked-exception", GlobalExceptionMapper.class);
	}

	@Test
	public void testUnspecificUncheckedException() {
		testException("/unspecific-unchecked-exception", GlobalExceptionMapper.class);
	}

	private void testException(String resource, Class<? extends ExceptionMapper<?>> exceptionMapper) {
		DefaultGatewayRequest request = new DefaultGatewayRequestBuilder()
				.httpMethod("GET")
				.resource(resource)
				.build();

		GatewayResponse response = handler.handleRequest(request, context);
		assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
		assertEquals(exceptionMapper.getSimpleName(), response.getBody());
	}

	@Path("/")
	@Singleton // singleton in order to test proxies
	public static class TestResource {

		@javax.ws.rs.core.Context
		private Context lambdaContextMember;

		@javax.ws.rs.core.Context
		private GatewayRequest gatewayRequestMember;

		private final TestService service;
		private final UriInfo uriInfo;

		@Inject
		public TestResource(TestService service, UriInfo uriInfo) {
			this.service = service;
			this.uriInfo = uriInfo;
		}

		@DELETE
		public Response injectLambdaContext(@javax.ws.rs.core.Context Context context) {
			service.injectLambdaContext(context);
			return Response.ok().build();
		}

		@Path("/inject-lambda-context-member0")
		@GET
		public Response injectLambdaContextAsMember0() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
			return Response.ok().build();
		}

		@Path("/inject-lambda-context-member1")
		@GET
		public Response injectLambdaContextAsMember1() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
			return Response.ok().build();
		}

		@Path("/inject-gateway-request")
		@PUT
		public Response injectGatewayRequest(@javax.ws.rs.core.Context GatewayRequest request) {
			service.injectGatewayRequest(request);
			return Response.ok().build();
		}

		@Path("/inject-gateway-request-member0")
		@GET
		public Response injectGatewayRequestAsMember0() {
			service.injectedStringArg(gatewayRequestMember.getPath());
			return Response.ok().build();
		}

		@Path("/inject-gateway-request-member1")
		@GET
		public Response injectGatewayRequestAsMember1() {
			service.injectedStringArg(gatewayRequestMember.getPath());
			return Response.ok().build();
		}

		@Path("/round-trip")
		@POST
		public Response putSomething(Entity entity) {
			return Response.ok(entity).build();
		}

		@Path("/streaming-output")
		@GET
		public StreamingOutput getStreamingOutput() {
			return new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					output.write("test".getBytes());
				}
			};
		}

		@Path("/input-stream")
		@GET
		public InputStream getInputStream() {
			return new ByteArrayInputStream("test".getBytes());
		}

		@Path("/byte-array")
		@GET
		public byte[] getByteArray() {
			return "test".getBytes();
		}

		@Path("/file")
		@GET
		public File getFile() throws IOException {
			File file = File.createTempFile("some-test-file", ".test");
			try (OutputStream os = new FileOutputStream(file)) {
				os.write("test".getBytes());
			}
			return file;
		}

		@Path("/data-source")
		@GET
		public DataSource getDataSrouce() {
			return new DataSource() {
				@Override
				public OutputStream getOutputStream() throws IOException {
					return null;
				}
				@Override
				public String getName() {
					return null;
				}
				@Override
				public InputStream getInputStream() throws IOException {
					return new ByteArrayInputStream("test".getBytes());
				}
				@Override
				public String getContentType() {
					return null;
				}
			};
		}

		@Path("/binary-data")
		@PUT
		public void putBinary(byte[] in) {
			service.binaryData(in);
		}

		@Path("/test-string")
		@GET
		public String getTestString() {
			return "test";
		}

		@Path("/security-context")
		@GET
		public void getSc(@javax.ws.rs.core.Context SecurityContext securityContext) {
			service.injectSecurityContext(securityContext);
		}

		@Path("uris")
		@GET
		public void getBaseUri() {
			service.baseUri(uriInfo.getBaseUri());
			service.requestUri(uriInfo.getRequestUri());
		}

		@Path("specific-checked-exception")
		@GET
		public void throwSpecificCheckedException() throws SomeCheckedAppException {
			throw new SomeCheckedAppException();
		}

		@Path("specific-unchecked-exception")
		@GET
		public void throwSpecificUncheckedException() {
			throw new SomeUncheckedAppException();
		}

		@Path("unspecific-checked-exception")
		@GET
		public void throwUnspecificCheckedException() throws FileNotFoundException {
			throw new FileNotFoundException();
		}

		@Path("unspecific-unchecked-exception")
		@GET
		public void throwUnspecificUncheckedException() {
			throw new RuntimeException();
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectGatewayRequest(GatewayRequest request);
		void injectedStringArg(String arg);
		void injectGatewayRequestContext(GatewayRequestContext requestContext);
		void injectGatewayIdentity(GatewayIdentity identity);
		void binaryData(byte[] data);
		void injectSecurityContext(SecurityContext sc);
		void baseUri(URI baseUri);
		void requestUri(URI baseUri);
	}

	public static class Entity {
		private String value;

		@JsonCreator
		public Entity(@JsonProperty("value") String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (!getClass().equals(other.getClass())) {
				return false;
			}
			Entity castOther = (Entity) other;
			return Objects.equals(value, castOther.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class GatewayRequestObjectHandlerImpl extends GatewayRequestObjectHandler {
	}

	@ApplicationPath("api")
	public static class ApiResourceConfig extends ResourceConfig {
	}

	public static class SomeCheckedAppException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	@Provider
	public static class SomeCheckedAppExceptionMapper implements ExceptionMapper<SomeCheckedAppException> {
		@Override
		public Response toResponse(SomeCheckedAppException exception) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(SomeCheckedAppExceptionMapper.class.getSimpleName()).build();
		}
	}

	public static class SomeUncheckedAppException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	@Provider
	public static class SomeUncheckedAppExceptionMapper implements ExceptionMapper<SomeUncheckedAppException> {
		@Override
		public Response toResponse(SomeUncheckedAppException exception) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity(SomeUncheckedAppExceptionMapper.class.getSimpleName()).build();
		}
	}

	@Provider
	public static class GlobalExceptionMapper implements ExceptionMapper<Exception> {
		@Override
		public Response toResponse(Exception exception) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(GlobalExceptionMapper.class.getSimpleName())
					.build();
		}
	}
}
