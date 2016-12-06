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

import java.security.Principal;

import javax.annotation.Nonnull;

/**
 * User authenticated by a custom authorizer.
 * <p>
 * See <a href=
 * "http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html#api-gateway-custom-authorizer-output">
 * http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html#api-gateway-custom-authorizer-output</a>
 *
 * @author Bjoern Bilger
 *
 */
public interface CustomAuthorizerPrincipal extends Principal {
	/**
	 * Returns the user's claims.
	 * @return the user's claims
	 */
	@Nonnull
	CustomAuthorizerClaims getClaims();

	/**
	 * Returns the principalId.
	 *
	 * @return the principalId
	 */
	@Nonnull
	@Override
	default String getName() {
		return getClaims().getPrincipalId();
	}
}
