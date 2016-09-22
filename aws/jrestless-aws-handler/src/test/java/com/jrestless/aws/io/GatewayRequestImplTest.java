package com.jrestless.aws.io;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;

import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

public class GatewayRequestImplTest {

	@Test(expected = UnsupportedOperationException.class)
	public void testHeadersReturnsImmutableMap() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testReturnsImmutableMap(request::setHeaders, request::getHeaders);
	}

	@Test
	public void testHeadersCopiesValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapCopiesValues(request::setHeaders, request::getHeaders);
	}

	@Test
	public void testHeadersClearsValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValues(request::setHeaders, request::getHeaders);
	}

	@Test
	public void testHeadersClearsValuesOnNull() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValuesOnNull(request::setHeaders, request::getHeaders);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testQueryStringParametersReturnsImmutableMap() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testReturnsImmutableMap(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test
	public void testQueryStringParametersCopiesValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapCopiesValues(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test
	public void testQueryStringParametersClearsValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValues(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test
	public void testQueryStringParametersClearsValuesOnNull() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValuesOnNull(request::setQueryStringParameters, request::getQueryStringParameters);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testPathParameterReturnsImmutableMap() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testReturnsImmutableMap(request::setPathParameters, request::getPathParameters);
	}

	@Test
	public void testPathParameterCopiesValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapCopiesValues(request::setPathParameters, request::getPathParameters);
	}

	@Test
	public void testPathParameterClearsValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValues(request::setPathParameters, request::getPathParameters);
	}

	@Test
	public void testPathParameterClearsValuesOnNull() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValuesOnNull(request::setPathParameters, request::getPathParameters);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testStageVariablesReturnsImmutableMap() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testReturnsImmutableMap(request::setStageVariables, request::getStageVariables);
	}

	@Test
	public void testStageVariablesCopiesValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapCopiesValues(request::setStageVariables, request::getStageVariables);
	}

	@Test
	public void testStageVariablesClearsValues() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValues(request::setStageVariables, request::getStageVariables);
	}

	@Test
	public void testStageVariablesClearsValuesOnNull() {
		GatewayRequestImpl request = new GatewayRequestImpl();
		testMapClearsValuesOnNull(request::setStageVariables, request::getStageVariables);
	}

	private void testReturnsImmutableMap(Consumer<Map<String, String>> setter, Supplier<Map<String, String>> getter) {
		setter.accept(new HashMap<>());
		getter.get().put("1", "1");
	}

	private void testMapCopiesValues(Consumer<Map<String, String>> setter, Supplier<Map<String, String>> getter) {
		Map<String, String> stageVariables = new HashMap<>();
		stageVariables.put("1", "1");
		stageVariables.put("2", "2");
		setter.accept(stageVariables);
		stageVariables.remove("1");
		assertEquals(2, getter.get().size());
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
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
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
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl();
		GatewayRequestImpl request = new GatewayRequestImpl("resource", "path", "httpMethod",
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

	private Constructor<GatewayRequestImpl> getConstructor() {
		try {
			return GatewayRequestImpl.class.getDeclaredConstructor(String.class, String.class, String.class, Map.class,
					Map.class, Map.class, Map.class, GatewayRequestContextImpl.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
