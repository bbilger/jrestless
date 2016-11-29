package com.jrestless.aws.gateway.io;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.jrestless.aws.gateway.io.DefaultGatewayIdentity;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultGatewayIdentityTest {

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "cognitoIdentityPoolId")
			.addArguments(1, null, "accountId")
			.addArguments(2, null, "cognitoIdentityId")
			.addArguments(3, null, "caller")
			.addArguments(4, null, "apiKey")
			.addArguments(5, null, "sourceIp")
			.addArguments(6, null, "cognitoAuthenticationType")
			.addArguments(7, null, "cognitoAuthenticationProvider")
			.addArguments(8, null, "userArn")
			.addArguments(9, null, "userAgent")
			.addArguments(10, null, "user")
			.testEquals();
	}

	@Test
	public void testGetters() {
		DefaultGatewayIdentity identity = new DefaultGatewayIdentity("cognitoIdentityPoolId", "accountId", "cognitoIdentityId",
				"caller", "apiKey", "sourceIp", "cognitoAuthenticationType", "cognitoAuthenticationProvider", "userArn",
				"userAgent", "user");

		assertEquals("cognitoIdentityPoolId", identity.getCognitoIdentityPoolId());
		assertEquals("accountId", identity.getAccountId());
		assertEquals("cognitoIdentityId", identity.getCognitoIdentityId());
		assertEquals("caller", identity.getCaller());
		assertEquals("apiKey", identity.getApiKey());
		assertEquals("sourceIp", identity.getSourceIp());
		assertEquals("cognitoAuthenticationType", identity.getCognitoAuthenticationType());
		assertEquals("cognitoAuthenticationProvider", identity.getCognitoAuthenticationProvider());
		assertEquals("userArn", identity.getUserArn());
		assertEquals("userAgent", identity.getUserAgent());
		assertEquals("user", identity.getUser());
	}

	private Constructor<DefaultGatewayIdentity> getConstructor() {
		try {
			return DefaultGatewayIdentity.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
