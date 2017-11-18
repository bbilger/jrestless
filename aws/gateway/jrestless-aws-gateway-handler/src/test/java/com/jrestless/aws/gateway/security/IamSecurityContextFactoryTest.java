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
import com.jrestless.aws.security.IamPrincipal;

public class IamSecurityContextFactoryTest extends SecurityContextFactoryTestBase {
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
	public void isApplicable_NoAccessKeyGiven_ShouldNotBeApplicable() {
		GatewayRequest request = createRequestMock(null, null, null, null);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_AccessKeyGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock("accessKey", null, null, null);
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isValid_NoUserArnAndUserAndCallerGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("accessKey", null, null, null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_NoUserArnAndCallerGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("accessKey", null, "user", null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_NoUserAndCallerGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("accessKey", "userArn", null, null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_NoUserAndUserrGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("accessKey", null, null, "caller");
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_UserArnAndUserAndCallerGiven_ShouldBeValid() {
		GatewayRequest request = createRequestMock("accessKey", "userArn", "user", "caller");
		assertTrue(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void createPrincipal_AccessKeyAndUserArnAndUserAndCallerGiven_ShouldCreateIamPrincipal() {
		GatewayRequest request = createRequestMock("accessKey", "userArn", "user", "caller");
		IamPrincipal principal = (IamPrincipal) createSecurityContextFactory(request).createPrincipal();
		assertEquals("accessKey", principal.getAccessKey());
		assertEquals("userArn", principal.getUserArn());
		assertEquals("user", principal.getUser());
		assertEquals("caller", principal.getCaller());
		assertEquals(principal.getUserArn(), principal.getName());
	}

	@Test
	public void createPrincipal_OnlyAccessKeyGiven_ShouldCreateIamPrincipal() {
		GatewayRequest request = createRequestMock("accessKey", null, null, null);
		IamPrincipal principal = (IamPrincipal) createSecurityContextFactory(request).createPrincipal();
		assertEquals("accessKey", principal.getAccessKey());
		assertNull(principal.getUserArn());
		assertNull(principal.getUser());
		assertNull(principal.getCaller());
		assertEquals(principal.getUserArn(), principal.getName());
	}

	private static GatewayRequest createRequestMock(String accessKey, String userArn, String user, String caller) {
		GatewayIdentity identity = mock(GatewayIdentity.class);
		when(identity.getAccessKey()).thenReturn(accessKey);
		when(identity.getUserArn()).thenReturn(userArn);
		when(identity.getUser()).thenReturn(user);
		when(identity.getCaller()).thenReturn(caller);
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getIdentity()).thenReturn(identity);
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		return request;
	}

	@Override
	protected AbstractSecurityContextFactory createSecurityContextFactory(GatewayRequest request) {
		return new IamSecurityContextFactory(request);
	}

	@Override
	protected String getAuthenticationScheme() {
		return AwsAuthenticationSchemes.AWS_IAM;
	}

	@Override
	protected GatewayRequest createInapplicableInvlidRequest() {
		return mock(GatewayRequest.class);
	}

	@Override
	protected GatewayRequest createApplicableInvalidRequest() {
		return createRequestMock("accessKey", null, null, null);
	}

	@Override
	protected GatewayRequest createApplicableValidRequest() {
		return createRequestMock("accessKey", "userArn", "user", null); // caller is optional
	}
}
