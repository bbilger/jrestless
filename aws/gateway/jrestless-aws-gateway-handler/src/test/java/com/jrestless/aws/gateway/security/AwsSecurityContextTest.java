package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.security.Principal;

import org.junit.jupiter.api.Test;

public class AwsSecurityContextTest {

	@Test
	public void getAuthenticationScheme_NullAuthSchemeGiven_ShouldReturnNull() {
		assertNull(new AwsSecurityContext(null, null).getAuthenticationScheme());
	}

	@Test
	public void getAuthenticationScheme_AuthSchemeGiven_ShouldReturnAuthScheme() {
		assertEquals("authScheme", new AwsSecurityContext("authScheme", null).getAuthenticationScheme());
	}

	@Test
	public void getUserPrincipal_NullPrincipalGiven_ShouldReturnNull() {
		assertNull(new AwsSecurityContext(null, null).getAuthenticationScheme());
	}

	@Test
	public void getUserPrincipal_PrincipalGiven_ShouldReturnPrincipal() {
		Principal principal = mock(Principal.class);
		assertEquals(principal, new AwsSecurityContext(null, principal).getUserPrincipal());
	}

	@Test
	public void isSecure_Any_ShouldAlwaysBeSecure() {
		assertTrue(new AwsSecurityContext(null, null).isSecure());
	}

	@Test
	public void isUserInRole_Any_ShouldNeverBeInRole() {
		Principal principal = mock(Principal.class);
		assertFalse(new AwsSecurityContext(null, null).isUserInRole("user"));
		assertFalse(new AwsSecurityContext(null, null).isUserInRole(null));
		assertFalse(new AwsSecurityContext("123", null).isUserInRole("user"));
		assertFalse(new AwsSecurityContext("123", principal).isUserInRole("user"));
		assertFalse(new AwsSecurityContext("123", principal).isUserInRole(null));
		assertFalse(new AwsSecurityContext(null, principal).isUserInRole("user"));
		verifyZeroInteractions(principal);
	}
}
