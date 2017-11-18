/*
 * Copyright 2017 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.openwhisk.webaction.io;

import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_BODY_NAME;
import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_HEADERS_NAME;
import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_METHOD_NAME;
import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_PATH_NAME;
import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_QUERY_NAME;
import static com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest.SERIALIZED_USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultWebActionRequestTest {

	private static final Gson GSON = new GsonBuilder().create();

	@Test
	public void deser_PrimitiveBodyGiven_ShouldDeserializeToString() {
		testDeserializesPrimitiveToString(SERIALIZED_BODY_NAME, DefaultWebActionRequest::getBody);
	}

	@Test
	public void deser_EmptyObjectBodyGiven_ShouldDeserializedAsNull() {
		testBodyDeserialization(null, new JsonObject());
	}

	@Test
	public void deser_NonEmptyObjectBodyGiven_ShouldDeserializedAsNull() {
		JsonObject bodyObject = new JsonObject();
		bodyObject.addProperty("key", "value");
		assertThrows(JsonSyntaxException.class, () -> testBodyDeserialization(null, bodyObject));
	}

	@Test
	public void deser_ArrayBodyGiven_ShouldDeserializedAsNull() {
		JsonObject bodyObject = new JsonObject();
		bodyObject.addProperty("key", "value");
		assertThrows(JsonSyntaxException.class, () -> testBodyDeserialization(null, bodyObject));
	}

	@Test
	public void deser_PrimitivePathGiven_ShouldDeserializeToString() {
		testDeserializesPrimitiveToString(SERIALIZED_PATH_NAME, DefaultWebActionRequest::getPath);
	}

	@Test
	public void deser_NonPrimitivePathGiven_DeserializationShouldFail() {
		testDeserializationFailsForNonPrimitiveTypes(SERIALIZED_PATH_NAME);
	}

	@Test
	public void deser_PrimitiveUserGiven_ShouldDeserializeToString() {
		testDeserializesPrimitiveToString(SERIALIZED_USER_NAME, DefaultWebActionRequest::getUser);
	}

	@Test
	public void deser_NonPrimitiveUserGiven_DeserializationShouldFail() {
		testDeserializationFailsForNonPrimitiveTypes(SERIALIZED_USER_NAME);
	}

	@Test
	public void deser_PrimitiveQueryGiven_ShouldDeserializeToString() {
		testDeserializesPrimitiveToString(SERIALIZED_QUERY_NAME, DefaultWebActionRequest::getQuery);
	}

	@Test
	public void deser_NonPrimitivQueryGiven_DeserializationShouldFail() {
		testDeserializationFailsForNonPrimitiveTypes(SERIALIZED_QUERY_NAME);
	}

	@Test
	public void deser_PrimitiveMethodGiven_ShouldDeserializeToString() {
		testDeserializesPrimitiveToString(SERIALIZED_METHOD_NAME, DefaultWebActionRequest::getMethod);
	}

	@Test
	public void deser_NonPrimitivMethodGiven_DeserializationShouldFail() {
		testDeserializationFailsForNonPrimitiveTypes(SERIALIZED_METHOD_NAME);
	}

	@Test
	public void deser_HeadersUndefinedGiven_ShouldDeserializeToNull() {
		assertNull(GSON.fromJson(new JsonObject(), DefaultWebActionRequest.class).getHeaders());
	}

	@Test
	public void deser_HeadersWithStringKeysAndValuesGiven_ShouldDeserialize() {
		Map<String, String> headers = ImmutableMap.of("key1", "value1", "key2", "value2");
		testHeadersDeserialization(headers, headers);
	}

	@Test
	public void deser_HeadersPrimitiveKeysAndValuesWithGiven_ShouldDeserialize() {
		JsonObject serializedHeaders = new JsonObject();
		serializedHeaders.addProperty("key1", true);
		serializedHeaders.addProperty("key2", 2);
		serializedHeaders.addProperty("key4", "value4");
		Map<String, String> deserializedHeaders = new HashMap<>();
		deserializedHeaders.put("key1", "true");
		deserializedHeaders.put("key2", "2");
		deserializedHeaders.put("key4", "value4");
		testHeadersDeserialization(deserializedHeaders, serializedHeaders);
	}

	@Test
	public void deser_HeadersAsPrimitiveGiven_ShouldFailToDeserialize() {
		try {
			testHeadersDeserialization(new HashMap<>(), 1);
			fail("expected deserialization to fail");
		} catch (JsonSyntaxException jse) {
			; // expected
		}
		try {
			testHeadersDeserialization(new HashMap<>(), true);
			fail("expected deserialization to fail");
		} catch (JsonSyntaxException jse) {
			; // expected
		}
		try {
			testHeadersDeserialization(new HashMap<>(), "some string");
			fail("expected deserialization to fail");
		} catch (JsonSyntaxException jse) {
			; // expected
		}
	}

	@Test
	public void deser_HeadersWithNonPrimitiveValuesGiven_ShouldFailToDeserialize() {
		JsonObject serializedHeaders = new JsonObject();
		serializedHeaders.add("key1", new JsonObject());
		assertThrows(JsonSyntaxException.class, () -> testHeadersDeserialization(new HashMap<>(), serializedHeaders));
	}

	private void testDeserializationFailsForNonPrimitiveTypes(String propertyName) {
		try {
			testDeserialization(propertyName, new JsonObject(), null, null);
			fail("expected deserialization to fail for objects");
		} catch (JsonSyntaxException jse) {
			// expected
		}
		try {
			testDeserialization(propertyName, new JsonArray(), null, null);
			fail("expected deserialization to fail for arrays");
		} catch (JsonSyntaxException jse) {
			// expected
		}
	}

	private void testDeserializesPrimitiveToString(String propertyName, Function<DefaultWebActionRequest, String> getter) {
		testDeserialization(propertyName, true, "true", getter);
		testDeserialization(propertyName, 1, "1", getter);
		testDeserialization(propertyName, "some string", "some string", getter);
		testDeserialization(propertyName, null, null, getter);
		assertNull(getter.apply(GSON.fromJson(new JsonObject(), DefaultWebActionRequest.class)));
	}

	private void testHeadersDeserialization(Map<String, String> expectedDeserializedHeaders, Object serializedHeaders) {
		testDeserialization(SERIALIZED_HEADERS_NAME, serializedHeaders, expectedDeserializedHeaders, DefaultWebActionRequest::getHeaders);
	}

	private void testBodyDeserialization(String expectedDeserializedValue, Object serializedValue) {
		testDeserialization(SERIALIZED_BODY_NAME, serializedValue, expectedDeserializedValue, DefaultWebActionRequest::getBody);
	}

	private <T> void testDeserialization(String propertyName, Object propertyValue, T expectedValue, Function<DefaultWebActionRequest, T> getter) {
		JsonObject serializedRequest = new JsonObject();
		serializedRequest.add(propertyName, GSON.toJsonTree(propertyValue));
		assertEquals(expectedValue, getter.apply(GSON.fromJson(serializedRequest, DefaultWebActionRequest.class)));
	}

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "httpMethod")
			.addArguments(1, null, ImmutableMap.of("headers", "headers"))
			.addArguments(2, null, "path")
			.addArguments(3, null, "user")
			.addArguments(4, null, "body")
			.addArguments(5, null, "query")
			.testEquals();
	}

	@Test
	public void testGetters() {
		Map<String, String> headers = ImmutableMap.of("headers", "headers");
		DefaultWebActionRequest request = new DefaultWebActionRequest(
				"httpMethod",
				headers,
				"path",
				"user",
				"body",
				"query");

		assertEquals("httpMethod", request.getMethod());
		assertEquals(headers, request.getHeaders());
		assertEquals("path", request.getPath());
		assertEquals("user", request.getUser());
		assertEquals("body", request.getBody());
		assertEquals("query", request.getQuery());
	}

	@Test
	public void testNoArgsConsutructorCreatesEmptyObject() {
		DefaultWebActionRequest request = new DefaultWebActionRequest();
		assertNull(request.getMethod());
		assertNull(request.getHeaders());
		assertNull(request.getPath());
		assertNull(request.getUser());
		assertNull(request.getBody());
		assertNull(request.getQuery());

	}

	private Constructor<DefaultWebActionRequest> getConstructor() {
		try {
			return DefaultWebActionRequest.class.getDeclaredConstructor(String.class, Map.class, String.class,
					String.class, String.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
