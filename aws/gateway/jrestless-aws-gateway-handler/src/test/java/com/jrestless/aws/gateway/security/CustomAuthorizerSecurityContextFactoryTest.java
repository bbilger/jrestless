package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;

public class CustomAuthorizerSecurityContextFactoryTest extends SecurityContextFactoryTestBase {

	@Test
	public void isApplicable_NoContextGiven_ShouldNotBeApplicable() {
		GatewayRequest request = mock(GatewayRequest.class);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_NoAuthorizerDateGiven_ShouldNotBeApplicable() {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(null);
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_ValidPrincipalIdGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock("principalId");
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_InvalidPrincipalIdGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock(1);
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isValid_ValidPrincipalIdGiven_ShouldBeValid() {
		GatewayRequest request = createRequestMock("principalId");
		assertTrue(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_InvalidPrincipalIdGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock(1);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void createPrincipal_PrincipalIdGiven_ShouldCreatePrincipal() {
		GatewayRequest request = createRequestMock("principalId");
		CustomAuthorizerPrincipal principal = (CustomAuthorizerPrincipal) createSecurityContextFactory(request)
				.createPrincipal();
		assertEquals("principalId", principal.getName());
		assertEquals("principalId", principal.getClaims().getPrincipalId());
		assertEquals("principalId", principal.getClaims().getAllClaims().get("principalId"));
	}

	@Test
	public void createPrincipal_PrincipalIdWithAdditionalClaimsGiven_ShouldCreatePrincipal() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("principalId", "principalId");
		claims.put("someOtherClaim", "someOtherClaimValue");
		GatewayRequest request = createRequestMock(claims);
		CustomAuthorizerPrincipal principal = (CustomAuthorizerPrincipal) createSecurityContextFactory(request)
				.createPrincipal();
		assertEquals("principalId", principal.getName());
		assertEquals("principalId", principal.getClaims().getPrincipalId());
		assertEquals("principalId", principal.getClaims().getAllClaims().get("principalId"));
		assertEquals("someOtherClaimValue", principal.getClaims().getAllClaims().get("someOtherClaim"));
	}

	private static GatewayRequest createRequestMock(Object principalId) {
		return createRequestMock(Collections.singletonMap("principalId", principalId));
	}

	private static GatewayRequest createRequestMock(Map<String, Object> claims) {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(claims);
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		return request;
	}

	@Override
	protected AbstractSecurityContextFactory createSecurityContextFactory(GatewayRequest request) {
		return new CustomAuthorizerSecurityContextFactory(request);
	}

	@Override
	protected String getAuthenticationScheme() {
		return AwsAuthenticationSchemes.AWS_CUSTOM_AUTHORIZER;
	}

	@Override
	protected GatewayRequest createInapplicableInvlidRequest() {
		return mock(GatewayRequest.class);
	}

	@Override
	protected GatewayRequest createApplicableInvalidRequest() {
		return createRequestMock(1);
	}

	@Override
	protected GatewayRequest createApplicableValidRequest() {
		return createRequestMock("principalId");
	}
}
