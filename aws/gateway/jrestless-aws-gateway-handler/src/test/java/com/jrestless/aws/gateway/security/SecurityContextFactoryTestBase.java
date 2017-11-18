package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.SecurityContext;

import org.junit.jupiter.api.Test;

import com.jrestless.aws.gateway.io.GatewayRequest;

public abstract class SecurityContextFactoryTestBase {

	@Test
	public void init_NullRequestGiven_ShouldFailWithNpe() {
		assertThrows(NullPointerException.class, () -> createSecurityContextFactory(null));
	}

	@Test
	public void testGetAuthenticationScheme() {
		assertEquals(getAuthenticationScheme(),
				createSecurityContextFactory(mock(GatewayRequest.class)).getAuthenticationScheme());
	}

	@Test
	public void createSecurityContext_InApplicableRequestGiven_ShouldFail() {
		assertThrows(IllegalStateException.class, () -> createSecurityContextFactory(createInapplicableInvlidRequest()).createSecurityContext());
	}

	@Test
	public void createSecurityContext_InApplicableInvlidRequestGiven_ShouldCreateAnonSecurityContext() {
		SecurityContext sc = createSecurityContextFactory(createApplicableInvalidRequest()).createSecurityContext();
		assertNull(sc.getAuthenticationScheme());
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void createSecurityContext_InApplicableValidRequestGiven_ShouldSecurityContext() {
		SecurityContext sc = createSecurityContextFactory(createApplicableValidRequest()).createSecurityContext();
		assertEquals(getAuthenticationScheme(), sc.getAuthenticationScheme());
		assertNotNull(sc.getUserPrincipal());
	}

	protected abstract AbstractSecurityContextFactory createSecurityContextFactory(GatewayRequest request);

	protected abstract String getAuthenticationScheme();

	protected abstract GatewayRequest createInapplicableInvlidRequest();

	protected abstract GatewayRequest createApplicableInvalidRequest();

	protected abstract GatewayRequest createApplicableValidRequest();
}
