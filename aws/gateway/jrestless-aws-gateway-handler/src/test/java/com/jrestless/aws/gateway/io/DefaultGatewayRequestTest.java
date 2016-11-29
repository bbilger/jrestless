package com.jrestless.aws.gateway.io;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultGatewayRequestTest {

	@Test(expected = UnsupportedOperationException.class)
	public void testHeadersReturnsUnmodifiableMap() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testReturnsUnmodifiableMap(request::setHeaders, request::getHeaders);
	}

	@Test
	public void testHeadersClearsValues() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValues(request::setHeaders, request::getHeaders);
	}

	@Test
	public void testHeadersClearsValuesOnNull() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValuesOnNull(request::setHeaders, request::getHeaders);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testQueryStringParametersReturnsUnmodifiableMap() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testReturnsUnmodifiableMap(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test
	public void testQueryStringParametersClearsValues() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValues(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test
	public void testQueryStringParametersClearsValuesOnNull() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValuesOnNull(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testPathParameterReturnsUnmodifiableMap() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testReturnsUnmodifiableMap(request::setPathParameters, request::getPathParameters);
	}

	@Test
	public void testPathParameterClearsValues() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValues(request::setPathParameters, request::getPathParameters);
	}

	@Test
	public void testPathParameterClearsValuesOnNull() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValuesOnNull(request::setPathParameters, request::getPathParameters);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testStageVariablesReturnsUnmodifiableMap() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testReturnsUnmodifiableMap(request::setStageVariables, request::getStageVariables);
	}

	@Test
	public void testStageVariablesClearsValues() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValues(request::setStageVariables, request::getStageVariables);
	}

	@Test
	public void testStageVariablesClearsValuesOnNull() {
		DefaultGatewayRequest request = new DefaultGatewayRequest();
		testMapClearsValuesOnNull(request::setStageVariables, request::getStageVariables);
	}

	private void testReturnsUnmodifiableMap(Consumer<Map<String, String>> setter, Supplier<Map<String, String>> getter) {
		setter.accept(new HashMap<>());
		getter.get().put("1", "1");
	}

	private void testMapClearsValues(Consumer<Map<String, String>> setter, Supplier<Map<String, String>> getter) {
		setter.accept(ImmutableMap.of("1", "1"));
		setter.accept(ImmutableMap.of("2", "2"));
		assertEquals(1, getter.get().size());
		assertEquals("2", getter.get().get("2"));
	}

	public void testMapClearsValuesOnNull(Consumer<Map<String, String>> setter, Supplier<Map<String, String>> getter) {
		setter.accept(ImmutableMap.of("1", "1"));
		setter.accept(null);
		assertEquals(0, getter.get().size());
	}

	@Test
	public void testEquals() {
		DefaultGatewayRequestContext requestContext = new DefaultGatewayRequestContext();
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "resource")
			.addArguments(1, null, "path")
			.addArguments(2, null, "httpMethod")
			.addArguments(3, null, ImmutableMap.of("headers", "headers"))
			.addArguments(4, null, ImmutableMap.of("queryStringParameters", "queryStringParameters"))
			.addArguments(5, null, ImmutableMap.of("pathParameters", "pathParameters"))
			.addArguments(6, null, ImmutableMap.of("stageVariables", "stageVariables"))
			.addArguments(7, null, requestContext)
			.addArguments(8, null, "body")
			.testEquals();
	}

	@Test
	public void testGetters() {
		DefaultGatewayRequestContext requestContext = new DefaultGatewayRequestContext();
		DefaultGatewayRequest request = new DefaultGatewayRequest("resource", "path", "httpMethod",
				ImmutableMap.of("headers", "headers"),
				ImmutableMap.of("queryStringParameters", "queryStringParameters"),
				ImmutableMap.of("pathParameters", "pathParameters"),
				ImmutableMap.of("stageVariables", "stageVariables"), requestContext, "body");

		assertEquals("resource", request.getResource());
		assertEquals("path", request.getPath());
		assertEquals("httpMethod", request.getHttpMethod());
		assertEquals(ImmutableMap.of("headers", "headers"), request.getHeaders());
		assertEquals(ImmutableMap.of("queryStringParameters", "queryStringParameters"), request.getQueryStringParameters());
		assertEquals(ImmutableMap.of("pathParameters", "pathParameters"), request.getPathParameters());
		assertEquals(ImmutableMap.of("stageVariables", "stageVariables"), request.getStageVariables());
		assertEquals(requestContext, request.getRequestContext());
		assertEquals("body", request.getBody());
	}

	private Constructor<DefaultGatewayRequest> getConstructor() {
		try {
			return DefaultGatewayRequest.class.getDeclaredConstructor(String.class, String.class, String.class, Map.class,
					Map.class, Map.class, Map.class, DefaultGatewayRequestContext.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
