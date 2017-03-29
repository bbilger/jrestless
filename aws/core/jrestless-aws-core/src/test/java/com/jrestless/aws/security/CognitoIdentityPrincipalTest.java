package com.jrestless.aws.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class CognitoIdentityPrincipalTest {

	@Test
	public void testNameReturnsCognitoIdentityId() {
		CognitoIdentityPrincipal principal = mock(CognitoIdentityPrincipal.class);
		when(principal.getName()).thenCallRealMethod();
		when(principal.getCognitoIdentityId()).thenReturn("cognitoIdentityId");
		assertEquals("cognitoIdentityId", principal.getName());
	}

}
