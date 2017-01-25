package com.jrestless.core.container.io;

import java.net.URI;

/**
 * Pair containing the request and base {@link URI}.
 *
 * @author Bjoern Bilger
 *
 */
public class RequestAndBaseUri {
	private final URI baseUri;
	private final URI requestUri;
	public RequestAndBaseUri(URI baseUri, URI requestUri) {
		this.baseUri = baseUri;
		this.requestUri = requestUri;
	}
	public URI getBaseUri() {
		return baseUri;
	}
	public URI getRequestUri() {
		return requestUri;
	}
}
