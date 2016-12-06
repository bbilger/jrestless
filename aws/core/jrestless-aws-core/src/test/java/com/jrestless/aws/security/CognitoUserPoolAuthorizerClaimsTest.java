package com.jrestless.aws.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class CognitoUserPoolAuthorizerClaimsTest {

	@Test
	public void getName_PrincipalIdInClaimsGiven_ShouldReturnPrincipalIdFromClaims() {
		CognitoUserPoolAuthorizerClaims claims = mock(CognitoUserPoolAuthorizerClaims.class);
		when(claims.getClaim("cognito:username")).thenReturn("someCognitoUsernameValue");
		when(claims.getCognitoUserName()).thenCallRealMethod();
		assertEquals("someCognitoUsernameValue", claims.getCognitoUserName());
	}
}
