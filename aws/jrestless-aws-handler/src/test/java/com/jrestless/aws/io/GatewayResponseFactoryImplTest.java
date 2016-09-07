package com.jrestless.aws.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GatewayResponseFactoryImplTest {

	private static final String DEFAULT_BODY = "{\"attr0\":\"val0\"}";
	private static final StatusType DEFAULT_STATUS_TYPE = Status.OK;
	private static final Map<String, List<String>> DEFAULT_HEADERS = ImmutableMap.of(
			HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON),
			HttpHeaders.LOCATION, ImmutableList.of("/1", "/2")); // doesn't make any sense, just for testing

	private static final LinkedHashMap<String, String> DEFAULT_FLATTENED_HEADERS;
	static {
		LinkedHashMap<String, String> flattenedHeaders = new LinkedHashMap<>();
		flattenedHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		flattenedHeaders.put(HttpHeaders.LOCATION, "/1,/2");
		DEFAULT_FLATTENED_HEADERS = flattenedHeaders;
	}

	private final GatewayResponseFactory factory = new GatewayResponseFactoryImpl();

	@Test(expected = IllegalArgumentException.class)
	public void init_NoContentTypeGiven_ShouldFail() {
		new GatewayResponseFactoryImpl(ImmutableList.of());
	}

	@Test
	public void init_ContentTypeGiven_ShouldInstantiate() {
		new GatewayResponseFactoryImpl(ImmutableList.of(HttpHeaders.CONTENT_TYPE));
	}

	/*
	 * ###########################################
	 * # default response tests
	 * ###########################################
	 */

	@Test(expected = NullPointerException.class)
	public void createResponse_Default_NullStatusGiven_ShouldThrowNpe() {
		factory.createResponse(DEFAULT_BODY, DEFAULT_HEADERS, null, true);
	}

	@Test(expected = NullPointerException.class)
	public void createResponse_Default_NullHeaderGiven_ShouldThrowNpe() {
		factory.createResponse(DEFAULT_BODY, null, DEFAULT_STATUS_TYPE, true);
	}

	@Test
	public void createResponse_Default_NullBodyGiven_ShouldCreateDefaultResponseWithoutBody() {
		GatewayDefaultResponse response = factory.createResponse(null, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, true);
		assertEquals(new GatewayDefaultResponse(null, DEFAULT_FLATTENED_HEADERS, DEFAULT_STATUS_TYPE), response);
	}

	@Test
	public void createResponse_Default_BodyGiven_ShouldCreateResponse() {
		GatewayDefaultResponse response = factory.createResponse(DEFAULT_BODY, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, true);
		assertEquals(new GatewayDefaultResponse(DEFAULT_BODY, DEFAULT_FLATTENED_HEADERS, DEFAULT_STATUS_TYPE), response);
	}

	@Test
	public void createResponse_Default_MoreHeadersThanNonDefaultHeadersGiven_ShouldIncludeAllHeaders() {
		Map<String, List<String>> headers = ImmutableMap.of(HttpHeaders.SET_COOKIE, ImmutableList.of("0", "1"),
				HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON));
		Map<String, String> flattenedHeaders = ImmutableMap.of(HttpHeaders.SET_COOKIE, "0,1", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		GatewayDefaultResponse response = factory.createResponse(DEFAULT_BODY, headers, DEFAULT_STATUS_TYPE, true);
		assertEquals(new GatewayDefaultResponse(DEFAULT_BODY, flattenedHeaders, DEFAULT_STATUS_TYPE), response);
	}

	@Test
	public void createResponse_Default_HeadersWithNullValuesGiven_ShouldFilterNullValues() {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> cookieHeaderValues = new ArrayList<>();
		cookieHeaderValues.add("val0");
		cookieHeaderValues.add(null);
		headers.put(HttpHeaders.SET_COOKIE, cookieHeaderValues);
		headers.put(HttpHeaders.DATE, null);
		headers.put(HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON));
		Map<String, String> flattenedHeaders = ImmutableMap.of(HttpHeaders.SET_COOKIE, "val0", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		GatewayDefaultResponse response = factory.createResponse(DEFAULT_BODY, headers, DEFAULT_STATUS_TYPE, true);
		assertEquals(new GatewayDefaultResponse(DEFAULT_BODY, flattenedHeaders, DEFAULT_STATUS_TYPE), response);
	}

	@Test
	public void createResponse_Default_StatusGiven_ShouldIncludeStatus() {
		GatewayDefaultResponse response = factory.createResponse(DEFAULT_BODY, DEFAULT_HEADERS, Status.MOVED_PERMANENTLY, true);
		assertEquals(new GatewayDefaultResponse(DEFAULT_BODY, DEFAULT_FLATTENED_HEADERS, Status.MOVED_PERMANENTLY), response);
	}

	/*
	 * ###########################################
	 * # non-default response tests
	 * ###########################################
	 */

	@Test(expected = NullPointerException.class)
	public void createResponse_NonDefaultAndNullStatusGiven_ShouldThrowNpe() {
		factory.createResponse(DEFAULT_BODY, DEFAULT_HEADERS, null, false);
	}

	@Test(expected = NullPointerException.class)
	public void createResponse_NonDefaultAndNullHeaderGiven_ShouldThrowNpe() {
		factory.createResponse(DEFAULT_BODY, null, DEFAULT_STATUS_TYPE, false);
	}

	@Test
	public void createResponse_NonDefault_NullBodyGiven_ShouldCreateDefaultResponseWithoutBody() {
		createAndAssertAdditionalResponse(factory, null, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, null, DEFAULT_FLATTENED_HEADERS, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_BodyGiven_ShouldCreateResponse() {
		createAndAssertAdditionalResponse(factory, DEFAULT_BODY, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, DEFAULT_BODY, DEFAULT_FLATTENED_HEADERS, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_NonJsonBody_ShouldCreateResponse() {
		String body =  "a\nb\"c";
		createAndAssertAdditionalResponse(factory, body, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, body, DEFAULT_FLATTENED_HEADERS, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_MoreHeadersThanNonDefaultHeadersGiven_ShouldIncludePredefinedHeadersInChainOnly() {
		Map<String, List<String>> headers = ImmutableMap.of(HttpHeaders.SET_COOKIE, ImmutableList.of("0", "1"),
				HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON));
		LinkedHashMap<String, String> flattenedHeaders = new LinkedHashMap<>();
		flattenedHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		createAndAssertAdditionalResponse(factory, DEFAULT_BODY, headers, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, DEFAULT_BODY, flattenedHeaders, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_DifferentHeaderOrderGiven_ShouldCreateHeaderChainInOrder() {
		LinkedHashMap<String, String> flattenedHeaders = new LinkedHashMap<>();
		flattenedHeaders.put(HttpHeaders.LOCATION, "/1,/2");
		flattenedHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		GatewayResponseFactory customFactory = new GatewayResponseFactoryImpl(ImmutableList.of(HttpHeaders.LOCATION, HttpHeaders.CONTENT_TYPE));
		createAndAssertAdditionalResponse(customFactory, DEFAULT_BODY, DEFAULT_HEADERS, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, DEFAULT_BODY, flattenedHeaders, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_AdditionalHeadersSet_ShouldIncludeAllSetHeadersInChain() {
		GatewayResponseFactory customFactory = new GatewayResponseFactoryImpl(ImmutableList.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.SET_COOKIE));
		Map<String, List<String>> headers = ImmutableMap.of(HttpHeaders.SET_COOKIE, ImmutableList.of("0", "1"),
				HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON));
		LinkedHashMap<String, String> flattenedHeaders = new LinkedHashMap<>();
		flattenedHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		createAndAssertAdditionalResponse(customFactory, DEFAULT_BODY, headers, DEFAULT_STATUS_TYPE, (response) -> {
			assertAdditionalResponseException(response, DEFAULT_BODY, flattenedHeaders, DEFAULT_STATUS_TYPE);
		});
	}

	@Test
	public void createResponse_NonDefault_StatusGiven_ShouldIncludeStatus() {
		createAndAssertAdditionalResponse(factory, DEFAULT_BODY, DEFAULT_HEADERS, Status.MOVED_PERMANENTLY, (response) -> {
			assertAdditionalResponseException(response, DEFAULT_BODY, DEFAULT_FLATTENED_HEADERS, Status.MOVED_PERMANENTLY);
		});
	}

	private void createAndAssertAdditionalResponse(GatewayResponseFactory factory, String body, Map<String, List<String>> headers, StatusType statusType, Consumer<GatewayAdditionalResponseException> assertConsumer) {
		try {
			factory.createResponse(body, headers, statusType, false);
			fail("expected the non-default response to be created");
		} catch (GatewayAdditionalResponseException response) {
			assertConsumer.accept(response);
		}
	}

	private void assertAdditionalResponseException(GatewayAdditionalResponseException response, String expectedBody, LinkedHashMap<String, String> expectedHeaders, StatusType expectedStatusType) {
		assertNotNull(response);
		NonDefaultResponseBody responseBody = new NonDefaultResponseBody(expectedStatusType, expectedBody);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL); // do not serialize null values
		try {
			String serializedResponseBody = mapper.writeValueAsString(responseBody);
			JSONAssert.assertEquals(serializedResponseBody, response.getMessage(), true);
			// check if the chain ordered correctly
			Throwable cause =  response.getCause();
			for (Entry<String, String> header : expectedHeaders.entrySet()) {
				assertNotNull(cause);
				assertEquals(GatewayNonDefaultHeaderException.class, cause.getClass());
				assertEquals(header.getValue(), cause.getMessage());
				cause = cause.getCause();
			}
		} catch (JsonProcessingException | JSONException e) {
			throw new RuntimeException(e);
		}
	}

	class NonDefaultResponseBody {
		private String statusCode;
		private String body;

		NonDefaultResponseBody(StatusType statusType, String body) {
			super();
			this.statusCode = "" + statusType.getStatusCode();
			this.body = body;
		}

		public String getStatusCode() {
			return statusCode;
		}

		public String getBody() {
			return body;
		}

	}

}
