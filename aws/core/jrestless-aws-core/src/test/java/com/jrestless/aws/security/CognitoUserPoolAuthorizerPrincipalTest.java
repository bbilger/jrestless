package com.jrestless.aws.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;


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
