package com.jrestless.aws.gateway.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import com.jrestless.aws.gateway.io.GatewayRequest;

abstract class AbstractGatewayRequestBuilder<T extends GatewayRequest> {
	private String domain;
	private String basePath;
	private String resource;
	private Map<String, String> pathParams;
	private Map<String, String> queryParams;
	private Map<String, String> headers;
	private String requestContextStage;
	private String httpMethod;
	private String body;
	private boolean isBase64Encoded;
	private Map<String, Object> authorizerData;
	private String path;

	void setDomain(String domain) {
		this.domain = domain;
	}

	void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * If not set, then it'll be derived from the resource.
	 */
	void setPath(String path) {
		this.path = path;
	}

	void setPathParams(Map<String, String> pathParams) {
		if (pathParams == null) {
			this.pathParams = null;
		} else {
			this.pathParams = new HashMap<>(pathParams);
		}
	}

	void setQueryParams(Map<String, String> queryParams) {
		if (queryParams == null) {
			this.queryParams = null;
		} else {
			this.queryParams = new HashMap<>(queryParams);
		}
	}

	void setHeaders(Map<String, String> headers) {
		if (headers == null) {
			this.headers = null;
		} else {
			this.headers = new HashMap<>(headers);
		}
	}

	void setRequestContextStage(String requestContextStage) {
		this.requestContextStage = requestContextStage;
	}

	void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	void setBody(String body) {
		this.body = body;
	}

	void setIsBase64Encoded(boolean isBase64Encoded) {
		this.isBase64Encoded = isBase64Encoded;
	}

	void setAuthorizerData(Map<String, Object> authorizerData) {
		this.authorizerData = authorizerData;
	}

	abstract T createGatewayRequest(String body, Map<String, String> headers, Map<String, String> pathParams,
			Map<String, String> queryParams, String httpMethod, String resource, String path,
			String requestContextStage, boolean isBase64Encoded, Map<String, Object> authorizerData);

	public final T build() {

		Map<String, String> actualHeaders = null;
		if (headers != null || domain != null) {
			actualHeaders = new HashMap<>();
			if (domain != null) {
				actualHeaders.put(HttpHeaders.HOST, domain);
			}
			if (headers != null) {
				actualHeaders.putAll(headers);
			}
		}

		if (resource == null) {
			throw new IllegalStateException("resource must be set");
		}

		// proxy param to ordinary param
		String resourceTemplate = resource.replaceAll("\\+\\}", "}");

		UriBuilder pathUriBuilder = UriBuilder.fromUri(URI.create("/"));
		if (basePath != null) {
			pathUriBuilder.path(basePath);
		}
		// avoid single trailing slash
		if (!"/".equals(resourceTemplate)) {
			pathUriBuilder.path(resourceTemplate);
		}
		String actualPath;
		if (path != null) {
			actualPath = path;
		} else if (pathParams != null) {
			actualPath = pathUriBuilder.buildFromEncodedMap(pathParams).toString();
		} else {
			actualPath = pathUriBuilder.build().toString();
		}

		return createGatewayRequest(body, actualHeaders, pathParams, queryParams, httpMethod, resource, actualPath,
				requestContextStage, isBase64Encoded, authorizerData);
	}
}
