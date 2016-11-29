package com.jrestless.aws.service.io;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultServiceRequestTest {

	@Test
	public void testGetters() {
		ServiceRequest req0 = new DefaultServiceRequest(null, ImmutableMap.of(), URI.create("/"), "GET");
		assertEquals(null, req0.getBody());
		assertEquals(ImmutableMap.of(), req0.getHeaders());
		assertEquals(URI.create("/"), req0.getRequestUri());
		assertEquals("GET", req0.getHttpMethod());

		Map<String, List<String>> headers = ImmutableMap.of("123", ImmutableList.of("1"));
		ServiceRequest req1 = new DefaultServiceRequest("123", headers, URI.create("/1"), "POST");
		assertEquals("123", req1.getBody());
		assertEquals(headers, req1.getHeaders());
		assertEquals(URI.create("/1"), req1.getRequestUri());
		assertEquals("POST", req1.getHttpMethod());

		ServiceRequest req2 = new DefaultServiceRequest();
		assertEquals(null, req2.getBody());
		assertEquals(Collections.emptyMap(), req2.getHeaders());
		assertEquals(null, req2.getRequestUri());
		assertEquals(null, req2.getHttpMethod());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testHeaderMapImmutability() {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("0", emptyList());
		ServiceRequest req = new DefaultServiceRequest(null, headers, URI.create("/"), "GET");
		Map<String, List<String>> requestHeaders = req.getHeaders();
		requestHeaders.put("0", emptyList());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testHeaderValueListImmutability() {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> headerValues = new ArrayList<>();
		headers.put("0", headerValues);
		ServiceRequest req = new DefaultServiceRequest(null, headers, URI.create("/"), "GET");
		Map<String, List<String>> requestHeaders = req.getHeaders();
		requestHeaders.get("0").add("1");
	}

	@Test
	public void testHeaderMapCopied() {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("0", emptyList());
		ServiceRequest req = new DefaultServiceRequest(null, headers, URI.create("/"), "GET");
		headers.put("1", emptyList());
		assertEquals(1, req.getHeaders().size());
	}

	@Test
	public void testHeaderValueListCopied() {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> headerValues = new ArrayList<>();
		headerValues.add("0");
		headers.put("0", headerValues);
		ServiceRequest req = new DefaultServiceRequest(null, headers, URI.create("/"), "GET");
		headerValues.add("1");
		assertEquals(1, req.getHeaders().get("0").size());
	}

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
			// body
			.addArguments(0, null, "", "123")
			// headers
			.addArguments(1, ImmutableMap.of(), ImmutableMap.of("123", ImmutableList.of()), ImmutableMap.of("123", ImmutableList.of("1")))
			// requestUri
			.addArguments(2, URI.create("/"), URI.create("/a"))
			// httpMethod
			.addArguments(3, "GET", "POST")
			.testEquals();
	}

	@Test
	public void testHeaderSpecificEquality() {
		EqualsTester equalTester = new EqualsTester();

		Map<String, List<String>> headers0 = new HashMap<>();
		headers0.put("1", singletonList("1_0"));

		Map<String, List<String>> invalidHeaders00 = new HashMap<>();
		invalidHeaders00.put(null, singletonList("0_0"));
		invalidHeaders00.put("1", singletonList("1_0"));

		Map<String, List<String>> invalidHeaders01 = new HashMap<>();
		invalidHeaders01.put("1", singletonList("1_0"));
		invalidHeaders01.put("0", null);

		Map<String, List<String>> headers1 = new HashMap<>();

		Map<String, List<String>> headers2 = new HashMap<>();
		headers2.put("1", emptyList());

		ServiceRequest req00 = new DefaultServiceRequest(null, invalidHeaders00, URI.create("/"), "GET");
		ServiceRequest req01 = new DefaultServiceRequest(null, invalidHeaders01, URI.create("/"), "GET");
		ServiceRequest req02 = new DefaultServiceRequest(null, headers0, URI.create("/"), "GET");

		ServiceRequest req10 = new DefaultServiceRequest(null, headers1, URI.create("/"), "GET");

		ServiceRequest req20 = new DefaultServiceRequest(null, headers2, URI.create("/"), "GET");

		equalTester
			.addEqualityGroup(req00, req01, req02)
			.addEqualityGroup(req10)
			.addEqualityGroup(req20)
			.testEquals();
	}

	@Test
	public void testPreconditions() {
		Map<String, List<String>> nullHeader = new HashMap<>();
		nullHeader.put(null, singletonList("1"));
		Map<String, String> nullHeaderValue = new HashMap<>();
		nullHeader.put("0", null);
		Map<String, List<String>> headers = singletonMap("0", singletonList("1"));
		new ConstructorPreconditionsTester(getConstructor())
			// body
			.addValidArgs(0, null, "body")
			// headers
			.addValidArgs(1, ImmutableMap.of(), nullHeader, nullHeaderValue, headers)
			.addInvalidNpeArg(1)
			// requestUri
			.addValidArgs(2, URI.create("/"))
			.addInvalidNpeArg(2)
			// httpMethod
			.addValidArgs(3, "GET")
			.addInvalidNpeArg(3)
			.testPreconditionsAndValidCombinations();
	}

	private Constructor<DefaultServiceRequest> getConstructor() {
		try {
			return DefaultServiceRequest.class.getConstructor(String.class, Map.class, URI.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
