package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CognitoIdentityPrincipal;

public class CognitoIdentitySecurityContextFactoryTest extends SecurityContextFactoryTestBase {

	@Test
	public void isApplicable_NoContextGiven_ShouldNotBeApplicable() {
		GatewayRequest request = mock(GatewayRequest.class);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_NoIdentityGiven_ShouldNotBeApplicable() {
		GatewayRequest request = mock(GatewayRequest.class);
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(request.getRequestContext()).thenReturn(context);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_NoCognitoAuthenticationTypeGiven_ShouldNotBeApplicable() {
		GatewayRequest request = createRequestMock(null, null, null, null, null, null, null, null);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_CognitoAuthenticationTypeGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock("cognitoAuthenticationType", null, null, null, null, null, null,
				null);
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isValid_NoCognitoIdentityIdGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("cognitoAuthenticationType", null, null, null, null, null, null,
				null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_CognitoIdentityIdGiven_ShouldBeValid() {
		GatewayRequest request = createRequestMock("cognitoAuthenticationType", "cognitoIdentityId", null, null, null,
				null, null, null);
		assertTrue(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void createPrincipal_MinimalDateGiven_ShouldCreatePrincipal() {
		GatewayRequest request = createRequestMock("cognitoAuthenticationType", "cognitoIdentityId", null, null, null,
				null, null, null);
		CognitoIdentityPrincipal principal = (CognitoIdentityPrincipal) createSecurityContextFactory(request)
				.createPrincipal();
		assertEquals("cognitoIdentityId", principal.getName());
		assertEquals("cognitoIdentityId", principal.getCognitoIdentityId());
		assertEquals("cognitoAuthenticationType", principal.getCognitoAuthenticationType());
		assertNull(principal.getCognitoAuthenticationProvider());
		assertNull(principal.getCognitoIdentityPoolId());
		assertNull(principal.getUserArn());
		assertNull(principal.getUser());
		assertNull(principal.getAccessKey());
		assertNull(principal.getCaller());
	}

	@Test
	public void createPrincipal_AllDateGiven_ShouldCreatePrincipal() {
		GatewayRequest request = createRequestMock("cognitoAuthenticationType", "cognitoIdentityId",
				"cognitoIdentityPoolId", "cognitoAuthenticationProvider", "userArn", "user", "accessKey", "caller");
		CognitoIdentityPrincipal principal = (CognitoIdentityPrincipal) createSecurityContextFactory(request)
				.createPrincipal();
		assertEquals("cognitoIdentityId", principal.getName());
		assertEquals("cognitoIdentityId", principal.getCognitoIdentityId());
		assertEquals("cognitoAuthenticationType", principal.getCognitoAuthenticationType());
		assertEquals("cognitoAuthenticationProvider", principal.getCognitoAuthenticationProvider());
		assertEquals("cognitoIdentityPoolId", principal.getCognitoIdentityPoolId());
		assertEquals("userArn", principal.getUserArn());
		assertEquals("user", principal.getUser());
		assertEquals("accessKey", principal.getAccessKey());
		assertEquals("caller", principal.getCaller());
	}

	private static GatewayRequest createRequestMock(String cognitoAuthenticationType, String cognitoIdentityId,
			String cognitoIdentityPoolId, String cognitoAuthenticationProvider, String userArn, String user,
			String accessKey, String caller) {
		GatewayIdentity identity = mock(GatewayIdentity.class);
		when(identity.getCognitoAuthenticationType()).thenReturn(cognitoAuthenticationType);
		when(identity.getCognitoIdentityId()).thenReturn(cognitoIdentityId);
		when(identity.getCognitoIdentityPoolId()).thenReturn(cognitoIdentityPoolId);
		when(identity.getCognitoAuthenticationProvider()).thenReturn(cognitoAuthenticationProvider);
		when(identity.getUserArn()).thenReturn(userArn);
		when(identity.getUser()).thenReturn(user);
		when(identity.getAccessKey()).thenReturn(accessKey);
		when(identity.getCaller()).thenReturn(caller);
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getIdentity()).thenReturn(identity);
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		return request;
	}

	@Override
	protected AbstractSecurityContextFactory createSecurityContextFactory(GatewayRequest request) {
		return new CognitoIdentitySecurityContextFactory(request);
	}

	@Override
	protected String getAuthenticationScheme() {
		return AwsAuthenticationSchemes.AWS_COGNITO_IDENTITY;
	}

	@Override
	protected GatewayRequest createInapplicableInvlidRequest() {
		return mock(GatewayRequest.class);
	}

	@Override
	protected GatewayRequest createApplicableInvalidRequest() {
		return createRequestMock("cognitoAuthenticationType", null, null, null, null, null, null, null);
	}

	@Override
	protected GatewayRequest createApplicableValidRequest() {
		return createRequestMock("cognitoAuthenticationType", "cognitoIdentityId", null, null, null, null, null, null);
	}

}
