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

/**
 * The identity of the request passed through from AWS API Gateway to the Lambda
 * function.
 *
 * @author Bjoern Bilger
 *
 */
public interface GatewayIdentity {
	/**
	 * The Amazon Cognito identity pool ID of the caller making the request.
	 * Available only if the request was signed with Amazon Cognito credentials.
	 */
	String getCognitoIdentityPoolId();

	/**
	 * The AWS account ID associated with the request.
	 */
	String getAccountId();

	/**
	 * The Amazon Cognito identity ID of the caller making the request.
	 * Available only if the request was signed with Amazon Cognito credentials.
	 */
	String getCognitoIdentityId();

	/**
	 * The principal identifier of the caller making the request.
	 */
	String getCaller();

	/**
	 * The API owner key associated with your API.
	 */
	String getApiKey();

	/**
	 * The source IP address of the TCP connection making the request to API
	 * Gateway.
	 */
	String getSourceIp();

	// undocumented
	String getAccessKey();

	/**
	 * The Amazon Cognito authentication type of the caller making the request.
	 * Available only if the request was signed with Amazon Cognito credentials.
	 */
	String getCognitoAuthenticationType();

	/**
	 * The Amazon Cognito authentication provider used by the caller making the
	 * request. Available only if the request was signed with Amazon Cognito
	 * credentials.
	 * <p>
	 * For information related to this and the other Amazon Cognito $context
	 * variables, see
	 * <a href="http://docs.aws.amazon.com/cognito/devguide/identity/">Amazon
	 * Cognito Identity</a>.
	 *
	 * @return
	 */
	String getCognitoAuthenticationProvider();

	/**
	 * The Amazon Resource Name (ARN) of the effective user identified after
	 * authentication.
	 */
	String getUserArn();

	/**
	 * The User Agent of the API caller.
	 */
	String getUserAgent();

	/**
	 * The principal identifier of the user making the request.
	 */
	String getUser();
}
