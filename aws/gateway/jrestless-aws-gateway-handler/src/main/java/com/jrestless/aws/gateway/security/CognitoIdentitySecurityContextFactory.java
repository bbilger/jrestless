/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.aws.gateway.security;

import java.security.Principal;

import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CognitoIdentityPrincipal;

final class CognitoIdentitySecurityContextFactory extends AbstractSecurityContextFactory {

	protected CognitoIdentitySecurityContextFactory(GatewayRequest request) {
		super(request, AwsAuthenticationSchemes.AWS_COGNITO_IDENTITY);
	}

	@Override
	protected boolean isApplicable() {
		GatewayIdentity identity = getIdentitySafe();
		if (identity == null) {
			return false;
		}
		return identity.getCognitoAuthenticationType() != null;
	}

	@Override
	protected boolean isValid() {
		return getIdentitySafe().getCognitoIdentityId() != null;
	}

	@Override
	protected Principal createPrincipal() {
		GatewayIdentity identity = getIdentitySafe();
		String cognitoIdentityId = identity.getCognitoIdentityId();
		String cognitoIdentityPoolId = identity.getCognitoIdentityPoolId();
		String cognitoAuthenticationType = identity.getCognitoAuthenticationType();
		String cognitoAuthenticationProvider = identity.getCognitoAuthenticationProvider();
		String userArn = identity.getUserArn();
		String user = identity.getUser();
		String accessKey = identity.getAccessKey();
		String caller = identity.getCaller();
		return new CognitoIdentityPrincipal() {
			@Override
			public String getCognitoIdentityId() {
				return cognitoIdentityId;
			}
			@Override
			public String getCognitoIdentityPoolId() {
				return cognitoIdentityPoolId;
			}
			@Override
			public String getCognitoAuthenticationType() {
				return cognitoAuthenticationType;
			}
			@Override
			public String getCognitoAuthenticationProvider() {
				return cognitoAuthenticationProvider;
			}
			@Override
			public String getUserArn() {
				return userArn;
			}
			@Override
			public String getUser() {
				return user;
			}
			@Override
			public String getAccessKey() {
				return accessKey;
			}
			@Override
			public String getCaller() {
				return caller;
			}
		};
	}
}
