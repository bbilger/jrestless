package com.jrestless.aws.gateway.util;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.gateway.handler.GatewayRequestAndLambdaContext;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;

/**
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayRequestBuilder {

	private String domain;
	private String basePath;
	private String resource;
	private Map<String, String> pathParams;
	private Map<String, String> queryParams;
	private Map<String, String> headers;
	private String requestContextStage;
	private String httpMethod;

	public GatewayRequestBuilder domain(String domain) {
		this.domain = domain;
		return this;
	}

	public GatewayRequestBuilder basePath(String basePath) {
		this.basePath = basePath;
		return this;
	}

	public GatewayRequestBuilder resource(String resource) {
		this.resource = resource;
		return this;
	}

	public GatewayRequestBuilder pathParams(Map<String, String> pathParams) {
		if (pathParams == null) {
			this.pathParams = null;
		} else {
			this.pathParams = new HashMap<>(pathParams);
		}
		return this;
	}

	public GatewayRequestBuilder queryParams(Map<String, String> queryParams) {
		if (queryParams == null) {
			this.queryParams = null;
		} else {
			this.queryParams = new HashMap<>(queryParams);
		}
		return this;
	}

	public GatewayRequestBuilder headers(Map<String, String> headers) {
		if (headers == null) {
			this.headers = null;
		} else {
			this.headers = new HashMap<>(headers);
		}
		return this;
	}

	public GatewayRequestBuilder requestContextStage(String requestContextStage) {
		this.requestContextStage = requestContextStage;
		return this;
	}

	public GatewayRequestBuilder httpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
		return this;
	}

	public GatewayRequest build() {
		GatewayRequest request = mock(GatewayRequest.class);
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
		when(request.getHeaders()).thenReturn(actualHeaders);

		when(request.getPathParameters()).thenReturn(pathParams);

		when(request.getQueryStringParameters()).thenReturn(queryParams);

		when(request.getHttpMethod()).thenReturn(httpMethod);

		if (resource == null) {
			throw new IllegalStateException("resource must be set");
		}
		when(request.getResource()).thenReturn(resource);

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
		String path;
		if (pathParams != null) {
			path = pathUriBuilder.buildFromEncodedMap(pathParams).toString();
		} else {
			path = pathUriBuilder.build().toString();
		}
		when(request.getPath()).thenReturn(path);

		GatewayRequestContext gatewayRequestContext = mock(GatewayRequestContext.class);
		when(gatewayRequestContext.getStage()).thenReturn(requestContextStage);

		when(request.getRequestContext()).thenReturn(gatewayRequestContext);

		return request;
	}

	public GatewayRequestAndLambdaContext buildWrapped(Context lambdaContext) {
		GatewayRequest req = build();
		GatewayRequestAndLambdaContext reqAndContext = new GatewayRequestAndLambdaContext(req, lambdaContext);
		return reqAndContext;
	}

	public GatewayRequestAndLambdaContext buildWrapped() {
		return buildWrapped(null);
	}

}
