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
import java.util.Map;

import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CustomAuthorizerClaims;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;

final class CustomAuthorizerSecurityContextFactory extends AbstractSecurityContextFactory {

	protected CustomAuthorizerSecurityContextFactory(GatewayRequest request) {
		super(request, AwsAuthenticationSchemes.AWS_CUSTOM_AUTHORIZER);
	}

	@Override
	protected boolean isApplicable() {
		return getRawPrincipalIdSafe() != null;
	}

	@Override
	protected boolean isValid() {
		return getRawPrincipalIdSafe() instanceof String;
	}

	@Override
	protected Principal createPrincipal() {
		Map<String, Object> authorizerData = getAuthorizerDataSafe();
		String principalId = (String) getRawPrincipalIdSafe(authorizerData);
		CustomAuthorizerClaims claims = createCustomAuthorizerClaims(authorizerData, principalId);
		CustomAuthorizerPrincipal principal = () -> claims;
		return principal;
	}

	private Object getRawPrincipalIdSafe() {
		return getRawPrincipalIdSafe(getAuthorizerDataSafe());
	}

	private static Object getRawPrincipalIdSafe(Map<String, Object> authorizerData) {
		if (authorizerData == null) {
			return null;
		}
		return authorizerData.get("principalId");
	}

	private static CustomAuthorizerClaims createCustomAuthorizerClaims(Map<String, Object> authorizerData,
			String principalId) {
		return new CustomAuthorizerClaims() {
			@Override
			public String getPrincipalId() {
				return principalId;
			}

			@Override
			public Map<String, Object> getAllClaims() {
				return authorizerData;
			}
		};
	}
}
