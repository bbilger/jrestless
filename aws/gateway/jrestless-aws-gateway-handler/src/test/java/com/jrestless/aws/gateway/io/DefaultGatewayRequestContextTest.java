package com.jrestless.aws.gateway.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultGatewayRequestContextTest {

	@Test
	public void testEquals() {
		DefaultGatewayIdentity identity = new DefaultGatewayIdentity();
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "accountId")
			.addArguments(1, null, "resourceId")
			.addArguments(2, null, "stage")
			.addArguments(3, null, "requestId")
			.addArguments(4, null, identity)
			.addArguments(5, null, "resourcePath")
			.addArguments(6, null, "httpMethod")
			.addArguments(7, null, "apiId")
			.addArguments(8, null, Collections.singletonMap("123", "123"))
			.testEquals();
	}

	@Test
	public void testGetters() {
		DefaultGatewayIdentity identity = new DefaultGatewayIdentity();
		Map<String, Object> authorizer = ImmutableMap.of("key", "value");
		DefaultGatewayRequestContext requestContext = new DefaultGatewayRequestContext("accountId", "resourceId", "stage",
				"requestId", identity, "resourcePath", "httpMethod", "apiId", authorizer);

		assertEquals("accountId", requestContext.getAccountId());
		assertEquals("resourceId", requestContext.getResourceId());
		assertEquals("stage", requestContext.getStage());
		assertEquals("requestId", requestContext.getRequestId());
		assertSame(identity, requestContext.getIdentity());
		assertEquals("resourcePath", requestContext.getResourcePath());
		assertEquals("httpMethod", requestContext.getHttpMethod());
		assertEquals("apiId", requestContext.getApiId());
		assertEquals(authorizer, requestContext.getAuthorizer());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testAuthorizerImmutability() {
		Map<String, Object> authorizerInput = createAuthorizerTestMap();
		DefaultGatewayRequestContext context = new DefaultGatewayRequestContext();
		context.setAuthorizer(authorizerInput);
		Map<String, Object> authorizerOutput = context.getAuthorizer();
		assertMapUnmodifiable(authorizerOutput);
		assertMapUnmodifiable(((Map)authorizerOutput.get("map")));
		assertMapUnmodifiable(((Map)((Map)authorizerOutput.get("map")).get("map")));
	}

	@Test
	public void testAuthorizerMapsNullToEmpty() {
		DefaultGatewayRequestContext context = new DefaultGatewayRequestContext();
		context.setAuthorizer(null);
		assertNotNull(context.getAuthorizer());
		assertTrue(context.getAuthorizer().isEmpty());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void assertMapUnmodifiable(Map map) {
		try {
			map.put("123", "123");
			fail("map must be unmodifiable");
		} catch (UnsupportedOperationException uoe) {
		} catch (Exception e) {
			fail("expected UnsupportedOperationException");
		}
	}

	@Test
	public void testAuthorizerEquality() {
		DefaultGatewayRequestContext context = new DefaultGatewayRequestContext();
		context.setAuthorizer(createAuthorizerTestMap());
		assertEquals(createAuthorizerTestMap(), context.getAuthorizer());
	}

	private static Map<String, Object> createAuthorizerTestMap() {
		Map<String, Object> authorizer = new HashMap<>();
		authorizer.put("string", "123");
		Map<Object, Object> map = new HashMap<>();
		map.put("string", "321");
		map.put("map", new HashMap<>());
		authorizer.put("map", map);
		return authorizer;
	}

	private Constructor<DefaultGatewayRequestContext> getConstructor() {
		try {
			return DefaultGatewayRequestContext.class.getDeclaredConstructor(String.class, String.class, String.class,
					String.class, DefaultGatewayIdentity.class, String.class, String.class, String.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
