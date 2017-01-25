/*
 * Copyright 2017 Bjoern Bilger
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
