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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnonSecurityContextTest {
	@Test
	public void getUserPrincipal_ShouldAlwaysBeNull() {
		assertNull(new AnonSecurityContext().getUserPrincipal());
	}

	@Test
	public void isUserInRole_ShouldNeverBeInRole() {
		assertFalse(new AnonSecurityContext().isUserInRole(null));
		assertFalse(new AnonSecurityContext().isUserInRole(""));
		assertFalse(new AnonSecurityContext().isUserInRole("user"));
		assertFalse(new AnonSecurityContext().isUserInRole("admin"));
	}

	@Test
	public void getAuthenticationScheme_ShouldAlwaysBeNull() {
		assertNull(new AnonSecurityContext().getAuthenticationScheme());
		assertNull(new AnonSecurityContext(false).getAuthenticationScheme());
		assertNull(new AnonSecurityContext(true).getAuthenticationScheme());
	}

	@Test
	public void isSecure_SecureConstructorArgGiven_ShouldBeSecure() {
		assertTrue(new AnonSecurityContext(true).isSecure());
	}

	@Test
	public void isSecure_InsecureConstructorArgGiven_ShouldBeInSecure() {
		assertFalse(new AnonSecurityContext(false).isSecure());
	}

	@Test
	public void isSecure_NoSecurityConstructorArgGiven_ShouldBeSecure() {
		assertTrue(new AnonSecurityContext().isSecure());
	}
}
