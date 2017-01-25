package com.jrestless.core.container.io;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

public class RequestAndBaseUriTest {
	@Test
	public void constructor_BaseUriAndRequestUriGiven_ReturnsUrisAsPassed() {
		URI baseUri = URI.create("baseUri");
		URI requestUri = URI.create("requestUri");
		RequestAndBaseUri pair = new RequestAndBaseUri(baseUri, requestUri);
		assertEquals(baseUri, pair.getBaseUri());
		assertEquals(requestUri, pair.getRequestUri());
	}

	@Test
	public void constructor_NullBaseUriAndNullRequestUriGiven_ReturnsUrisAsPassed() {
		RequestAndBaseUri pair = new RequestAndBaseUri(null, null);
		assertEquals(null, pair.getBaseUri());
		assertEquals(null, pair.getRequestUri());
	}
}
