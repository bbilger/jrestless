package com.jrestless.aws.gateway.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.ws.rs.core.SecurityContext;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;

public class CustomAuthorizerFilterTest extends AuthorizerFilterTest {

	@Override
	AuthorizerFilter createCognitoAuthorizerFilter(GatewayRequest gatewayRequest) {
		CustomAuthorizerFilter filter = new CustomAuthorizerFilter();
		filter.setGatewayRequest(gatewayRequest);
		return filter;
	}

	@Test
	public void nullAuthorizerDateGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet((Map<String, Object>) null);
	}

	@Test
	public void emptyAuthorizerDateGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet(Collections.emptyMap());
	}

	@Test
	public void noPrincipalIdGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet(Collections.singletonMap("anotherClaim", "someValue"));
	}

	@Test
	public void emptyPrincipalIdGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet(Collections.singletonMap("principalId", ""));
	}

	@Test
	public void invalidTypePrincipalIdGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet(Collections.singletonMap("principalId", new Object()));
	}

	@Test
	public void blankPrincipalIdGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet(Collections.singletonMap("principalId", " "));
	}

	@Test
	public void principalIdGiven_ShouldSetSecurityContext() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertNotNull(sc);
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextThatIsSecure() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertTrue(sc.isSecure());
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextWithUserCognitoPoolAuthorizerAuthenticationScheme() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertEquals("custom_authorizer", sc.getAuthenticationScheme());
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextWithUserNeverInAnyRole() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertFalse(sc.isUserInRole(null));
		assertFalse(sc.isUserInRole(""));
		assertFalse(sc.isUserInRole("user"));
		assertFalse(sc.isUserInRole("USER"));
	}

	@Test
	public void principalIdGiven_ShouldSetCognitoCustomAuthorizerPrincipalSecurityContext() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertTrue(sc.getUserPrincipal() instanceof CustomAuthorizerPrincipal);
	}

	@Test
	public void principalIdGiven_ShouldSetPrincipalNameToPrincipalId() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		assertEquals("123", sc.getUserPrincipal().getName());
	}

	@Test
	public void principalIdGiven_ShouldMakePrincipalIdAvailableThroughClaims() {
		SecurityContext sc = filterAndReturnSetSecurityContext(Collections.singletonMap("principalId", "123"));
		CustomAuthorizerPrincipal principal = ((CustomAuthorizerPrincipal) sc.getUserPrincipal());
		assertEquals("123", principal.getClaims().getAllClaims().get("principalId"));
	}

	@Test
	public void principalIdAndAdditionalClaimGiven_ShouldMakeClaimAvailable() {
		SecurityContext sc = filterAndReturnSetSecurityContext(ImmutableMap.of("someClaimKey", true, "principalId", "123"));
		CustomAuthorizerPrincipal principal = ((CustomAuthorizerPrincipal) sc.getUserPrincipal());
		assertTrue((Boolean) principal.getClaims().getAllClaims().get("someClaimKey"));
	}

}
