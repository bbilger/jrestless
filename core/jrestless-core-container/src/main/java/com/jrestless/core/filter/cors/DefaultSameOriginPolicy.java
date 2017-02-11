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
package com.jrestless.core.filter.cors;

import java.net.URI;
import java.util.Locale;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * Uses the request URI as a target to check the origin against in order to
 * determine if it is a same-origin request or not.
 *
 * @author Bjoern Bilger
 *
 */
public class DefaultSameOriginPolicy implements SameOriginPolicy {

	private static final int DEFAULT_HTTP_PORT = 80;
	private static final int DEFAULT_HTTPS_PORT = 443;

	@Override
	public boolean isSameOrigin(ContainerRequestContext requestContext, String origin) {
		URI requestUri = requestContext.getUriInfo().getRequestUri();
		StringBuilder target = new StringBuilder();
		String scheme = requestUri.getScheme();
		if (scheme == null) {
			return false;
		}
		scheme = scheme.toLowerCase(Locale.ENGLISH);
		target.append(scheme);
		target.append("://");
		String host = requestUri.getHost();
		if (host == null) {
			return false;
		}
		target.append(host);

		int port = requestUri.getPort();
		if (port != -1 && ("http".equals(scheme) && port != DEFAULT_HTTP_PORT
				|| "https".equals(scheme) && port != DEFAULT_HTTPS_PORT)) {
			target.append(":");
			target.append(port);
		}
		return origin.equalsIgnoreCase(target.toString());
	}
}
