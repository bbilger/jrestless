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

import com.jrestless.aws.security.CustomAuthorizerClaims;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;

/**
 * Filter to set a security context with the
 * {@link CustomAuthorizerPrincipal} if the request contains the
 * required data.
 * <p>
 * If you want to use roles, you need to override {@link #createSecurityContext(CustomAuthorizerPrincipal)}.
 * <p>
 * Note: {@link com.jrestless.aws.gateway.io.GatewayRequest} must contain a
 * {@link com.jrestless.aws.gateway.io.GatewayRequestContext}, its authorizer
 * data must contain the principalId property (non-blank string).
 *
 * @author Bjoern Bilger
 *
 */
@Priority(Priorities.AUTHORIZATION)
public class CustomAuthorizerFilter extends AuthorizerFilter {

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
	protected SecurityContext createSecurityContext(@Nonnull CustomAuthorizerPrincipal principal) {
		return new AuthorizerSecurityContext("cognito_custom_authorizer", principal);
	}

	@Override
	final SecurityContext createSecurityContext(Map<String, Object> authorizerData) {
		Object principalIdObj = authorizerData.get("principalId");
		if (principalIdObj != null) {
			if (principalIdObj instanceof String) {
				String principalId = (String) principalIdObj;
				if (!principalId.trim().isEmpty()) {
					CustomAuthorizerPrincipal principal = new CustomAuthorizerPrincipal() {
						@Override
						public CustomAuthorizerClaims getClaims() {
							return new CustomAuthorizerClaims() {
								@Override
								public String getPrincipalId() {
									return principalId;
								}
								@Override
								public Object getClaim(String name) {
									return authorizerData.get(name);
								}
							};
						}
					};
					return createSecurityContext(principal);
				} else {
					LOG.warn("principalId may not be empty or blank");
				}
			} else {
				LOG.warn("principalId is not a string but '" + principalIdObj.getClass().getName() + "'");
			}
		}
		return null;
	}
}
