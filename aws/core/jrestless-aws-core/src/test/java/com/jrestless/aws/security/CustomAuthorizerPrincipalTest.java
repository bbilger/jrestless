package com.jrestless.aws.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class CustomAuthorizerPrincipalTest {
	@Test
	public void getName_PrincipalIdInClaimsGiven_ShouldReturnPrincipalIdFromClaims() {
		CustomAuthorizerPrincipal principal = mock(CustomAuthorizerPrincipal.class);
		CustomAuthorizerClaims claims = mock(CustomAuthorizerClaims.class);
		when(claims.getPrincipalId()).thenReturn("somePrincipalId");
		when(principal.getClaims()).thenReturn(claims);
		when(principal.getName()).thenCallRealMethod();
		assertEquals("somePrincipalId", principal.getName());
	}
}
