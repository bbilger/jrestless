package com.jrestless.core.container.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.CopyConstructorEqualsTester;
import com.jrestless.test.IOUtils;

public class DefaultJRestlessContainerRequestTest {

	@Test
	public void testGetters() throws IOException {
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		assertEquals(URI.create("/123"), request.getBaseUri());
		assertEquals(URI.create("/456"), request.getRequestUri());
		assertEquals("123", IOUtils.toString(request.getEntityStream()));
		assertEquals(ImmutableMap.of("a", ImmutableList.of("a0", "a1")), request.getHeaders());
	}

	@Test
	public void testHeadersNotSame() throws IOException {
		Map<String, List<String>> headers = ImmutableMap.of("a", ImmutableList.of("a0", "a1"));
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), headers);
		assertNotSame(headers, request.getHeaders());
	}

	@Test
	public void testHeadersCopied() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), headers);
		headers.put("0", ImmutableList.of("0"));
		assertTrue(request.getHeaders().isEmpty());
	}

	@Test
	public void testHeadersImmutable() throws IOException {
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), new HashMap<>());
		assertThrows(UnsupportedOperationException.class, () -> request.getHeaders().put("0", ImmutableList.of()));
	}

	@Test
	public void testHeaderValuesImmutable() throws IOException {
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		assertThrows(UnsupportedOperationException.class, () -> request.getHeaders().get("a").add("123"));
	}

	@Test
	public void testNullHeaderValuesFiltered() throws IOException {
		Map<String, List<String>> headers = new HashMap<>();
		List<String> bVal = new ArrayList<>();
		bVal.add(null);
		bVal.add("b_0");
		headers.put("a", null);
		headers.put("b", bVal);
		JRestlessContainerRequest request = new DefaultJRestlessContainerRequest(URI.create("/123"), URI.create("/456"), "DELETE",
				new ByteArrayInputStream("123".getBytes()), headers);
		assertNull(request.getHeaders().get("a"));
		assertNull(request.getHeaders().get("b").get(0));
		assertEquals("b_0", request.getHeaders().get("b").get(1));
		assertEquals(1, request.getHeaders().size());
	}

	@Test
	public void construtor_NullRequestAndBaseUriPairGiven_ShouldThrowNpe() {
		assertThrows(NullPointerException.class, () -> {
			new DefaultJRestlessContainerRequest(null, "DELETE", new ByteArrayInputStream("123".getBytes()),
					ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		});
	}

	@Test
	public void construtor_NullRequestUriInPairGiven_ShouldThrowNpe() {
		assertThrows(NullPointerException.class, () -> {
			new DefaultJRestlessContainerRequest(new RequestAndBaseUri(URI.create("baseUri"), null), "DELETE",
					new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		});
	}

	@Test
	public void getBaseUri_BaseUriInPairGiven_ShouldReturnedPassedBaseUri() {
		DefaultJRestlessContainerRequest req = new DefaultJRestlessContainerRequest(
				new RequestAndBaseUri(URI.create("baseUri"), URI.create("requestUri")), "DELETE",
				new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		assertEquals(URI.create("baseUri"), req.getBaseUri());
	}

	@Test
	public void getBaseUri_NullBaseUriInPairGiven_ShouldReturnedNullBaseUri() {
		DefaultJRestlessContainerRequest req = new DefaultJRestlessContainerRequest(
				new RequestAndBaseUri(null, URI.create("requestUri")), "DELETE",
				new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		assertEquals(null, req.getBaseUri());
	}

	@Test
	public void getRequestUri_RequestUriInPairGiven_ShouldReturnedRequestUri() {
		DefaultJRestlessContainerRequest req = new DefaultJRestlessContainerRequest(
				new RequestAndBaseUri(null, URI.create("requestUri")), "DELETE",
				new ByteArrayInputStream("123".getBytes()), ImmutableMap.of("a", ImmutableList.of("a0", "a1")));
		assertEquals(URI.create("requestUri"), req.getRequestUri());
	}

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
		// baseUri
		.addArguments(0, URI.create("/"), URI.create("/123"))
		// requestUri
		.addArguments(1, URI.create("/"), URI.create("/123"))
		// httpMethod
		.addArguments(2, "GET", "POST")
		// entityStream
		.addArguments(3, new ByteArrayInputStream(new byte[0]), new ByteArrayInputStream("123".getBytes()))
		// headers
		.addArguments(4, ImmutableMap.of(), ImmutableMap.of("a", ImmutableList.of()))
		.addArguments(4, ImmutableMap.of("a", ImmutableList.of("a", "b")))
		.testEquals();
	}

	@Test
	public void testConstructorPreconditions() {
		Map<String, List<String>> nullHeader = new HashMap<>();
		nullHeader.put("headerName", null);
		new ConstructorPreconditionsTester(getConstructor())
			// baseUri
			.addValidArgs(0, URI.create("/"))
			// requestUri
			.addValidArgs(1, URI.create("/"))
			.addInvalidNpeArg(1)
			// httpMethod
			.addValidArgs(2, "GET")
			.addInvalidNpeArg(2)
			// entityStream
			.addValidArgs(3, new ByteArrayInputStream(new byte[0]))
			.addInvalidNpeArg(3)
			// headers
			.addValidArgs(4, ImmutableMap.of(), ImmutableMap.of("a", ImmutableList.of()))
			.addValidArgs(4, ImmutableMap.of("a", ImmutableList.of("a", "b")), nullHeader)
			.addInvalidNpeArg(4)
			.testPreconditionsAndValidCombinations();
	}

	private Constructor<DefaultJRestlessContainerRequest> getConstructor() {
		try {
			return DefaultJRestlessContainerRequest.class.getConstructor(URI.class, URI.class, String.class, InputStream.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
