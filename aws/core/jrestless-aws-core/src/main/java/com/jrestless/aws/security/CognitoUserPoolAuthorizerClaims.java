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
package com.jrestless.aws.security;

import com.jrestless.security.OpenIdIdTokenClaims;
import com.jrestless.security.OpenIdStandardClaims;

/**
 * Claims made by a cognito user pool authorizer.
 * <p>
 * The claims will contain all required ID token claims (see
 * {@link OpenIdIdTokenClaims} and can contain more standard claims (see
 * {@link OpenIdStandardClaims}), as well as custom claims. Which claims will be
 * available and are required or optional depends on the cognitio user pool
 * configuration.
 * <p>
 * See <a href=
 * "http://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-integrate-with-cognito.html">
 * http://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-integrate-with-cognito.html</a>
 *
 * @author Bjoern Bilger
 *
 */
public interface CognitoUserPoolAuthorizerClaims extends OpenIdIdTokenClaims, OpenIdStandardClaims {
	String OPEN_ID_CLAIM_CUSTOM_COGNITO_USER_NAME = "cognito:username";

	default String getCognitoUserName() {
		return (String) getClaim(OPEN_ID_CLAIM_CUSTOM_COGNITO_USER_NAME);
	}
}
