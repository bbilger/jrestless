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

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerClaims;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.security.OpenIdAddressClaims;
import com.jrestless.security.OpenIdClaimFieldNames;

final class CognitoUserPoolSecurityContextFactory extends AbstractSecurityContextFactory {

	private static final Logger LOG = LoggerFactory.getLogger(CognitoUserPoolSecurityContextFactory.class);

	protected CognitoUserPoolSecurityContextFactory(@Nonnull GatewayRequest request) {
		super(request, AwsAuthenticationSchemes.AWS_COGNITO_USER_POOL);
	}

	@Override
	protected boolean isApplicable() {
		return getRawClaimsSafe() != null;
	}

	@Override
	protected boolean isValid() {
		Object rawClaims = getRawClaimsSafe();
		if (!(rawClaims instanceof Map)) {
			LOG.debug("claims must be a map");
			return false;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> claims = (Map<String, Object>) rawClaims;
		if (!(claims.get(OpenIdClaimFieldNames.STANDARD_CLAIM_SUB) instanceof String)) {
			LOG.debug("sub claim must be present and of type string");
			return false;
		}
		Object rawAddressClaim = claims.get(OpenIdClaimFieldNames.STANDARD_CLAIM_ADDRESS);
		if (rawAddressClaim != null && !(rawAddressClaim instanceof Map)) {
			LOG.debug("address claim must be a map if present");
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Principal createPrincipal() {
		Map<String, Object> claims = (Map<String, Object>) getRawClaimsSafe();
		OpenIdAddressClaims openIdAddressClaims = createAddressClaims(
				(Map<String, Object>) claims.get(OpenIdClaimFieldNames.STANDARD_CLAIM_ADDRESS));

		CognitoUserPoolAuthorizerClaims cognitoClaims = createCognitoUserPoolAuthorizerClaims(claims,
				openIdAddressClaims);

		CognitoUserPoolAuthorizerPrincipal principal = () -> cognitoClaims;
		return principal;
	}

	private Object getRawClaimsSafe() {
		return getRawClaimsSafe(getAuthorizerDataSafe());
	}

	private static Object getRawClaimsSafe(Map<String, Object> authorizerData) {
		if (authorizerData == null) {
			return null;
		}
		return authorizerData.get("claims");
	}

	private static OpenIdAddressClaims createAddressClaims(Map<String, Object> addressClaims) {
		if (addressClaims == null) {
			return null;
		}
		return () -> addressClaims;
	}

	private static CognitoUserPoolAuthorizerClaims createCognitoUserPoolAuthorizerClaims(Map<String, Object> claims,
			OpenIdAddressClaims openIdAddressClaims) {
		return new CognitoUserPoolAuthorizerClaims() {
			@Override
			public OpenIdAddressClaims getAddress() {
				return openIdAddressClaims;
			}
			@Override
			public Map<String, Object> getAllClaims() {
				return claims;
			}
		};
	}
}
