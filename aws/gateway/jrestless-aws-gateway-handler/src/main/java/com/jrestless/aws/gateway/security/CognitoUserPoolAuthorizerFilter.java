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
package com.jrestless.aws.gateway.security;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.aws.security.CognitoUserPoolAuthorizerClaims;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.security.OpenIdAddressClaims;
import com.jrestless.security.OpenIdClaimFieldNames;

/**
 * Filter to set a security context with a
 * {@link CognitoUserPoolAuthorizerPrincipal} if the request contains the
 * required data.
 * <p>
 * Note: {@link com.jrestless.aws.gateway.io.GatewayRequest} must contain a
 * {@link com.jrestless.aws.gateway.io.GatewayRequestContext}, its authorizer
 * data must contain the claims property (map) and the claims map must contain
 * the sub property (non-blank string).
 * <p>
 * If you want to use roles, you need to override {@link #createSecurityContext(CognitoUserPoolAuthorizerPrincipal)}.
 *
 * @author Bjoern Bilger
 *
 */
@Priority(Priorities.AUTHORIZATION)
public class CognitoUserPoolAuthorizerFilter extends AuthorizerFilter {

	private static final Logger LOG = LoggerFactory.getLogger(CognitoUserPoolAuthorizerFilter.class);

	/**
	 * Creates a security context using the passed principal.
	 * <p>
	 * Note: the security context's {@link SecurityContext#isUserInRole(String)}
	 * will return false, always. So you want to overwrite this method if you
	 * want to use roles.
	 *
	 * @param principal
	 *            the principal to be used in the security context.
	 * @return security context using the passed principal
	 */
	protected SecurityContext createSecurityContext(@Nonnull CognitoUserPoolAuthorizerPrincipal principal) {
		return new AuthorizerSecurityContext("cognito_user_pool_authorizer", principal);
	}

	@SuppressWarnings("unchecked")
	@Override
	final SecurityContext createSecurityContext(Map<String, Object> authorizerData) {
		Map<String, Object> claims = (Map<String, Object>) authorizerData.get("claims");
		if (claims != null) {
			Object subClaimObj = claims.get(OpenIdClaimFieldNames.STANDARD_CLAIM_SUB);
			if (subClaimObj == null) {
				LOG.warn("sub claim is not set");
			} else if (subClaimObj instanceof String) {
				String subClaim = (String) subClaimObj;
				if (!subClaim.trim().isEmpty()) {
					OpenIdAddressClaims openIdAddressClaims = createAddressClaims(
							(Map<String, Object>) claims.get(OpenIdClaimFieldNames.STANDARD_CLAIM_ADDRESS));

					CognitoUserPoolAuthorizerClaims cognitoClaims = createCognitoUserPoolAuthorizerClaims(claims,
							openIdAddressClaims);

					CognitoUserPoolAuthorizerPrincipal principal = () -> cognitoClaims;
					return createSecurityContext(principal);
				} else {
					LOG.warn("sub claim may not be empty or blank");
				}
			} else {
				LOG.warn("sub claim is not a string but '" + subClaimObj.getClass().getName() + "'");
			}
		}
		return null;
	}

	private OpenIdAddressClaims createAddressClaims(Map<String, Object> addressClaims) {
		if (addressClaims == null) {
			return null;
		}
		return name -> addressClaims.get(name);
	}

	private CognitoUserPoolAuthorizerClaims createCognitoUserPoolAuthorizerClaims(Map<String, Object> claims,
			OpenIdAddressClaims openIdAddressClaims) {
		return new CognitoUserPoolAuthorizerClaims() {
			@Override
			public OpenIdAddressClaims getAddress() {
				return openIdAddressClaims;
			}
			@Override
			public Object getClaim(String name) {
				return claims.get(name);
			}
		};
	}
}
