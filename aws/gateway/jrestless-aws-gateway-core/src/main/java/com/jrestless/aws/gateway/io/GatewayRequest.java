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
package com.jrestless.aws.gateway.io;

import java.util.Map;

/**
 * The request passed through from AWS API Gateway to the Lambda function.
 * <p>
 * It can be injected into resources via {@code @Context}.
 *
 * @author Bjoern Bilger
 *
 */
public interface GatewayRequest {
	/**
	 * The path to your resource. For more information, see <a href=
	 * "http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-create-api-step-by-step.html">Build
	 * an API Gateway API as an HTTP Proxy</a>.
	 */
	String getResource();

	/**
	 * The path of the requested resource.
	 * <p>
	 * Path parameters are already resolved but query parameters are not part of
	 * it.
	 */
	String getPath();

	/**
	 * The HTTP method used. Valid values include: DELETE, GET, HEAD, OPTIONS,
	 * PATCH, POST, and PUT.
	 */
	String getHttpMethod();

	/**
	 * The headers of the request.
	 */
	Map<String, String> getHeaders();

	/**
	 * The query parameters of the request.
	 */
	Map<String, String> getQueryStringParameters();

	/**
	 * The path parameters of the request.
	 */
	Map<String, String> getPathParameters();

	/**
	 * The stage variables.
	 */
	Map<String, String> getStageVariables();

	/**
	 * The request context associated with the request.
	 */
	GatewayRequestContext getRequestContext();

	/**
	 * The request body.
	 */
	String getBody();
}
