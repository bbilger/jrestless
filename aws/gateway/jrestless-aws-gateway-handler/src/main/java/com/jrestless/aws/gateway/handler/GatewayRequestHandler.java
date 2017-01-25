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
package com.jrestless.aws.gateway.handler;

import static com.jrestless.aws.gateway.io.GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.AwsFeature;
import com.jrestless.aws.gateway.GatewayFeature;
import com.jrestless.aws.gateway.io.GatewayBinaryReadInterceptor;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.gateway.io.GatewayResponse;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.DefaultJRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.RequestAndBaseUri;
import com.jrestless.core.util.HeaderUtils;

/**
 * Base AWS API Gateway request handler.
 * <p>
 * Note: we don't implement
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler RequestHandler}
 * in case we need
 * {@link com.amazonaws.services.lambda.runtime.RequestStreamHandler
 * RequestStreamHandler} at some point.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class GatewayRequestHandler
		extends SimpleRequestHandler<GatewayRequestAndLambdaContext, GatewayResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(GatewayRequestHandler.class);
	private static final String AWS_DOMAIN = "amazonaws.com";

	protected GatewayRequestHandler() {
	}

	@Override
	protected JRestlessContainerRequest createContainerRequest(GatewayRequestAndLambdaContext requestAndLambdaContext) {
		requireNonNull(requestAndLambdaContext);
		GatewayRequest request = requestAndLambdaContext.getGatewayRequest();
		requireNonNull(request);
		requireNonNull(request.getPath());
		String body = request.getBody();
		InputStream entityStream;
		if (body != null) {
			entityStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		} else {
			entityStream = new ByteArrayInputStream(new byte[0]);
		}
		RequestAndBaseUri requestAndBaseUri = getRequestAndBaseUri(requestAndLambdaContext);
		return new DefaultJRestlessContainerRequest(requestAndBaseUri, request.getHttpMethod(), entityStream,
				HeaderUtils.expandHeaders(request.getHeaders()));
	}

	@Override
	protected void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, GatewayRequestAndLambdaContext requestAndLambdaContext) {
		GatewayRequest request = requestAndLambdaContext.getGatewayRequest();
		Context lambdaContext = requestAndLambdaContext.getLambdaContext();
		actualContainerRequest.setRequestScopedInitializer(locator -> {
			Ref<GatewayRequest> gatewayRequestRef = locator
					.<Ref<GatewayRequest>>getService(GatewayFeature.GATEWAY_REQUEST_TYPE);
			if (gatewayRequestRef != null) {
				gatewayRequestRef.set(request);
			} else {
				LOG.error("GatewayFeature has not been registered. GatewayRequest injection won't work.");
			}
			Ref<Context> contextRef = locator.<Ref<Context>>getService(AwsFeature.CONTEXT_TYPE);
			if (contextRef != null) {
				contextRef.set(lambdaContext);
			} else {
				LOG.error("AwsFeature has not been registered. Context injection won't work.");
			}
		});
		actualContainerRequest.setProperty(GatewayBinaryReadInterceptor.PROPERTY_BASE_64_ENCODED_REQUEST,
				request.isBase64Encoded());
	}

	@Override
	protected SimpleResponseWriter<GatewayResponse> createResponseWriter(
			GatewayRequestAndLambdaContext requestAndContext) {
		return new ResponseWriter();
	}

	@Override
	protected GatewayResponse onRequestFailure(Exception e, GatewayRequestAndLambdaContext request,
			JRestlessContainerRequest containerRequest) {
		LOG.error("request failed", e);
		return new GatewayResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR, false);
	}

	/**
	 * Returns the base and request URI for this request.
	 * <p>
	 * The baseUri is constructed as follows:
	 * <ol>
	 * <li>If the "Host" header is available, then the domain will be set to its
	 * value, the scheme will be set to {@code https}. If no "Host" header is
	 * available, then those values will be missing.
	 * <li>If the domain ("Host" header) indicates that the lambda function
	 * wasn't invoked from a custom domain
	 * ({@code !domain.endsWith("amazonaws.com")}), then the stage will be
	 * appended as first path to the baseUri.
	 * <li>The basePath (see below) is appended to the baseUri.
	 * </ol>
	 * The requestUri is constructed as follows:
	 * <ol>
	 * <li>baseUri without basePath
	 * <li>{@link GatewayRequest#getPath()} (already includes the basePath)
	 * <li>query parameters are taken from
	 * {@link GatewayRequest#getQueryStringParameters()} (the parameters are
	 * encoded again).
	 * </ol>
	 * Please note that whenever we run into an unknown situation, we fallback
	 * to baseUri=/ and requestUri={@link GatewayRequest#getPath()}+query
	 * parameters.
	 *
	 * <p>
	 * The previously mentioned basePath is defined as
	 * {@link GatewayRequest#getPath() path} -
	 * {@link GatewayRequest#getResource() resource}. For non-custom domain
	 * invocations there will never be any basePath. For custom domain
	 * invocations, however, there will be a basePath. Let's assume we have
	 * "/resource/{resourceid}/{proxy+}" registered as endpoint in APIGW and set
	 * up a custom domain "api.example.com". The custom domain can be configured
	 * to use your API in AGPIGW with the parameters "domain's base path" and
	 * "stage".
	 *
	 * <table summary="basePath calculation for custom domains" border="1">
	 *
	 * <tr align="left">
	 * <th>domain's base path
	 * <th>stage
	 * <th>invocation url
	 * <th>
	 * <th>path
	 * <th>request
	 * <th>
	 * <th>baseUri's base path
	 * <th>baseUri
	 * <th>requestUri
	 * </tr>
	 *
	 * <tr align="left">
	 * <td>-
	 * <td>-
	 * <td>https://api.example.com/dev/resource/1/subresource/2
	 * <td>
	 * <td>/dev/resource/1/subresource/2
	 * <td>/resource/{resourceid}/{proxy+}
	 * <td>
	 * <td>/dev
	 * <td>https://api.example.com/dev
	 * <td>https://api.example.com/dev/resource/1/subresource/2
	 * </tr>
	 *
	 * <tr align="left">
	 * <td>-
	 * <td>dev
	 * <td>https://api.example.com/resource/1/subresource/2
	 * <td>
	 * <td>/resource/1/subresource/2
	 * <td>/resource/{resourceid}/{proxy+}
	 * <td>
	 * <td>-
	 * <td>https://api.example.com
	 * <td>https://api.example.com/resource/1/subresource/2
	 * </tr>
	 *
	 * <tr align="left">
	 * <td>domainbasepath
	 * <td>-
	 * <td>https://api.example.com/domainbasepath/dev/resource/1/subresource/2
	 * <td>
	 * <td>/domainbasepath/dev/resource/1/subresource/2
	 * <td>/resource/{resourceid}/{proxy+}
	 * <td>
	 * <td>/domainbasepath/dev
	 * <td>https://api.example.com/domainbasepath/dev
	 * <td>https://api.example.com/domainbasepath/dev/resource/1/subresource/2
	 * </tr>
	 *
	 * <tr align="left">
	 * <td>domainbasepath
	 * <td>dev
	 * <td>https://api.example.com/domainbasepath/resource/1/subresource/2
	 * <td>
	 * <td>/domainbasepath/resource/1/subresource/2
	 * <td>/resource/{resourceid}/{proxy+}
	 * <td>
	 * <td>/domainbasepath
	 * <td>https://api.example.com/domainbasepath
	 * <td>https://api.example.com/domainbasepath/resource/1/subresource/2
	 * </tr>
	 *
	 * </table>
	 *
	 *
	 * @param requestAndLambdaContext
	 * @return the base and request URI for this request
	 */
	protected RequestAndBaseUri getRequestAndBaseUri(@Nonnull GatewayRequestAndLambdaContext requestAndLambdaContext) {

		GatewayRequest request = requestAndLambdaContext.getGatewayRequest();

		URI baseUri;
		URI baseUriWithoutBasePath;
		try {
			UriBuilder baseUriBuilder;
			String host = getHost(request);
			String basePath = getBasePathUri(request);
			boolean hostPresent = !isBlank(host);
			boolean validBasePath = basePath != null; // blank is ok

			if (!hostPresent) {
				LOG.warn("no host header available; using baseUri=/ as fallback");
				baseUriBuilder = UriBuilder.fromUri("/");
			} else if (!validBasePath) {
				LOG.warn("couldn't determine basePath; using baseUri=/ as fallback");
				baseUriBuilder = UriBuilder.fromUri("/");
			} else {
				/*
				 *  APIGW should not support anything but scheme=https and port=443
				 *  we could, however, fetch those values from the headers:
				 *  "X-Forwarded-Port" and "X-Forwarded-Proto"
				 */
				baseUriBuilder = UriBuilder.fromUri("https://" + host);
				if (isDirectApiGatewayInvocation(host)) {
					addStagePathIfAvailable(baseUriBuilder, request);
				}
			}

			baseUriWithoutBasePath = baseUriBuilder.build();
			if (hostPresent && validBasePath) {
				baseUriBuilder.path(basePath);
			}
			baseUri = baseUriBuilder.build();
		} catch (RuntimeException e) {
			LOG.error("baseUriCreationFailure; using baseUri=/ as fallback", e);
			baseUri = URI.create("/");
			baseUriWithoutBasePath = baseUri;
		}

		UriBuilder requestUriBuilder = UriBuilder.fromUri(baseUriWithoutBasePath).path(request.getPath());
		addQueryParametersIfAvailable(requestUriBuilder, request);

		return new RequestAndBaseUri(baseUri, requestUriBuilder.build());
	}

	private static String getHost(GatewayRequest request) {
		Map<String, String> headers = request.getHeaders();
		if (headers == null) {
			return null;
		}
		return headers.get(HttpHeaders.HOST);
	}

	private static void addQueryParametersIfAvailable(UriBuilder uriBuilder, GatewayRequest request) {
		Map<String, String> queryStrings = request.getQueryStringParameters();
		if (queryStrings != null) {
			for (Map.Entry<String, String> queryStringEntry : queryStrings.entrySet()) {
				// Note: APIGW decodes them and we encode them, again which should be fine.
				uriBuilder.queryParam(queryStringEntry.getKey(), queryStringEntry.getValue());
			}
		}
	}

	private static boolean isDirectApiGatewayInvocation(String host) {
		return host != null && host.endsWith(AWS_DOMAIN);
	}

	private static void addStagePathIfAvailable(UriBuilder uriBuilder, GatewayRequest request) {
		GatewayRequestContext requestContext = request.getRequestContext();
		if (requestContext != null) {
			String stage = requestContext.getStage();
			if (!isBlank(stage)) {
				uriBuilder.path(stage);
			}
		}
	}

	/**
	 * Resolves path parameters in the resource against the actual ones.
	 * <p>
	 * <ul>
	 * <li>/users/{uid} + {"uid": "123"} => /users/123
	 * <li>/users/{proxy+} + {"proxy": "1/contacts"} => /users/1/contacts
	 * </ul>
	 * Please note APIGW uses the parameter name "proxy" for the path parameter
	 * "{proxy+}".
	 *
	 * @param resource
	 *            {@link GatewayRequest#getResource()}
	 * @param pathParams
	 *            {@link GatewayRequest#getPathParameters()}
	 * @return the resolved resource or null in case an error occurred
	 */
	private static String resolveResource(String resource, Map<String, String> pathParams) {
		String[] resourceSplits = resource.split("/");
		for (int i = 0; i < resourceSplits.length; i++) {
			String resourceSplit = resourceSplits[i];
			if (resourceSplit.startsWith("{")) {
				String pathParamName = null;
				if (resourceSplit.endsWith("+}")) {
					pathParamName = resourceSplit.substring(1, resourceSplit.length() - 2);
				} else if (resourceSplit.endsWith("}")) {
					pathParamName = resourceSplit.substring(1, resourceSplit.length() - 1);
				} else {
					LOG.warn("resource '{}': invalid resource", resource);
					return null;
				}
				if (pathParams == null) {
					LOG.warn("resource '{}': pathParams are null but expected '{}'", resource, pathParamName);
					return null;
				}
				String pathParamValue = pathParams.get(pathParamName);
				if (pathParamValue == null) {
					LOG.warn("resource '{}': couldn't find value for path parameter '{}'", resource,
							pathParamName);
				}
				resourceSplits[i] = pathParamValue;
			}
		}
		return String.join("/", resourceSplits);
	}

	/**
	 * Returns the basePath which is basically {@link GatewayRequest#getPath()}
	 * - {@link GatewayRequest#getResource()}.
	 * <p>
	 * <table border="1" summary="base path mapping">
	 * <tr>
	 * <th>path
	 * <th>resource
	 * <th>base path
	 * </tr>
	 *
	 * <tr>
	 * <td>/users/1
	 * <td>/users/{uid}
	 * <td>&lt;blank&gt; (no base path)
	 * </tr>
	 *
	 * <tr>
	 * <td>/base/users/1
	 * <td>/users{uid}
	 * <td>/base
	 * </tr>
	 *
	 * <tr>
	 * <td>/users/1/friends
	 * <td>/users/{uid}/friends
	 * <td>&lt;blank&gt; (no base path)
	 * </tr>
	 *
	 * <tr>
	 * <td>/base/users/1/friends
	 * <td>/users/{uid}/{proxy+}
	 * <td>/base
	 * </tr>
	 *
	 * <tr>
	 * <td>/users/1/friends/////
	 * <td>/users/{uid}/friends
	 * <td>&lt;blank&gt; (no base path)
	 * </tr>
	 * </table>
	 * In case we fail to determine the base path {@code null} will be returned
	 *
	 * @param request
	 * @return the base path, "" if there's no base path or {@code null} if we
	 *         cannot determine the base path
	 */
	private static String getBasePathUri(GatewayRequest request) {
		String resource = request.getResource();
		// APIGW allows trailing slashes => strip them off for basePath detection
		String normalizedPath = removeTrailingSlashes(request.getPath());
		String basePath = null;
		if (isBlank(resource) || isBlank(normalizedPath)) {
			LOG.warn("unexpected configuration => cannot determine base path; resource='{}', path='{}'", resource,
					normalizedPath);
		} else if ("/".equals(resource) && "/".equals(normalizedPath)) {
			basePath = ""; // no basePath but OK!
		} else if ("/".equals(resource)) {
			basePath = normalizedPath;
		} else {
			/*
			 *  please note path params won't contain trailing slashes
			 *  given path=/log/123/// and resource=/log/{proxy+} => pathParameters={"proxy": "123"}
			 */
			String resolvedResource = resolveResource(resource, request.getPathParameters());
			if (resolvedResource != null) {
				int pathLength = normalizedPath.length();
				int resolvedResourceLength = resolvedResource.length();
				int lastIndex = normalizedPath.lastIndexOf(resolvedResource);
				boolean pathContainsResource = lastIndex >= 0;
				boolean pathEndsWithResource = lastIndex + resolvedResourceLength == pathLength;
				if (pathContainsResource && pathEndsWithResource) {
					basePath = normalizedPath.substring(0, lastIndex);
				} else {
					LOG.warn("resource '{}': unable to map resolved resource '{}' against path '{}'", resource,
							resolvedResource, normalizedPath);
				}
			} else {
				LOG.warn("resource '{}': couldn't resolve resource", resource);
			}
		}
		return basePath;
	}

	private static String removeTrailingSlashes(String path) {
		if (path == null || path.length() <= 1) {
			return path;
		}
		// remove trailing slashes but allow a leading one
		int lastValidIndex = path.length();
		for (int i = path.length() - 1; i > 0; i--) {
			if (path.charAt(i) == '/') {
				lastValidIndex = i;
			} else {
				break;
			}
		}
		return path.substring(0, lastValidIndex);
	}

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static class QueryParameterEncodingException extends RuntimeException {

		private static final long serialVersionUID = -7545175514996382745L;

		QueryParameterEncodingException(Exception cause) {
			super(cause);
		}
	}

	protected static class ResponseWriter implements SimpleResponseWriter<GatewayResponse> {
		private GatewayResponse response;

		public ResponseWriter() {
			// allow usage by GatewayRequestHandler subclasses
		}

		@Override
		public OutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			List<String> binaryResponseHeader = headers.get(HEADER_BINARY_RESPONSE);
			boolean binaryResponse = binaryResponseHeader != null
					&& binaryResponseHeader.size() == 1
					&& "true".equals(binaryResponseHeader.get(0));
			Map<String, String> flattenedHeaders = HeaderUtils.flattenHeaders(headers,
					headerName -> !HEADER_BINARY_RESPONSE.equals(headerName));
			String body = ((ByteArrayOutputStream) entityOutputStream).toString(StandardCharsets.UTF_8.name());
			response = new GatewayResponse(body, flattenedHeaders, statusType, binaryResponse);
		}

		@Override
		public GatewayResponse getResponse() {
			return response;
		}
	}
}
