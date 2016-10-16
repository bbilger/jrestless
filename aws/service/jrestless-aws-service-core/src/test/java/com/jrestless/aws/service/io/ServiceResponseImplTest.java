package com.jrestless.aws.service.io;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import com.jrestless.aws.service.io.ServiceResponse;
import com.jrestless.aws.service.io.ServiceResponseImpl;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class ServiceResponseImplTest {

	@Test
	public void testGetters() {
		ServiceResponse resp0 = new ServiceResponseImpl(null, emptyMap(), 200, "a");
		assertEquals(null, resp0.getBody());
		assertEquals(ImmutableMap.of(), resp0.getHeaders());
		assertEquals(200, resp0.getStatusCode());
		assertEquals("a", resp0.getReasonPhrase());

		Map<String, List<String>> headers = singletonMap("123", singletonList("1"));
		ServiceResponse resp1 = new ServiceResponseImpl("123", headers, 500, "b");
		assertEquals("123", resp1.getBody());
		assertEquals(headers, resp1.getHeaders());
		assertEquals(500, resp1.getStatusCode());
		assertEquals("b", resp1.getReasonPhrase());

		ServiceResponse resp2 = new ServiceResponseImpl();
		assertEquals(null, resp2.getBody());
		assertEquals(null, resp2.getHeaders());
		assertEquals(0, resp2.getStatusCode());
		assertEquals(null, resp2.getReasonPhrase());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testHeaderMapImmutability() {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("0", emptyList());
		ServiceResponse req = new ServiceResponseImpl(null, headers, 0, null);
		Map<String, List<String>> requestHeaders = req.getHeaders();
		requestHeaders.put("0", emptyList());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testHeaderValueListImmutability() {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> headerValues = new ArrayList<>();
		headers.put("0", headerValues);
		ServiceResponse req = new ServiceResponseImpl(null, headers, 0, null);
		Map<String, List<String>> requestHeaders = req.getHeaders();
		requestHeaders.get("0").add("1");
	}

	@Test
	public void testHeaderMapCopied() {
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("0", emptyList());
		ServiceResponse req = new ServiceResponseImpl(null, headers, 0, null);
		headers.put("1", emptyList());
		assertEquals(1, req.getHeaders().size());
	}

	@Test
	public void testHeaderValueListCopied() {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> headerValues = new ArrayList<>();
		headerValues.add("0");
		headers.put("0", headerValues);
		ServiceResponse req = new ServiceResponseImpl(null, headers, 0, null);
		headerValues.add("1");
		assertEquals(1, req.getHeaders().get("0").size());
	}

	@Test
	public void testEquals() {
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// body
			.addArguments(0, null, "", "123")
			// headers
			.addArguments(1, ImmutableMap.of(), ImmutableMap.of("123", ImmutableList.of()), ImmutableMap.of("123", ImmutableList.of("1")))
			// statusCode
			.addArguments(2, 200, 400)
			// reasonPhrase
			.addArguments(3, null, "a", "b")
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

		ServiceResponse resp00 = new ServiceResponseImpl(null, invalidHeaders00, 200, null);
		ServiceResponse resp01 = new ServiceResponseImpl(null, invalidHeaders01, 200, null);
		ServiceResponse resp02 = new ServiceResponseImpl(null, headers0, 200, null);

		ServiceResponse resp10 = new ServiceResponseImpl(null, headers1, 200, null);

		ServiceResponse resp20 = new ServiceResponseImpl(null, headers2, 200, null);

		equalTester
			.addEqualityGroup(resp00, resp01, resp02)
			.addEqualityGroup(resp10)
			.addEqualityGroup(resp20)
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
			// statusCode
			.addValidArgs(2, 500)
			// reasonPhrase
			.addValidArgs(3, null, "a")
			.testPreconditionsAndValidCombinations();
	}

	private Constructor<ServiceResponseImpl> getConstructor() {
		try {
			return ServiceResponseImpl.class.getConstructor(String.class, Map.class, int.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
