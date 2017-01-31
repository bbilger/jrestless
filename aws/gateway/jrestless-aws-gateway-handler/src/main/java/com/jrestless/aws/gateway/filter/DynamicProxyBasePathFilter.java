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
package com.jrestless.aws.gateway.filter;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;

import com.jrestless.aws.gateway.io.GatewayRequest;

/**
 * This filter adds an additional base bath if the resource is a prefixed proxy
 * resource.
 * <p>
 * The filter allows you to use the very same lambda function with different
 * APIGW resources without changing any code. Let's say you have one APIGW
 * resource configured as "/v1/{proxy+}" and one configured as "/v2/{proxy+}"
 * and both invoke the very same lambda function. With this filter in place,
 * your JAX-RS resources don't need to be mapped to "/v1" or "/v2". So they are
 * agnostic to it. Note: In general this filter makes sense only if you map one
 * endpoint to a lambda function.
 * <p>
 * The detected base path is added to the base URI + a trailing slash.
 * <ol>
 * <li>If the resource contains a proxy (greedy path variable), then everything
 * before the proxy is added as additional base path to the base URI. For
 * example "/a/b" is added if the resource is "/a/b/{proxy+}".
 * <li>If there's no prefix, then no base path will be added (e.g. "/{proxy+}").
 * <li>If there's no proxy, then no base path will be added (e.g. "/a", "/",
 * "/a/{id}").
 * <li>If there the resource is misconfigured, then no base path will be added
 * (e.g. "/proxy+}", {@code null}).
 * </ol>
 * <p>
 * Note: in some situations this filter results in undesired behavior. So we
 * won't add it to the framework by default.
 *
 * @author Bjoern Bilger
 *
 */
@PreMatching
public class DynamicProxyBasePathFilter implements ContainerRequestFilter {

	private static final String PROXY_START = "/{";
	private static final String PROXY_END = "+}";

	// request-scoped proxy
	private final GatewayRequest gatwayRequest;

	@Inject
	DynamicProxyBasePathFilter(GatewayRequest gatwayRequest) {
		this.gatwayRequest = gatwayRequest;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String dynamicApplicationPath = getDynamicBasePath();
		if (dynamicApplicationPath != null && !dynamicApplicationPath.isEmpty()) {
			UriInfo uriInfo = requestContext.getUriInfo();
			URI baseUri = uriInfo.getBaseUriBuilder()
					.path(dynamicApplicationPath)
					.path("/") // baseUri must have a trailing slash
					.build();
			URI requestUri = uriInfo.getRequestUri();
			requestContext.setRequestUri(baseUri, requestUri);
		}
	}

	private String getDynamicBasePath() {
		String resource = gatwayRequest.getResource();
		if (resource == null) {
			return null;
		} else if (!resource.endsWith(PROXY_END)) {
			return "";
		}
		int proxyStart = resource.lastIndexOf(PROXY_START);
		if (proxyStart < 0) {
			return null;
		} else if (proxyStart == 0) {
			return "";
		} else {
			return resource.substring(0, proxyStart);
		}
	}
}
