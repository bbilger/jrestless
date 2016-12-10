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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;

abstract class AuthorizerFilter implements ContainerRequestFilter {

	private final GatewayRequest gatewayRequest;

	AuthorizerFilter(@Nonnull GatewayRequest gatewayRequest) {
		this.gatewayRequest = Objects.requireNonNull(gatewayRequest);
	}

	@Override
	public final void filter(ContainerRequestContext requestContext) throws IOException {
		Map<String, Object> authorizerData = getAuthorizerData();
		if (authorizerData != null) {
			SecurityContext securityContext = createSecurityContext(authorizerData);
			if (securityContext != null) {
				requestContext.setSecurityContext(securityContext);
			}
		}
	}

	private Map<String, Object> getAuthorizerData() {
		GatewayRequestContext gatewayRequestContext = gatewayRequest.getRequestContext();
		if (gatewayRequestContext == null) {
			return null;
		}
		return gatewayRequestContext.getAuthorizer();
	}

	abstract SecurityContext createSecurityContext(@Nonnull Map<String, Object> authorizerData);

}
