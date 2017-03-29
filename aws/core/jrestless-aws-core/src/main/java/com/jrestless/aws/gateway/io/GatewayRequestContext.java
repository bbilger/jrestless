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
 * The request context passed through from AWS API Gateway to the Lambda
 * function.
 *
 * @author Bjoern Bilger
 *
 */
public interface GatewayRequestContext {
	/**
	 * The AWS account ID associated with the request.
	 */
	String getAccountId();

	/**
	 * The identifier API Gateway assigns to your resource.
	 */
	String getResourceId();

	/**
	 * The deployment stage of the API call (for example, Beta or Prod).
	 */
	String getStage();

	/**
	 * An automatically generated ID for the API call.
	 */
	String getRequestId();

	/**
	 * The identity associated with the request.
	 */
	GatewayIdentity getIdentity();

	/**
	 * The path to your resource. For more information, see <a href=
	 * "http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-create-api-step-by-step.html">Build
	 * an API Gateway API as an HTTP Proxy</a>.
	 */
	String getResourcePath();

	/**
	 * The HTTP method used. Valid values include: DELETE, GET, HEAD, OPTIONS,
	 * PATCH, POST, and PUT.
	 */
	String getHttpMethod();

	/**
	 * The identifier API Gateway assigns to your API.
	 */
	String getApiId();

	/**
	 * The authorizer data.
	 * <ul>
	 * <li>If a custom authorizer is used, then the key "principalId" and all
	 * property keys passed through the "context" map will be available.
	 * <li>If a cognito user pool authorizer is used, then the "claims" key will
	 * be available. The "claims" value will be a Map of strings.
	 * <li>If no authorizer is used an empty map will be returned.
	 * </ul>
	 */
	// this data is highly customizable so we use a map, here
	Map<String, Object> getAuthorizer();
}
