package com.jrestless.aws.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class IamPrincipalTest {

	@Test
	public void testNameReturnsCognitoIdentityId() {
		IamPrincipal principal = mock(IamPrincipal.class);
		when(principal.getName()).thenCallRealMethod();
		when(principal.getUserArn()).thenReturn("userArn");
		assertEquals("userArn", principal.getName());
	}
}
