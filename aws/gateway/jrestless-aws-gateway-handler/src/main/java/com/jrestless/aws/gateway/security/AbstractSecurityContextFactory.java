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

import static java.util.Objects.requireNonNull;

import java.security.Principal;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.core.security.AnonSecurityContext;

abstract class AbstractSecurityContextFactory {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSecurityContextFactory.class);

	private final GatewayRequest request;
	private final String authenticationScheme;

	protected AbstractSecurityContextFactory(@Nonnull GatewayRequest request, @Nonnull String authenticationScheme) {
		this.request = requireNonNull(request);
		this.authenticationScheme = requireNonNull(authenticationScheme);
	}

	protected abstract boolean isApplicable();

	protected abstract boolean isValid();

	protected abstract Principal createPrincipal();

	protected GatewayRequest getRequest() {
		return request;
	}

	protected GatewayIdentity getIdentitySafe() {
		GatewayRequestContext context = getRequest().getRequestContext();
		if (context == null) {
			return null;
		}
		GatewayIdentity identity = context.getIdentity();
		if (identity == null) {
			return null;
		}
		return identity;
	}

	protected Map<String, Object> getAuthorizerDataSafe() {
		GatewayRequestContext context = getRequest().getRequestContext();
		if (context == null) {
			return null;
		}
		Map<String, Object> authorizerData = context.getAuthorizer();
		if (authorizerData == null) {
			return null;
		}
		return authorizerData;
	}

	public final SecurityContext createSecurityContext() {
		if (!isApplicable()) {
			String msg = "the factory is not applicable for the request";
			LOG.error(msg);
			throw new IllegalStateException(msg);
		}
		if (!isValid()) {
			LOG.warn("the request data is invalid/unexpected thus creating an anonymous security context");
			return new AnonSecurityContext();
		}
		return new AwsSecurityContext(authenticationScheme, createPrincipal());
	}

	public String getAuthenticationScheme() {
		return authenticationScheme;
	}
}
