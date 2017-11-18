package com.jrestless.aws.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;


public class CognitoUserPoolAuthorizerClaimsTest {

	@Test
	public void getName_PrincipalIdInClaimsGiven_ShouldReturnPrincipalIdFromClaims() {
		CognitoUserPoolAuthorizerClaims claims = mock(CognitoUserPoolAuthorizerClaims.class);
		when(claims.getAllClaims()).thenReturn(Collections.singletonMap("cognito:username", "someCognitoUsernameValue"));
		when(claims.getCognitoUserName()).thenCallRealMethod();
		assertEquals("someCognitoUsernameValue", claims.getCognitoUserName());
	}
}
