package com.jrestless.openwhisk.webaction.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class WebActionRequestBodyAdapterTest {

	private WebActionRequestBodyAdapter deserializer = new WebActionRequestBodyAdapter();

	@Test
	public void deser_NullGiven_ShouldReturnNull() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(true);
		assertNull(deserializer.deserialize(json, null, null));
	}

	@Test
	public void deser_PrimitiveGiven_ShouldReturnAsString() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(false);
		when(json.isJsonPrimitive()).thenReturn(true);
		when(json.getAsString()).thenReturn("whatever");
		assertEquals("whatever", deserializer.deserialize(json, null, null));
	}

	@Test
	public void deser_EmptyObjectGiven_ShouldReturnNull() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(false);
		when(json.isJsonPrimitive()).thenReturn(false);
		when(json.isJsonObject()).thenReturn(true);
		when(json.getAsJsonObject()).thenReturn(new JsonObject());
		assertNull(deserializer.deserialize(json, null, null));
	}

	@Test(expected = IllegalStateException.class)
	public void deser_NonEmptyObjectGiven_ShouldFail() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(false);
		when(json.isJsonPrimitive()).thenReturn(false);
		when(json.isJsonObject()).thenReturn(true);
		JsonObject object = new JsonObject();
		object.addProperty("what", "ever");
		when(json.getAsJsonObject()).thenReturn(object);
		assertNull(deserializer.deserialize(json, null, null));
	}

	@Test(expected = IllegalStateException.class)
	public void deser_UnsupportedTypeGiven_ShouldFail() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(false);
		when(json.isJsonPrimitive()).thenReturn(false);
		when(json.isJsonObject()).thenReturn(false);
		assertNull(deserializer.deserialize(json, null, null));
	}

	// artificial case for code coverage
	@Test(expected = IllegalStateException.class)
	public void deser_NoObjectButObjectEntriesGiven_ShouldFail() {
		JsonElement json = mock(JsonElement.class);
		when(json.isJsonNull()).thenReturn(false);
		when(json.isJsonPrimitive()).thenReturn(false);
		when(json.isJsonObject()).thenReturn(false);
		when(json.getAsJsonObject()).thenReturn(new JsonObject());
		assertNull(deserializer.deserialize(json, null, null));
	}
}
