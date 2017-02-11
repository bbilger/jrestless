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

import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_MAX_AGE;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static com.jrestless.core.filter.cors.CorsHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static com.jrestless.core.filter.cors.CorsHeaders.ORIGIN;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CORS filter that handles pre-flight and actual requests.
 * <p>
 * https://www.w3.org/TR/cors/
 * @author Bjoern Bilger
 *
 */
@PreMatching
public final class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOG = LoggerFactory.getLogger(CorsFilter.class);

	private static final String CORS_FAILURE_PROPERTY_NAME = "jrestless.cors.filter.failure";

	private static final String ANY_ORIGIN = "*";

	private final Set<String> allowedOrigins;
	private final Set<String> allowedMethods;
	private final Optional<String> exposedHeaders;
	private final Set<String> allowedHeaders;
	private final long maxAge;
	private final boolean allowCredentials;
	private final SameOriginPolicy sameOriginPolicy;

	private final boolean anyOriginAllowed;

	private CorsFilter(Set<String> allowedOrigins, Set<String> allowedMethods,
			boolean allowCredentials, Set<String> exposedHeaders, Set<String> allowedHeaders,
			long maxAge, SameOriginPolicy sameOriginPolicy) {
		requireNonEmpty(allowedOrigins, "allowedOrigins may not be empty");
		requireNonEmpty(allowedMethods, "allowedMethods may not be empty");
		requireNonNull(exposedHeaders, "exposedHeaders may not be null");
		requireNonNull(allowedHeaders, "allowedHeaders may not be null");

		this.allowedOrigins = allowedOrigins.stream().collect(Collectors.toSet());
		this.allowedMethods = allowedMethods.stream().collect(Collectors.toSet());
		if (exposedHeaders.isEmpty()) {
			this.exposedHeaders = Optional.empty();
		} else {
			this.exposedHeaders = Optional.of(String.join(",", exposedHeaders));
		}
		this.allowedHeaders = allowedHeaders.stream()
				.map(String::trim)
				.map(h -> h.toLowerCase(Locale.ENGLISH))
				.collect(Collectors.toSet());
		this.maxAge = maxAge;
		this.allowCredentials = allowCredentials;
		this.anyOriginAllowed = allowedOrigins.contains(ANY_ORIGIN);
		if (this.anyOriginAllowed && allowedOrigins.size() > 1) {
			throw new IllegalArgumentException("if any origin ('*') is allowed, no more origins may be defined");
		}
		this.sameOriginPolicy = requireNonNull(sameOriginPolicy);
	}

	private static <T> Set<T> requireNonEmpty(Set<T> set, String message) {
		if (set == null) {
			throw new NullPointerException(message);
		}
		if (set.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
		return set;
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String origin = requestContext.getHeaderString(ORIGIN);
		if (origin == null) {
			return; // not CORS
		}
		URI originUri = toUriSafe(origin);
		if (!isValidOrigin(originUri)) {
			throw prepareCorsFailureRequest(new ForbiddenException("invalid origin"), requestContext);
		}
		if (sameOriginPolicy.isSameOrigin(requestContext, origin)) {
			return; // same origin => nothing to do
		}

		String accessControlRequestMethod = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
		if (!isValidAccessControlRequestMethod(accessControlRequestMethod)) {
			throw prepareCorsFailureRequest(new ForbiddenException("accessControlRequestMethod may not be empty"),
					requestContext);
		}
		String requestMethod = requestContext.getMethod();

		if (isPreflightRequest(requestMethod, accessControlRequestMethod)) {
			String accessControlRequestHeaders = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
			WebApplicationException corsPreflightFailure = validateCorsPreflightRequest(origin,
					accessControlRequestMethod, accessControlRequestHeaders);
			if (corsPreflightFailure != null) {
				throw prepareCorsFailureRequest(corsPreflightFailure, requestContext);
			} else {
				requestContext.abortWith(
						buildPreflightResponse(origin, accessControlRequestMethod, accessControlRequestHeaders));
			}
		} else {
			WebApplicationException corsActualRequestFailure = validateCorsActualRequest(origin, requestMethod);
			if (corsActualRequestFailure != null) {
				throw prepareCorsFailureRequest(corsActualRequestFailure, requestContext);
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		String origin = requestContext.getHeaderString(ORIGIN);
		Object originFailureProperty = requestContext.getProperty(CORS_FAILURE_PROPERTY_NAME);
		String accessControlRequestMethod = requestContext.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
		String requestMethod = requestContext.getMethod();
		if (origin == null
				|| originFailureProperty != null
				|| isPreflightRequest(requestMethod, accessControlRequestMethod)
				|| sameOriginPolicy.isSameOrigin(requestContext, origin)) {
			return; // not CORS or a CORS failure => do not add any CORS headers
		}
		addCorsResponseHeaders(responseContext.getHeaders(), origin);
	}

	private WebApplicationException validateCorsPreflightRequest(String origin, String accessControlRequestMethod,
			String accessControlRequestHeaders) {
		if (!isOriginAllowed(origin)) {
			return new ForbiddenException("origin '" + origin + "' is not allowed");
		}
		if (!isMethodAllowed(accessControlRequestMethod)) {
			return new ForbiddenException("method '" + accessControlRequestMethod + "' is not allowed");
		}
		if (!isHeadersAllowed(accessControlRequestHeaders)) {
			return new ForbiddenException(
					"one or many header fields are not allowed '" + accessControlRequestHeaders + "'");
		}
		return null;
	}

	private WebApplicationException validateCorsActualRequest(String origin, String requestMethod) {
		if (!isOriginAllowed(origin)) {
			return new ForbiddenException("origin '" + origin + "' is not allowed");
		}
		if (!isMethodAllowed(requestMethod)) {
			return new ForbiddenException("method '" + requestMethod + "' is not allowed");
		}
		return null;
	}

	private <T extends Exception> T prepareCorsFailureRequest(T failure, ContainerRequestContext requestContext) {
		requestContext.setProperty(CORS_FAILURE_PROPERTY_NAME, true);
		LOG.debug("CORS request failed: {}", failure.getMessage());
		return failure;
	}

	private Response buildPreflightResponse(String origin, String accessControlRequestMethod,
			String accessControlRequestHeaders) {
		Response.ResponseBuilder builder = Response.ok();

		if (anyOriginAllowed && !allowCredentials) {
			builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, ANY_ORIGIN);
		} else {
			builder.header(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			builder.header(HttpHeaders.VARY, ORIGIN);
		}
		if (allowCredentials) {
			builder.header(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
		}
		if (maxAge > 0) {
			builder.header(ACCESS_CONTROL_MAX_AGE, maxAge);
		}
		builder.header(ACCESS_CONTROL_ALLOW_METHODS, accessControlRequestMethod);
		if (!isBlank(accessControlRequestHeaders)) {
			builder.header(ACCESS_CONTROL_ALLOW_HEADERS, accessControlRequestHeaders);
		}
		return builder.build();
	}

	private void addCorsResponseHeaders(MultivaluedMap<String, Object> responseHeaders, String origin) {
		if (anyOriginAllowed && !allowCredentials) {
			responseHeaders.putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, ANY_ORIGIN);
		} else {
			responseHeaders.putSingle(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
			responseHeaders.add(HttpHeaders.VARY, ORIGIN);
		}
		if (allowCredentials) {
			responseHeaders.putSingle(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
		}
		if (exposedHeaders.isPresent()) {
			responseHeaders.putSingle(ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders.get());
		}
	}

	private static boolean isValidOrigin(URI origin) {
		return origin != null
				&& origin.getScheme() != null
				&& origin.getHost() != null
				&& isBlank(origin.getRawPath())
				&& origin.getRawQuery() == null
				&& origin.getRawFragment() == null
				&& origin.getRawUserInfo() == null;
	}

	private static boolean isValidAccessControlRequestMethod(String accessControlRequestMethod) {
		return accessControlRequestMethod == null || !accessControlRequestMethod.trim().isEmpty();
	}

	private boolean isMethodAllowed(String method) {
		return !isBlank(method) && allowedMethods.contains(method);
	}

	private boolean isOriginAllowed(String origin) {
		return !isBlank(origin) && (anyOriginAllowed || allowedOrigins.contains(origin));
	}

	private boolean isHeadersAllowed(String accessControlRequestHeaders) {
		if (isBlank(accessControlRequestHeaders)) {
			return true;
		}
		for (String headerFieldName : accessControlRequestHeaders.split(",")) {
			headerFieldName = headerFieldName.trim().toLowerCase(Locale.ENGLISH);
			if (!allowedHeaders.contains(headerFieldName)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isPreflightRequest(String requestMethod, String accessControlRequestMethod) {
		return HttpMethod.OPTIONS.equals(requestMethod) && accessControlRequestMethod != null;
	}

	private static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	// returns null on failure
	private static URI toUriSafe(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * {@link CorsFilter} Builder.
	 * <p>
	 * By default max age is set to 3600, the methods GET, POST, PUT, DELETE,
	 * OPTIONS, HEAD are allowed, all headers will be allowed (since none are
	 * set), credentials are allowed, no headers are exposed and
	 * {@link DefaultSameOriginPolicy} is used.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	public static final class Builder {

		private static final long MAX_AGE_DEFAULT = 3600;

		private final Set<String> allowedMethods = new HashSet<>();
		private final Set<String> allowedOrigins = new HashSet<>();
		private final Set<String> exposedHeaders = new HashSet<>();
		private final Set<String> allowedHeaders = new HashSet<>();
		private long maxAge = MAX_AGE_DEFAULT;
		private boolean allowCredentials = true;
		private SameOriginPolicy sameOriginPolicy;

		public Builder sameOriginPolicy(SameOriginPolicy sameOriginPolicy) {
			this.sameOriginPolicy = requireNonNull(sameOriginPolicy);
			return this;
		}

		public Builder allowCredentials(boolean allowCredentials) {
			this.allowCredentials = allowCredentials;
			return this;
		}

		public Builder allowCredentials() {
			return allowCredentials(true);
		}

		public Builder disallowCredentials() {
			return allowCredentials(false);
		}

		public Builder maxAge(long maxAge) {
			this.maxAge = maxAge;
			return this;
		}

		public Builder allowHeaders(Set<String> headers) {
			this.allowedHeaders.addAll(headers);
			return this;
		}

		public Builder allowHeader(String header) {
			return allowHeaders(Collections.singleton(header));
		}

		public Builder exposeHeaders(Set<String> headers) {
			this.exposedHeaders.addAll(headers);
			return this;
		}

		public Builder exposeHeader(String header) {
			return exposeHeaders(Collections.singleton(header));
		}

		public Builder allowOrigins(Set<String> origins) {
			this.allowedOrigins.addAll(origins);
			return this;
		}

		public Builder allowOrigin(String origin) {
			return allowOrigins(Collections.singleton(origin));
		}

		public Builder allowAnyOrigin() {
			this.allowedOrigins.clear();
			return allowOrigin(ANY_ORIGIN);
		}

		public Builder allowMethods(Set<String> methods) {
			this.allowedMethods.addAll(methods);
			return this;
		}

		public Builder allowMethod(String method) {
			return allowMethods(Collections.singleton(method));
		}

		public Builder allowMethodGet() {
			return allowMethod(HttpMethod.GET);
		}

		public Builder allowMethodPost() {
			return allowMethod(HttpMethod.POST);
		}

		public Builder allowMethodPut() {
			return allowMethod(HttpMethod.PUT);
		}

		public Builder allowMethodDelete() {
			return allowMethod(HttpMethod.DELETE);
		}

		public Builder allowMethodOptions() {
			return allowMethod(HttpMethod.OPTIONS);
		}

		public Builder allowMethodHead() {
			return allowMethod(HttpMethod.HEAD);
		}

		public Builder allowMethodDefaults() {
			allowMethodGet();
			allowMethodPost();
			allowMethodPut();
			allowMethodDelete();
			allowMethodOptions();
			allowMethodHead();
			return this;
		}

		public CorsFilter build() {
			if (allowedMethods.isEmpty()) {
				allowMethodDefaults();
			}
			if (allowedOrigins.isEmpty()) {
				allowAnyOrigin();
			}
			if (sameOriginPolicy == null) {
				sameOriginPolicy(new DefaultSameOriginPolicy());
			}
			return new CorsFilter(allowedOrigins, allowedMethods, allowCredentials, exposedHeaders, allowedHeaders,
					maxAge, sameOriginPolicy);
		}
	}
}
