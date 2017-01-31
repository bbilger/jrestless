package com.jrestless.aws.gateway.util;

import java.util.Map;

import com.jrestless.aws.gateway.io.DefaultGatewayRequest;
import com.jrestless.aws.gateway.io.DefaultGatewayRequestContext;

public class DefaultGatewayRequestBuilder extends AbstractGatewayRequestBuilder<DefaultGatewayRequest> {

	public DefaultGatewayRequestBuilder domain(String domain) {
		setDomain(domain);
		return this;
	}

	public DefaultGatewayRequestBuilder basePath(String basePath) {
		setBasePath(basePath);
		return this;
	}

	public DefaultGatewayRequestBuilder resource(String resource) {
		setResource(resource);
		return this;
	}
	/**
	 * If not set, then it'll be derived from the resource.
	 */
	public DefaultGatewayRequestBuilder path(String path) {
		setPath(path);
		return this;
	}

	public DefaultGatewayRequestBuilder pathParams(Map<String, String> pathParams) {
		setPathParams(pathParams);
		return this;
	}

	public DefaultGatewayRequestBuilder queryParams(Map<String, String> queryParams) {
		setQueryParams(queryParams);
		return this;
	}

	public DefaultGatewayRequestBuilder headers(Map<String, String> headers) {
		setHeaders(headers);
		return this;
	}

	public DefaultGatewayRequestBuilder requestContextStage(String requestContextStage) {
		setRequestContextStage(requestContextStage);
		return this;
	}

	public DefaultGatewayRequestBuilder httpMethod(String httpMethod) {
		setHttpMethod(httpMethod);
		return this;
	}

	public DefaultGatewayRequestBuilder body(String body) {
		setBody(body);
		return this;
	}

	public DefaultGatewayRequestBuilder base64Encoded(boolean isBase64Encoded) {
		setIsBase64Encoded(isBase64Encoded);
		return this;
	}

	public DefaultGatewayRequestBuilder authorizerData(Map<String, Object> authorizerData) {
		setAuthorizerData(authorizerData);
		return this;
	}

	@Override
	DefaultGatewayRequest createGatewayRequest(String body, Map<String, String> headers, Map<String, String> pathParams,
			Map<String, String> queryParams, String httpMethod, String resource, String path,
			String requestContextStage, boolean isBase64Encoded, Map<String, Object> authorizerData) {
		DefaultGatewayRequestContext context = new DefaultGatewayRequestContext();
		context.setStage(requestContextStage);
		context.setAuthorizer(authorizerData);

		DefaultGatewayRequest request = new DefaultGatewayRequest();
		request.setBody(body);
		request.setHeaders(headers);
		request.setPathParameters(pathParams);
		request.setQueryStringParameters(queryParams);
		request.setHttpMethod(httpMethod);
		request.setResource(resource);
		request.setPath(path);
		request.setIsBase64Encoded(isBase64Encoded);
		request.setRequestContext(context);

		return request;
	}

}
