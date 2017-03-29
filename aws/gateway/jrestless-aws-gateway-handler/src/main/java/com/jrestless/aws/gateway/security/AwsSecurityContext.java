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

import java.security.Principal;

import javax.annotation.Nullable;
import javax.ws.rs.core.SecurityContext;

class AwsSecurityContext implements SecurityContext {

	private final String authenticationScheme;
	private final Principal principal;

	AwsSecurityContext(@Nullable String authenticationScheme, @Nullable Principal principal) {
		this.authenticationScheme = authenticationScheme;
		this.principal = principal;
	}

	@Override
	public Principal getUserPrincipal() {
		return principal;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public boolean isSecure() {
		// always true since API Gateway can be invoked via HTTPS, only
		return true;
	}

	@Override
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}

}
