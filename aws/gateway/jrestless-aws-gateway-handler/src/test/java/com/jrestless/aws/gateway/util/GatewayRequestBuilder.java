package com.jrestless.aws.gateway.util;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.gateway.handler.GatewayRequestAndLambdaContext;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;

/**
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayRequestBuilder extends AbstractGatewayRequestBuilder<GatewayRequest> {

	public GatewayRequestBuilder domain(String domain) {
		setDomain(domain);
		return this;
	}

	public GatewayRequestBuilder basePath(String basePath) {
		setBasePath(basePath);
		return this;
	}

	public GatewayRequestBuilder resource(String resource) {
		setResource(resource);
		return this;
	}
	/**
	 * If not set, then it'll be derived from the resource.
	 */
	public GatewayRequestBuilder path(String path) {
		setPath(path);
		return this;
	}

	public GatewayRequestBuilder pathParams(Map<String, String> pathParams) {
		setPathParams(pathParams);
		return this;
	}

	public GatewayRequestBuilder queryParams(Map<String, String> queryParams) {
		setQueryParams(queryParams);
		return this;
	}

	public GatewayRequestBuilder headers(Map<String, String> headers) {
		setHeaders(headers);
		return this;
	}

	public GatewayRequestBuilder requestContextStage(String requestContextStage) {
		setRequestContextStage(requestContextStage);
		return this;
	}

	public GatewayRequestBuilder httpMethod(String httpMethod) {
		setHttpMethod(httpMethod);
		return this;
	}

	public GatewayRequestBuilder body(String body) {
		setBody(body);
		return this;
	}

	public GatewayRequestBuilder base64Encoded(boolean isBase64Encoded) {
		setIsBase64Encoded(isBase64Encoded);
		return this;
	}

	public GatewayRequestBuilder authorizerDate(Map<String, Object> authorizerData) {
		setAuthorizerData(authorizerData);
		return this;
	}

	public GatewayRequestAndLambdaContext buildWrapped(Context lambdaContext) {
		GatewayRequest req = build();
		GatewayRequestAndLambdaContext reqAndContext = new GatewayRequestAndLambdaContext(req, lambdaContext);
		return reqAndContext;
	}

	public GatewayRequestAndLambdaContext buildWrapped() {
		return buildWrapped(null);
	}

	@Override
	GatewayRequest createGatewayRequest(String body, Map<String, String> headers, Map<String, String> pathParams,
			Map<String, String> queryParams, String httpMethod, String resource, String path,
			String requestContextStage, boolean isBase64Encoded, Map<String, Object> authorizerData) {
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getBody()).thenReturn(body);
		when(request.getHeaders()).thenReturn(headers);
		when(request.getPathParameters()).thenReturn(pathParams);
		when(request.getQueryStringParameters()).thenReturn(queryParams);
		when(request.getHttpMethod()).thenReturn(httpMethod);
		when(request.getResource()).thenReturn(resource);
		when(request.isBase64Encoded()).thenReturn(isBase64Encoded);

		when(request.getPath()).thenReturn(path);

		GatewayRequestContext gatewayRequestContext = mock(GatewayRequestContext.class);
		when(gatewayRequestContext.getStage()).thenReturn(requestContextStage);
		when(gatewayRequestContext.getAuthorizer()).thenReturn(authorizerData);

		when(request.getRequestContext()).thenReturn(gatewayRequestContext);
		return request;
	}

}
