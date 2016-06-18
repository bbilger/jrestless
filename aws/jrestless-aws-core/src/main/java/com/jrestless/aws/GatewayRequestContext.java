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
package com.jrestless.aws;

import java.util.Map;

/**
 * API Gateway's context for a request.
 * <p>
 * It can be injected into resources via {@code @Context}.
 *
 * @author Bjoern Bilger
 *
 */
public interface GatewayRequestContext {
	/**
	 * Gets the identifier API Gateway assigns to your API.
	 */
	String getApiId();

	/**
	 * Gets the principal user identification associated with the token sent by
	 * the client.
	 */
	String getPrincipalId();

	/**
	 * Gets the HTTP method used. Valid values include: DELETE, GET, HEAD,
	 * OPTIONS, PATCH, POST, and PUT.
	 */
	String getHttpMethod();

	/**
	 * Gets the AWS account ID associated with the request.
	 */
	String getAccountId();

	/**
	 * Gets the API owner key associated with your API.
	 */
	String getApiKey();

	/**
	 * Gets the principal identifier of the caller making the request.
	 */
	String getCaller();

	/**
	 * Gets the Amazon Cognito authentication provider used by the caller making
	 * the request. Available only if the request was signed with Amazon Cognito
	 * credentials.
	 * <p>
	 * For information related to this and the other Amazon Cognito $context
	 * variables, see Amazon Cognito Identity.
	 */
	String getCognitoAuthenticationProvider();

	/**
	 * Gets the Amazon Cognito authentication type of the caller making the
	 * request. Available only if the request was signed with Amazon Cognito
	 * credentials.
	 */
	String getCognitoAuthenticationType();

	/**
	 * Gets the Amazon Cognito identity ID of the caller making the request.
	 * Available only if the request was signed with Amazon Cognito credentials.
	 */
	String getCognitoIdentityId();

	/**
	 * Gets the Amazon Cognito identity pool ID of the caller making the
	 * request. Available only if the request was signed with Amazon Cognito
	 * credentials.
	 */
	String getCognitoIdentityPoolId();

	/**
	 * Gets the IP address of the API caller as determined by the
	 * X-Forwarded-For header.
	 *
	 * Note that this should not be used for security purposes. To get the full
	 * value of X-Forwarded-For addresses you can define the X-Forwarded-For
	 * header in your API and then map it to your integration.
	 */
	String getSourceIp();

	/**
	 * Gets the principal identifier of the user making the request.
	 */
	String getUser();

	/**
	 * Gets the User Agent of the API caller.
	 */
	String getUserAgent();

	/**
	 * Gets the Amazon Resource Name (ARN) of the effective user identified
	 * after authentication.
	 */
	String getUserArn();

	/**
	 * Gets an automatically generated ID for the API call.
	 */
	String getRequestId();

	/**
	 * Gets the identifier API Gateway assigns to your resource.
	 */
	String getResourceId();

	/**
	 * Gets the path to your resource. For more information, see Build an API
	 * Gateway API Step by Step.
	 */
	String getResourcePath();

	/**
	 * Gets the deployment stage of the API call (for example, Beta or Prod).
	 */
	String getStage();

	/**
	 * Gets the stage variables.
	 */
	Map<String, String> getStageVariables();
}
