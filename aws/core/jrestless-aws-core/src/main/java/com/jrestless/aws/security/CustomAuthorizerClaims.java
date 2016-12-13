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

import javax.annotation.Nonnull;

import com.jrestless.security.Claims;

/**
 * Claims made by a custom authorizer.
 * <p>
 * At minimum a custom authorizer must return a the principalId but it can
 * return additional claims via context. The additional claims can be accessed
 * via {@link CustomAuthorizerClaims#getAllClaims()}.
 * <p>
 * See <a href=
 * "http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html#api-gateway-custom-authorizer-output">
 * http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html#api-gateway-custom-authorizer-output</a>
 *
 * @author Bjoern Bilger
 *
 */
public interface CustomAuthorizerClaims extends Claims {
	/**
	 * Returns the principalId - as returned from the custom authorizer.
	 * @return the principalId
	 */
	@Nonnull
	String getPrincipalId();
}
