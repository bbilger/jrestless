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
package com.jrestless.core.security;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * Simple anonymous / unauthenticated security context.
 *
 * @author Bjoern Bilger
 *
 */
public class AnonSecurityContext implements SecurityContext {

	private final boolean secure;

	/**
	 * Creates a new anonymous security context - assuming the request was made
	 * via a secure channel (HTTPS).
	 *
	 * @see {@link #AnonSecurityContext(boolean) AnonSecurityContext(true)}
	 */
	public AnonSecurityContext() {
		this(true);
	}

	/**
	 * Creates a new anonymous security context.
	 *
	 * @param secure
	 *            true if the request was made via a secure channel (HTTPS),
	 *            false otherwise
	 */
	public AnonSecurityContext(boolean secure) {
		this.secure = secure;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return null;
	}
}