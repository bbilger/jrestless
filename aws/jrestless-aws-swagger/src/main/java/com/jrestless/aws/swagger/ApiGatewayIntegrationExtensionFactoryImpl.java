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
package com.jrestless.aws.swagger;


import static com.jrestless.aws.swagger.util.LogUtils.createLogIdentifier;
import static com.jrestless.aws.swagger.util.LogUtils.logOnSupply;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationExtension;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationResponse;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration;
import com.jrestless.aws.swagger.util.AwsAnnotationsUtils;
import com.jrestless.aws.swagger.util.HeaderUtils;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;

/**
 * {@link ApiGatewayIntegrationExtensionFactory} implementation.
 *
 * @author Bjoern Bilger
 *
 */
public class ApiGatewayIntegrationExtensionFactoryImpl implements ApiGatewayIntegrationExtensionFactory {

	private final LogAdapter log;

	private final String awsCredentials;
	private final String awsLambdaUri;

	public ApiGatewayIntegrationExtensionFactoryImpl(@Nonnull LogAdapter log, @Nonnull String awsCredentials,
			@Nonnull String awsLambdaUri) {
		this.log = requireNonNull(log);
		this.awsCredentials = requireNonNull(awsCredentials, logOnSupply("credentials may not be null", log));
		this.awsLambdaUri = requireNonNull(awsLambdaUri, logOnSupply("lambdaUri may not be null", log));
	}

	@Override
	public ApiGatewayIntegrationExtension createApiGatewayExtension(OperationContext operationContext,
			AwsSwaggerConfiguration configuration) {
		requireNonNull(operationContext);
		requireNonNull(configuration);
		AwsOperationContext context = new AwsOperationContext(operationContext.getOperation(),
				operationContext.getEndpointMethod(), operationContext.getSwagger(), configuration);

		return new ApiGatewayIntegrationExtension(awsCredentials, awsLambdaUri,
				createRequestTemplates(context), ImmutableMap.of(), createIntegrationResponses(context));
	}

	/**
	 * Returns the response template. It differs depending on whether it's the
	 * default response, or not.
	 *
	 * @param responseContext
	 * @return
	 */
	protected String getResponseTemplate(ResponseContext responseContext) {
		if (isDefaultResponse(responseContext)) {
			return INTEGRATION_DEFAULT_RESPONSE_TEMPLATE;
		} else {
			return INTEGRATION_ERROR_RESPONSE_TEMPLATE;
		}
	}

	protected boolean isDefaultResponse(ResponseContext responseContext) {
		String statusCode = responseContext.getStatusCode();
		if ("default".equals(statusCode)) {
			return true;
		} else {
			String defaultStatusCode = getDefaultStatusCode(responseContext.getAwsOperationContext()) + "";
			return defaultStatusCode.equals(statusCode);
		}
	}

	protected boolean isDynamicHeaderValue(String headerValue) {
		return headerValue.startsWith(RESPONSE_PARAM_VALUE_INTEGRATION_RESPONSE_BODY_PREFIX);
	}

	/**
	 * Returns the endpoint's default status code. If none is given on the
	 * endpoint, then the resource's status code is given. If none is given there
	 * either, then 200 is being returned.
	 *
	 * @param context
	 * @return
	 */
	protected int getDefaultStatusCode(AwsOperationContext context) {
		return AwsAnnotationsUtils.getDefaultStatusCodeOrDefault(context.getEndpointMethod());
	}

	protected Map<String, ApiGatewayIntegrationResponse> createIntegrationResponses(AwsOperationContext context) {
		Operation operation = context.getOperation();
		Map<String, Response> responses = operation.getResponses();
		Map<String, ApiGatewayIntegrationResponse> integrationResponses = new HashMap<>();
		for (Map.Entry<String, Response> responseEntry : responses.entrySet()) {
			String statusCode = responseEntry.getKey();
			Response response = responseEntry.getValue();
			Map.Entry<String, ApiGatewayIntegrationResponse> intResponse = createIntegrationResponse(
					new ResponseContext(context, response, statusCode));
			integrationResponses.put(intResponse.getKey(), intResponse.getValue());
		}
		return integrationResponses;
	}

	protected Map.Entry<String, ApiGatewayIntegrationResponse> createIntegrationResponse(
			ResponseContext responseContext) {
		Map<String, String> integrationResponseParameters = createIntegrationResponseParameters(responseContext);
		String integrationStatusCodePattern = getIntegrationStatusCodePattern(responseContext);
		Map<String, String> responseTemplate = ImmutableMap.of(MediaType.APPLICATION_JSON,
				getResponseTemplate(responseContext));
		return new SimpleEntry<>(integrationStatusCodePattern, new ApiGatewayIntegrationResponse(
				getStatusCode(responseContext), integrationResponseParameters, responseTemplate));
	}

	protected Map<String, String> createIntegrationResponseParameters(ResponseContext responseContext) {
		AwsOperationContext context = responseContext.getAwsOperationContext();
		Response response = responseContext.getResponse();

		Map<String, String> integrationResponseParameters = new HashMap<>();

		if (response.getHeaders() != null) {
			for (Map.Entry<String, Property> swaggerHeaderEntry : response.getHeaders().entrySet()) {
				String swaggerHeaderName = swaggerHeaderEntry.getKey();
				String swaggerHeaderValue = swaggerHeaderEntry.getValue().getDescription();
				if (StringUtils.isBlank(swaggerHeaderName)) {
					warnSkipHeader("missing header name; add it via @ResponseHeader#name",
							createLogIdentifier(context));
					continue;
				}
				if (StringUtils.isBlank(swaggerHeaderValue)) {
					swaggerHeaderValue = RESPONSE_PARAM_VALUE_INTEGRATION_RESPONSE_BODY_HEADERS_PREFIX
							+ swaggerHeaderName;
				}

				boolean staticHeaderValue = HeaderUtils.isStaticValue(swaggerHeaderValue);
				boolean dynamicHeaderValue = isDynamicHeaderValue(swaggerHeaderValue);
				boolean defaultResponse = isDefaultResponse(responseContext);
				if (!staticHeaderValue && !dynamicHeaderValue) {
					warnSkipHeader(
							"Invalid header value. Headers must either be static ('yourStaticValueInSingleQuotes'),"
									+ " or must start with '"
									+ RESPONSE_PARAM_VALUE_INTEGRATION_RESPONSE_BODY_PREFIX
									+ "'. They must be passed via @ResponseHeader#description.",
									createLogIdentifier(context, "headerName=" + swaggerHeaderName,
											"headerValue=" + swaggerHeaderValue));
				} else if (!defaultResponse && dynamicHeaderValue) {
					warnSkipHeader(
							"Non-default responses support static header values ('yourStaticValueInSingleQuotes'),"
									+ " only. They must be passed via @ResponseHeader#description.",
									createLogIdentifier(context, "headerName=" + swaggerHeaderName,
											"headerValue=" + swaggerHeaderValue));
				} else {
					integrationResponseParameters.put(
							RESPONSE_PARAM_KEY_METHOD_RESPONSE_HEADER_PREFIX + swaggerHeaderName, swaggerHeaderValue);
				}
			}
		}

		return integrationResponseParameters;
	}

	/**
	 * Returns the status code pattern for "x-amazon-apigateway-integration" ->
	 * "responses". The returned pattern depends on whether it's the default
	 * response, or not.
	 *
	 * @param responseContext
	 * @return
	 */
	protected String getIntegrationStatusCodePattern(ResponseContext responseContext) {
		if (isDefaultResponse(responseContext)) {
			return DEFAULT_RESPONSE_CODE_PATTERN;
		} else {
			return String.format(RESPONSE_STATUS_CODE_PATTERN_FMT, responseContext.getStatusCode());
		}
	}

	protected String getStatusCode(ResponseContext responseContext) {
		if (isDefaultResponse(responseContext)) {
			return getDefaultStatusCode(responseContext.getAwsOperationContext()) + "";
		} else {
			return responseContext.getStatusCode();
		}
	}

	protected Map<String, String> createRequestTemplates(AwsOperationContext context) {
		return Collections.singletonMap(MediaType.APPLICATION_JSON, JSON_REQUEST_TEMPLATE.replaceAll("  ", ""));
	}

	void warnSkipHeader(String message, String logIdentifier) {
		log.warn("skipped header: " + message + logIdentifier);
	}

	/**
	 * Factory context value object.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class AwsOperationContext extends OperationContext {

		private final AwsSwaggerConfiguration configuration;

		protected AwsOperationContext(Operation operation, Method endpointMethod, Swagger swagger,
				AwsSwaggerConfiguration configuration) {
			super(operation, endpointMethod, swagger);
			requireNonNull(configuration);
			this.configuration = configuration;
		}

		protected AwsSwaggerConfiguration getConfiguration() {
			return configuration;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AwsOperationContext other = (AwsOperationContext) obj;
			if (configuration == null) {
				if (other.configuration != null) {
					return false;
				}
			} else if (!configuration.equals(other.configuration)) {
				return false;
			}
			return true;
		}
	}
	/**
	 * Response context value object.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class ResponseContext {

		private final Response response;
		private final String statusCode;
		private final AwsOperationContext awsOperationContext;

		protected ResponseContext(AwsOperationContext awsOperationContext, Response response, String statusCode) {
			requireNonNull(awsOperationContext);
			requireNonNull(response);
			requireNonNull(statusCode);
			this.response = response;
			this.statusCode = statusCode;
			this.awsOperationContext = awsOperationContext;
		}

		protected Response getResponse() {
			return response;
		}

		protected String getStatusCode() {
			return statusCode;
		}

		protected AwsOperationContext getAwsOperationContext() {
			return awsOperationContext;
		}
	}

	// CHECKSTYLE:OFF
	protected static final String DEFAULT_RESPONSE_CODE_PATTERN = "default";
	protected static final String RESPONSE_PARAM_KEY_METHOD_RESPONSE_HEADER_PREFIX = "method.response.header.";
	protected static final String RESPONSE_PARAM_VALUE_INTEGRATION_RESPONSE_BODY_PREFIX = "integration.response.body.";
	protected static final String RESPONSE_PARAM_VALUE_INTEGRATION_RESPONSE_BODY_HEADERS_PREFIX = "integration.response.body.headers.";
	protected static final String RESPONSE_STATUS_CODE_PATTERN_FMT = "(.|\\n)*\\\"statusCode\\\"\\:\\s*\\n*\\s*\\\"%s\\\"(.|\\n)*";

	protected static final String INTEGRATION_ERROR_RESPONSE_TEMPLATE = "$util.parseJson($input.path('$.errorMessage')).body";
	protected static final String INTEGRATION_DEFAULT_RESPONSE_TEMPLATE = "$input.path('$.body')";

	// in Java and not in a resource file since gradle has some probs loading them
	protected static final String JSON_REQUEST_TEMPLATE = ""
			+ "{\n"
			+ "  \"body\": \"$util.escapeJavaScript($input.json('$'))\",\n"
			+ "  \"headers\": {\n"
			+ "    #foreach($param in $input.params().header.keySet())\n"
			+ "      \"$param\": \"$util.escapeJavaScript($input.params().header.get($param))\" #if($foreach.hasNext),#end\n"
			+ "    #end\n"
			+ "  },\n"
			+ "  \"queryParams\": {\n"
			+ "    #foreach($param in $input.params().querystring.keySet())\n"
			+ "      \"$param\": \"$util.escapeJavaScript($input.params().querystring.get($param))\" #if($foreach.hasNext),#end\n"
			+ "    #end\n"
			+ "  },\n"
			+ "  \"pathParams\": {\n"
			+ "    #foreach($param in $input.params().path.keySet())\n"
			+ "      \"$param\": \"$util.escapeJavaScript($input.params().path.get($param))\" #if($foreach.hasNext),#end\n"
			+ "    #end\n"
			+ "  },\n"
			+ "  \"context\": {\n"
			+ "    \"apiId\":\"$context.apiId\",\n"
			+ "    \"principalId\":\"$util.escapeJavaScript($context.authorizer.principalId)\",\n"
			+ "    \"httpMethod\":\"$context.httpMethod\",\n"
			+ "    \"accountId\":\"$context.identity.accountId\",\n"
			+ "    \"apiKey\":\"$context.identity.apiKey\",\n"
			+ "    \"caller\":\"$context.identity.caller\",\n"
			+ "    \"cognitoAuthenticationProvider\":\"$context.identity.cognitoAuthenticationProvider\",\n"
			+ "    \"cognitoAuthenticationType\":\"$context.identity.cognitoAuthenticationType\",\n"
			+ "    \"cognitoIdentityId\":\"$context.identity.cognitoIdentityId\",\n"
			+ "    \"cognitoIdentityPoolId\":\"$context.identity.cognitoIdentityPoolId\",\n"
			+ "    \"sourceIp\":\"$context.identity.sourceIp\",\n"
			+ "    \"user\":\"$context.identity.user\",\n"
			+ "    \"userAgent\":\"$context.identity.userAgent\",\n"
			+ "    \"userArn\":\"$context.identity.userArn\",\n"
			+ "    \"requestId\":\"$context.requestId\",\n"
			+ "    \"resourceId\":\"$context.resourceId\",\n"
			+ "    \"resourcePath\":\"$context.resourcePath\",\n"
			+ "    \"stage\":\"$context.stage\",\n"
			+ "    \"stageVariables\": {\n"
			+ "      #foreach($stageKey in $stageVariables.keySet())\n"
			+ "        \"$stageKey\": \"$util.escapeJavaScript($stageVariables.get($stageKey))\" #if($foreach.hasNext),#end\n"
			+ "      #end\n"
			+ "    }\n"
			+ "  }\n"
			+ "}\n";
	// CHECKSTYLE:ON
}
