package com.jrestless.aws.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class CognitoUserPoolAuthorizerPrincipalTest {

	@Test
	public void getName_SubInClaimsGiven_ShouldReturnSubFromClaims() {
		CognitoUserPoolAuthorizerPrincipal principal = mock(CognitoUserPoolAuthorizerPrincipal.class);
		CognitoUserPoolAuthorizerClaims claims = mock(CognitoUserPoolAuthorizerClaims.class);
		when(claims.getSub()).thenReturn("someSubValue");
		when(principal.getClaims()).thenReturn(claims);
		when(principal.getName()).thenCallRealMethod();
		assertEquals("someSubValue", principal.getName());
	}

}
