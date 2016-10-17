package com.jrestless.aws.gateway.io;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.jrestless.aws.gateway.io.GatewayIdentityImpl;
import com.jrestless.aws.gateway.io.GatewayRequestContextImpl;
import com.jrestless.test.CopyConstructorEqualsTester;

public class GatewayRequestContextImplTest {

	@Test
	public void testEquals() {
		GatewayIdentityImpl identity = new GatewayIdentityImpl();
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "accountId")
			.addArguments(1, null, "resourceId")
			.addArguments(2, null, "stage")
			.addArguments(3, null, "requestId")
			.addArguments(4, null, identity)
			.addArguments(5, null, "resourcePath")
			.addArguments(6, null, "httpMethod")
			.addArguments(7, null, "apiId")
			.testEquals();
	}

	@Test
	public void testGetters() {
		GatewayIdentityImpl identity = new GatewayIdentityImpl();
		GatewayRequestContextImpl requestContext = new GatewayRequestContextImpl("accountId", "resourceId", "stage",
				"requestId", identity, "resourcePath", "httpMethod", "apiId");

		assertEquals("accountId", requestContext.getAccountId());
		assertEquals("resourceId", requestContext.getResourceId());
		assertEquals("stage", requestContext.getStage());
		assertEquals("requestId", requestContext.getRequestId());
		assertEquals(identity, requestContext.getIdentity());
		assertEquals("resourcePath", requestContext.getResourcePath());
		assertEquals("httpMethod", requestContext.getHttpMethod());
		assertEquals("apiId", requestContext.getApiId());
	}

	private Constructor<GatewayRequestContextImpl> getConstructor() {
		try {
			return GatewayRequestContextImpl.class.getDeclaredConstructor(String.class, String.class, String.class,
					String.class, GatewayIdentityImpl.class, String.class, String.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
