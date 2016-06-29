/*
 * Copyright 2016 Bjoern Bilger
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
package com.jrestless.aws.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;

import com.jrestless.aws.GatewayRequestContext;
import com.jrestless.core.container.io.JRestlessContainerRequest;

/**
 * AWS API Gateway request object.
 * <p>
 * The implementation highly depends on the AWS API Gateway request template and
 * is designed to get de-serialized from it.
 * <p>
 * It also acts as a glue to the container by implementing
 * {@link JRestlessContainerRequest}. Those values are generated upon request
 * since we want to avoid to fail during de-serialization-phase.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayRequest implements JRestlessContainerRequest {

	private static final URI ROOT_URI = URI.create("/");

	private String body;
	private GatewayRequestContextImpl context;
	private Map<String, String> headerParams = new HashMap<>();
	private Map<String, String> queryParams = new HashMap<>();
	private Map<String, String> pathParams = new HashMap<>();

	/**
	 * Body escaped as JSON string.
	 *
	 * @param body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	public void setHeaderParams(Map<String, String> headerParams) {
		this.headerParams.clear();
		if (headerParams != null) {
			this.headerParams.putAll(headerParams);
		}
	}

	public void setQueryParams(Map<String, String> queryParams) {
		this.queryParams.clear();
		if (queryParams != null) {
			this.queryParams.putAll(queryParams);
		}
	}

	public void setPathParams(Map<String, String> pathParams) {
		this.pathParams.clear();
		if (pathParams != null) {
			this.pathParams.putAll(pathParams);
		}
	}

	public GatewayRequestContext getContext() {
		return context;
	}

	public void setContext(GatewayRequestContextImpl context) {
		this.context = context;
	}

	@Override
	public URI getBaseUri() {
		return ROOT_URI;
	}

	private String replacePathParams(String templateRequestUri, Map<String, String> pathParameters) {
		String newRequesUri = templateRequestUri;
		for (Map.Entry<String, String> pathParam : pathParameters.entrySet()) {
			String pathParamKey = pathParam.getKey();
			String pathParamValue = pathParam.getValue();
			String updatedRequestUri = newRequesUri.replace("{" + pathParamKey + "}", pathParamValue);
			if (updatedRequestUri.equals(newRequesUri)) {
				throw new UnmatchedPathParamException(templateRequestUri, pathParamKey);
			}
			newRequesUri = updatedRequestUri;
		}
		if (newRequesUri.contains("{")) {
			int bracketOpen = newRequesUri.indexOf('{') + 1;
			int bracketClose = newRequesUri.indexOf('}');
			if (bracketClose > bracketOpen && bracketClose > -1) {
				throw new MissingPathParamException(templateRequestUri,
						newRequesUri.substring(bracketOpen, bracketClose));
			} else {
				throw new IllegalArgumentException("invalid requestUri: '" + templateRequestUri + "'");
			}
		} else if (newRequesUri.contains("}")) {
			throw new IllegalArgumentException("invalid requestUri: '" + templateRequestUri + "'");
		}
		return newRequesUri;
	}

	private String appendQueryParams(String requestUri, Map<String, String> queryParameters) {
		if (queryParameters.size() > 0) {
			StringBuilder requestUriBuilder = new StringBuilder(requestUri);
			requestUriBuilder.append("?");
			boolean first = true;
			for (Map.Entry<String, String> queryParam : queryParameters.entrySet()) {
				if (!first) {
					requestUriBuilder.append("&");
				}
				requestUriBuilder.append(encodeQueryParam(queryParam.getKey()));
				requestUriBuilder.append("=");
				requestUriBuilder.append(encodeQueryParam(queryParam.getValue()));
				first = false;
			}
			return requestUriBuilder.toString();
		} else {
			return requestUri;
		}
	}

	private String encodeQueryParam(String param) {
		try {
			return URLEncoder.encode(param, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the request {@link URI} from the given
	 * {@link GatewayRequestContext#getResourcePath() resource path}, the
	 * {@link #setQueryParams(Map) query parameters} and the
	 * {@link #setPathParams(Map)}.
	 *
	 * @return generates <b>new</b> request {@link URI}
	 */
	@Override
	public URI getRequestUri() {
		if (context == null) {
			throw new IllegalStateException("context has not been set");
		}
		String resourcePath = context.getResourcePath();
		if (resourcePath == null) {
			throw new IllegalStateException("context.resourcePath has not been set");
		}
		String requestUri = replacePathParams(resourcePath, pathParams);
		requestUri = appendQueryParams(requestUri, queryParams);
		return URI.create(requestUri);
	}

	/**
	 * Generates a <b>new</b> entity stream from the given
	 * {@link #setBody(String) body}. If no body is set, then an empty stream is
	 * returned. If the body is set, then the JSON body gets unescaped, first.
	 *
	 * @return generates a new entity stream
	 */
	@Override
	public InputStream getEntityStream() {
		if (body != null) {
			String unescapedBody = StringEscapeUtils.unescapeJson(body);
			return new ByteArrayInputStream(unescapedBody.getBytes(StandardCharsets.UTF_8));
		} else {
			return new ByteArrayInputStream(new byte[0]);
		}
	}

	/**
	 * Generates a new map of HTTP headers from the given
	 * {@link #setHeaderParams(Map) headers}. Null values will be filtered out
	 * and each value is mapped into an immutable list.
	 *
	 * @return generates the HTTP headers
	 */
	@Override
	public Map<String, List<String>> getHeaders() {
		Function<Map.Entry<String, String>, List<String>> toList = h -> Collections.singletonList(h.getValue());
		return headerParams.entrySet().stream()
				.filter(h -> h.getValue() != null)
				.collect(Collectors.toMap(h -> h.getKey(), toList));
	}

	@Override
	public String getHttpMethod() {
		if (context == null) {
			throw new IllegalStateException("context has not been set");
		}
		String httpMethod = context.getHttpMethod();
		if (httpMethod == null) {
			throw new IllegalStateException("context.httpMethod has not been set");
		}
		return httpMethod;
	}

	@Override
	public String toString() {
		return "AwsRequest [body=" + body + ", context=" + context + ", headerParams=" + headerParams + ", queryParams="
				+ queryParams + ", pathParams=" + pathParams + "]";
	}

	public static class PathParamException extends RuntimeException {

		private static final long serialVersionUID = -3683144705299818242L;

		private final String templateRequestUri;
		private final String param;

		public PathParamException(String errorMessage, String templateRequestUri, String pathParam) {
			super(errorMessage);
			this.templateRequestUri = templateRequestUri;
			this.param = pathParam;
		}

		public String getTemplateRequestUri() {
			return templateRequestUri;
		}

		public String getParam() {
			return param;
		}
	}

	public static class UnmatchedPathParamException extends PathParamException {

		private static final long serialVersionUID = -4060989195540687509L;

		public UnmatchedPathParamException(String templateRequestUri, String pathParam) {
			super("couldn't find path param '" + pathParam + "' in requestUri: " + templateRequestUri,
					templateRequestUri, pathParam);
		}
	}

	public static class MissingPathParamException extends PathParamException {

		private static final long serialVersionUID = 3356882087632432730L;

		public MissingPathParamException(String templateRequestUri, String pathParam) {
			super("path parameter '" + pathParam + "' couldn't be replace in requestUri: " + templateRequestUri,
					templateRequestUri, pathParam);
		}
	}

}
