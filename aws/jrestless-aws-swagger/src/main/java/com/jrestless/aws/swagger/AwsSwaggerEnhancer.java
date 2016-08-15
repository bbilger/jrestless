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

import static com.jrestless.aws.swagger.util.HeaderUtils.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.jrestless.aws.swagger.util.HeaderUtils.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.jrestless.aws.swagger.util.HeaderUtils.asStaticValue;
import static com.jrestless.aws.swagger.util.LogUtils.createLogIdentifier;
import static com.jrestless.aws.swagger.util.LogUtils.logAndReturn;
import static com.jrestless.aws.swagger.util.LogUtils.logOnSupply;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.collect.ImmutableSet;
import com.jrestless.aws.swagger.models.ApiGatewayAuth;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationExtension;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration.AuthType;
import com.jrestless.aws.swagger.util.AwsAnnotationsUtils;
import com.jrestless.aws.swagger.util.HeaderUtils;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;

/**
 * Strategy for enhancing a Swagger configuration by the AWS's API Gateway
 * extensions "x-amazon-apigateway-integration" and (iff configured)
 * "x-amazon-apigateway-auth".
 *
 * @author Bjoern Bilger
 *
 */
public class AwsSwaggerEnhancer implements SwaggerEnhancer {

	private final LogAdapter log;

	private static final ObjectMapper CONFIGURATION_OBJECT_MAPPER;
	static {
		CONFIGURATION_OBJECT_MAPPER = new ObjectMapper();
		CONFIGURATION_OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
	}
	private static final int STATUS_METHOD_NOT_ALLOWED = 405;
	private static final Set<Integer> DEFAULT_ADDITIONAL_STATUS_CODES;
	static {
		DEFAULT_ADDITIONAL_STATUS_CODES = ImmutableSet.of(
			Status.BAD_REQUEST.getStatusCode(),
			Status.UNAUTHORIZED.getStatusCode(),
			Status.FORBIDDEN.getStatusCode(),
			Status.NOT_FOUND.getStatusCode(),
			STATUS_METHOD_NOT_ALLOWED,
			Status.NOT_ACCEPTABLE.getStatusCode(),
			Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(),
			Status.INTERNAL_SERVER_ERROR.getStatusCode()
		);
	}

	private final ApiGatewayIntegrationExtensionFactory apiGatewayExtensionFactory;
	private final AwsSwaggerConfiguration configuration;
	private final ObjectMapper swaggerObjectMapper = Json.mapper();

	private boolean notifiedAboutCors = false;

	protected AwsSwaggerEnhancer(ApiGatewayIntegrationExtensionFactory apiGatewayExtensionFactory,
			AwsSwaggerConfiguration configuration, LogAdapter log) {
		this.log = log;
		this.apiGatewayExtensionFactory = requireNonNull(apiGatewayExtensionFactory,
				logOnSupply("apiGatewayExtensionFactory may not be null", log));
		this.configuration = requireNonNull(configuration, logOnSupply("configuration may not be null", log));
	}

	protected AwsSwaggerEnhancer(AwsSwaggerConfiguration configuration, LogAdapter log) {
		this(new ApiGatewayIntegrationExtensionFactoryImpl(log, configuration.getLambdaCredential(),
				configuration.getLambdaUri()), configuration, log);
	}

	public AwsSwaggerEnhancer(LogAdapter log) {
		this(readConfigration(log), log);
	}

	private static AwsSwaggerConfiguration readConfigration(LogAdapter log) {
		String configPath = System.getProperty(PROP_AWS_CONFIGURATION);
		if (configPath == null) {
			throw new RuntimeException(
					logAndReturn("no configuration set via property " + PROP_AWS_CONFIGURATION, log));
		}
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			throw new RuntimeException(logAndReturn(
					"the config file '" + configPath + "' provided via " + PROP_AWS_CONFIGURATION + " does not exist",
					log));
		} else if (!configFile.isFile()) {
			throw new RuntimeException(logAndReturn(
					"the config file '" + configPath + "' provided via " + PROP_AWS_CONFIGURATION + " is not a file",
					log));
		}
		try {
			return CONFIGURATION_OBJECT_MAPPER.readValue(configFile, AwsSwaggerConfiguration.class);
		} catch (Exception e) {
			throw new RuntimeException(logAndReturn(e.getMessage(), log));
		}
	}

	@Override
	public void onOperationCreationFinished(OperationContext operationContext) {
		addAdditionalResponses(operationContext);
		for (Response response : operationContext.getOperation().getResponses().values()) {
			addContentTypeHeader(response, operationContext);
			addCorsAllowOriginHeader(response, operationContext);
		}
		setApiGatewayExtension(operationContext);
		setSecurityExtension(operationContext);
	}

	@Override
	public void onSwaggerCreationFinished(Swagger swagger, Function<Operation, Method> operationMethodMapper) {
		if (swagger.getPaths() != null) {
			addCorsOperations(swagger.getPaths().values(), operationMethodMapper);
		}
	}

	protected void addAdditionalResponses(OperationContext operationContext) {
		/*
		 * if no additional response codes are given explicitly, then add
		 * the default ones.
		 */
		if (AwsAnnotationsUtils.getAdditionalStatusCodes(operationContext.getEndpointMethod()).isEmpty()) {
			if (configuration.isSetAdditionalResponseCodes()) {
				for (int statusCode : configuration.getAdditionalResponseCodes()) {
					addResponse(operationContext.getOperation(), statusCode);
				}
			} else {
				for (int statusCode : DEFAULT_ADDITIONAL_STATUS_CODES) {
					addResponse(operationContext.getOperation(), statusCode);
				}
			}
		}
	}

	/**
	 * Adds the content type header to the response if it's not yet present.
	 *
	 * @param response
	 * @param context
	 */
	protected void addContentTypeHeader(Response response, OperationContext context) {
		Map<String, Property> headers = response.getHeaders();
		if (headers == null || !headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
			StringProperty property = new StringProperty();
			property.setDescription(HeaderUtils.asStaticValue(getContentType(context)));
			response.addHeader(HttpHeaders.CONTENT_TYPE, property);
		}
	}

	/**
	 * Returns endpoint's Content-Type. If none is given, "application/json" is
	 * assumed. If multiple are given an exceptions gets thrown since we are not
	 * able to support this at the moment.
	 *
	 *
	 * @param context
	 * @return
	 */
	protected String getContentType(OperationContext context) {
		List<String> produces = context.getOperation().getProduces();
		String contentType;
		if (produces == null || produces.isEmpty()) {
			log.info("the endpoint doesn't produce anything explicitly; assuming '" + MediaType.APPLICATION_JSON + "'"
					+ createLogIdentifier(context));
			contentType = MediaType.APPLICATION_JSON;
		} else if (produces.size() == 1) {
			contentType = produces.get(0);
		} else {
			throw new RuntimeException(logAndReturn(
					"multiple produces is not supported at the moment " + createLogIdentifier(context), log));
		}
		return contentType;
	}

	protected void addCorsAllowOriginHeader(Response response, OperationContext operationContext) {
		Map<String, Property> headers = response.getHeaders();
		if (isCorsEnabled(operationContext.getEndpointMethod())
				&& (headers == null || !headers.containsKey(ACCESS_CONTROL_ALLOW_ORIGIN))) {
			StringProperty prop = new StringProperty();
			String allowOrigin = getCorsAllowOrigin(false);
			prop.setDescription(asStaticValue(allowOrigin));
			response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, prop);
		}
	}

	protected boolean isCorsEnabled(Method endpointMethod) {
		if (configuration.isSetDefaultCorsEnabled()) {
			return AwsAnnotationsUtils.isCorsEnabledOrDefault(endpointMethod, configuration.isDefaultCorsEnabled());
		} else {
			return AwsAnnotationsUtils.isCorsEnabledOrDefault(endpointMethod);
		}
	}

	protected void addCorsOperations(Collection<Path> paths, Function<Operation, Method> operationMethodMapper) {
		for (Path path : paths) {
			Operation optionsOperation = path.getOptions();
			if (optionsOperation == null) {
				Set<String> methodsAllowingCors = getMethodsAllowingCors(path, operationMethodMapper);
				if (!methodsAllowingCors.isEmpty()) {
					String allowHeaders = getCorsAllowHeaders(!notifiedAboutCors);
					String allowOrigin = getCorsAllowOrigin(!notifiedAboutCors);
					notifiedAboutCors = true;
					String corsOperationJson = String.format(CORS_OPERATION_FMT, allowHeaders, allowOrigin,
							StringUtils.join(methodsAllowingCors, ','));
					try {
						Operation corsOperation = swaggerObjectMapper.readValue(corsOperationJson, Operation.class);
						path.setOptions(corsOperation);
					} catch (IOException e) {
						log.error("failed to create CORS operation: " + e.getMessage());
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	protected Set<String> getMethodsAllowingCors(Path path, Function<Operation, Method> operationMethodMapper) {
		Set<String> methodsAllowingCors = new HashSet<>();
		for (Map.Entry<HttpMethod, Operation> opEntry : path.getOperationMap().entrySet())  {
			if (isCorsEnabled(operationMethodMapper.apply(opEntry.getValue()))) {
				methodsAllowingCors.add(opEntry.getKey().toString());
			}
		}
		return methodsAllowingCors;
	}

	protected String getCorsAllowHeaders(boolean logMissing) {
		if (!configuration.isSetDefaultAccessControlAllowHeaders()) {
			if (logMissing) {
				log.info("Setting CORS header '" + ACCESS_CONTROL_ALLOW_HEADERS + "' to '"
						+ DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS
						+ "'. It's recommended to set the actual value in the configuration.");
			}
			return DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS;
		} else {
			return configuration.getDefaultAccessControlAllowHeaders();
		}
	}

	protected String getCorsAllowOrigin(boolean logMissing) {
		if (!configuration.isSetDefaultAccessControlAllowOrigin()) {
			if (logMissing) {
				log.warn("Setting CORS header '" + ACCESS_CONTROL_ALLOW_ORIGIN
						+ "' to '*'. It's highly recommended to set the actual value in the configuration.");
			}
			return "*";
		} else {
			return configuration.getDefaultAccessControlAllowOrigin();
		}
	}

	protected void setApiGatewayExtension(OperationContext operationContext) {
		operationContext.getOperation().setVendorExtension(ApiGatewayIntegrationExtension.EXTENSION_NAME,
				apiGatewayExtensionFactory.createApiGatewayExtension(operationContext, configuration));

	}

	protected void setSecurityExtension(OperationContext operationContext) {
		Operation operation = operationContext.getOperation();
		// there's an explicit security set => don't add anything
		List<Map<String, List<String>>> security = operation.getSecurity();
		if (security != null && security.size() > 0) {
			return;
		}

		boolean securedEndpoint = AwsAnnotationsUtils.isSecured(operationContext.getEndpointMethod());
		if (securedEndpoint) {
			if (configuration.isSetDefaultAuthType() && !AuthType.NONE.equals(configuration.getDefaultAuthType())) {
				AuthType authType = configuration.getDefaultAuthType();
				if (AuthType.IAM.equals(authType)) {
					operation.setVendorExtension(ApiGatewayAuth.EXTENSION_NAME, new ApiGatewayAuth());
				} else if (AuthType.AUTHORIZER.equals(authType)) {
					if (!configuration.isSetDefaultSecurity()) {
						log.warn("setting 'defaultAuthType' to \"authorizer\" requires 'defaultSecurity' to be set");
					} else {
						operation.setSecurity(configuration.getDefaultSecurity());
					}
				}
			} else {
				log.warn("Found secured endpoint but no security is defined: " + createLogIdentifier(operationContext));
			}
		}
	}

	private void addResponse(Operation operation, int statusCode, Response response) {
		operation.getResponses().putIfAbsent(Integer.toString(statusCode), response);
	}

	private void addResponse(Operation operation, int statusCode) {
		addResponse(operation, statusCode, new Response());
	}

	// CHECKSTYLE:OFF
	protected static final String PROP_AWS_CONFIGURATION = "aws-swagger-configuration";

	protected static final String DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS = "Content-Type,X-Amz-Date,Authorization,X-Api-Key";

	protected static final String DEFAULT_ACCESS_CONTROL_ALLOW_ORIGIN = "*";

	/**
	 * Format string for a CORS operation.
	 * <ol>
	 * <li>arg0: static value for Access-Control-Allow-Headers
	 * <li>arg1: static value for Access-Control-Allow-Origin
	 * <li>arg2: static value for Access-Control-Allow-Methods
	 * </ol>
	 */
	// in Java and not in a resource file since gradle has some probs loading them
	protected static final String CORS_OPERATION_FMT = ""
			+ "{"
			+ "  \"tags\": [\"CORS\"],"
			+ "  \"summary\": \"CORS support\","
			+ "  \"description\": \"Enable CORS by returning correct headers\","
			+ "  \"consumes\": [\"application/json\"],"
			+ "  \"produces\": [\"application/json\"],"
			+ "  \"parameters\": [],"
			+ "  \"responses\": {"
			+ "    \"200\": {"
			+ "      \"description\": \"Default response for CORS method\","
			+ "      \"headers\": {"
			+ "        \"Access-Control-Allow-Headers\": {"
			+ "          \"type\": \"string\""
			+ "        },"
			+ "        \"Access-Control-Allow-Methods\": {"
			+ "          \"type\": \"string\""
			+ "        },"
			+ "        \"Access-Control-Allow-Origin\": {"
			+ "          \"type\": \"string\""
			+ "        }"
			+ "      }"
			+ "    }"
			+ "  },"
			+ "  \"x-amazon-apigateway-integration\": {"
			+ "    \"requestTemplates\": {"
			+ "      \"application/json\": \"{\\\"statusCode\\\":  200}\""
			+ "    },"
			+ "    \"requestParameters\": {},"
			+ "    \"responses\": {"
			+ "      \"default\": {"
			+ "        \"statusCode\": \"200\","
			+ "        \"responseParameters\": {"
			+ "          \"method.response.header.Access-Control-Allow-Headers\": \"'%s'\","
			+ "          \"method.response.header.Access-Control-Allow-Origin\": \"'%s'\","
			+ "          \"method.response.header.Access-Control-Allow-Methods\": \"'%s'\""
			+ "        },"
			+ "        \"responseTemplates\": {"
			+ "          \"application/json\": \"{}\""
			+ "        }"
			+ "      }"
			+ "    },"
			+ "    \"type\": \"mock\","
			+ "    \"httpMethod\": \"POST\""
			+ "  }"
			+ "}";
	// CHECKSTYLE:ON
}
