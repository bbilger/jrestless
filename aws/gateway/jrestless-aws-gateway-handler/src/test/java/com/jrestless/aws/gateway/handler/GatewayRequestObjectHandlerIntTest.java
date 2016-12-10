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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
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
import com.jrestless.aws.gateway.io.DefaultGatewayRequest;
import com.jrestless.aws.gateway.io.DefaultGatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayBinaryResponseCheckFilter;
import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;
import com.jrestless.core.container.dpi.InstanceBinder;

public class GatewayRequestObjectHandlerIntTest {

	private static final Logger LOGGER = Logger.getLogger(GatewayRequestObjectHandlerIntTest.class.getName());

	private GatewayRequestObjectHandlerImpl handler;
	private TestService testService;
	private Context context = mock(Context.class);

	@Before
	public void setup() {
		ResourceConfig config = new ResourceConfig();
		config.register(GatewayFeature.class);
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		config.register(EncodingFilter.class);
		config.register(GZipEncoder.class);
		config.register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY));
		handler = spy(new GatewayRequestObjectHandlerImpl());
		handler.doInit(config);
		handler.doStart();
	}

	@Test
	public void testLambdaContextInjection() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		DefaultGatewayRequestContext requestContext = new DefaultGatewayRequestContext();
		request.setRequestContext(requestContext);
		request.setHttpMethod("DELETE");
		request.setPath("/");
		handler.handleRequest(request, context);
		verify(testService).injectLambdaContext(context);
	}

	@Test
	public void testLambdaContextMemberInjection() {
		when(context.getAwsRequestId()).thenReturn("0", "1");
		for (int i = 0; i <= 1; i++) {
			DefaultGatewayRequest request = new DefaultGatewayRequest();
			request.setHttpMethod("GET");
			request.setPath("/inject-lambda-context-member" + i);
			handler.handleRequest(request, context);
			verify(testService).injectedStringArg("" + i);
		}
	}

	@Test
	public void testGatewayRequestInjection() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("PUT");
		request.setPath("/inject-gateway-request");
		handler.handleRequest(request, context);
		verify(testService).injectGatewayRequest(same(request));
	}

	@Test
	public void testGatewayRequestMemberInjection() {
		DefaultGatewayRequest request0 = new DefaultGatewayRequest();
		request0.setHttpMethod("GET");
		String path0 = "/inject-gateway-request-member0";
		request0.setPath(path0);
		DefaultGatewayRequest request1 = new DefaultGatewayRequest();
		request1.setHttpMethod("GET");
		String path1 = "/inject-gateway-request-member1";
		request1.setPath(path1);
		testProxy(request0, request1, path0, path1);
	}

	@Test
	public void testContainerFailureCreates500() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		doThrow(new RuntimeException()).when(handler).createContainerRequest(any());
		GatewayResponse response = handler.handleRequest(request, context);
		assertEquals(new GatewayResponse(null, new HashMap<>(), Status.INTERNAL_SERVER_ERROR, false), response);
	}

	@Test
	public void testRoundTrip() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> requestHeaders = ImmutableMap.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON,
				HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		String requestBody = mapper.writeValueAsString(new Entity("123"));
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("POST");
		request.setBody(requestBody);
		request.setPath("/round-trip");
		request.setHeaders(requestHeaders);
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
	public void testBase64EncodingWithContentEncoding() throws IOException {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("GET");
		request.setPath("/test-string");
		request.setHeaders(ImmutableMap.of(HttpHeaders.ACCEPT_ENCODING, "gzip"));
		GatewayResponse response = handler.handleRequest(request, context);
		assertTrue(response.isIsBase64Encoded());
		byte[] bytes = Base64.getDecoder().decode(response.getBody());
		InputStream unzipStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
		assertEquals("test", new String(toBytes(unzipStream)));
	}

	private byte[] toBytes(InputStream is) throws IOException {
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int nRead;
	    byte[] data = new byte[1024];
	    while ((nRead = is.read(data, 0, data.length)) != -1) {
	        buffer.write(data, 0, nRead);
	    }
	    buffer.flush();
	    return buffer.toByteArray();
	}

	private void testBase64Encoding(String path) {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("GET");
		request.setPath(path);
		GatewayResponse response = handler.handleRequest(request, context);
		assertTrue(response.isIsBase64Encoded());
		assertEquals(Base64.getEncoder().encodeToString("test".getBytes()), response.getBody());
		assertFalse(response.getHeaders().containsKey(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE));
	}

	@Test
	public void testBase64Decoding() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("PUT");
		request.setPath("/binary-data");
		request.setIsBase64Encoded(true);
		request.setBody(new String(Base64.getEncoder().encode("test".getBytes()), StandardCharsets.UTF_8));
		handler.handleRequest(request, context);
		verify(testService).binaryData("test".getBytes());
	}

	@Test
	public void testEncodedBase64Decoding() throws IOException {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("PUT");
		request.setPath("/binary-data");
		request.setIsBase64Encoded(true);
		request.setHeaders(Collections.singletonMap(HttpHeaders.CONTENT_ENCODING, "gzip"));
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
		assertEquals("blub", cognitoCustomPrincipal.getClaims().getClaim("custom:value"));
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
		assertEquals("123", cognitoUserPoolPrincipal.getClaims().getClaim("sub"));
	}

	private Principal testPrincipal(Map<String, Object> authorizerData) {
		DefaultGatewayRequestContext requestContext = new DefaultGatewayRequestContext();
		requestContext.setAuthorizer(authorizerData);
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setHttpMethod("GET");
		request.setPath("/security-context");
		request.setRequestContext(requestContext);
		ArgumentCaptor<SecurityContext> securityContextCapture = ArgumentCaptor.forClass(SecurityContext.class);
		handler.handleRequest(request, context);
		verify(testService).injectSecurityContext(securityContextCapture.capture());
		SecurityContext sc = securityContextCapture.getValue();
		return sc.getUserPrincipal();
	}

	@Path("/")
	@Singleton // singleton in order to test proxies
	public static class TestResource {

		@javax.ws.rs.core.Context
		private Context lambdaContextMember;

		@javax.ws.rs.core.Context
		private GatewayRequest gatewayRequestMember;

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
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
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectGatewayRequest(GatewayRequest request);
		void injectedStringArg(String arg);
		void injectGatewayRequestContext(GatewayRequestContext requestContext);
		void injectGatewayIdentity(GatewayIdentity identity);
		void binaryData(byte[] data);
		void injectSecurityContext(SecurityContext sc);
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
		void doStart() {
			start();
		}
		void doInit(Application application) {
			init(application);
		}
	}
}
