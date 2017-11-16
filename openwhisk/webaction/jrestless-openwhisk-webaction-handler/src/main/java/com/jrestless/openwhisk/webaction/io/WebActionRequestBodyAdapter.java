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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Special request body deserializer.
 * <p>
 * A deserializer is required because for GET request the body is given as an
 * empty object instead of null or undefined. Note: when a body is passed in a
 * POST or PUT request it's always a string, when the Web Action is handle raw
 * http.
 * <p>
 * The serializer is required for testing, only.
 *
 * @author Bjoern Bilger
 *
 */
class WebActionRequestBodyAdapter implements JsonDeserializer<String>, JsonSerializer<String> {

	@Override
	public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		if (json.isJsonNull()) {
			return null;
		} else if (json.isJsonPrimitive()) {
			return json.getAsString();
		} else if (json.isJsonObject() && json.getAsJsonObject().size() == 0) {
			/*
			 * for some reason OpenWhisk passes an empty object on GET requests
			 *  if the Web Action is defined as "raw", even though it should pass a string!
			 * => handle this case by returning null
			 */
			return null;
		}
		throw new IllegalStateException(
				"expected a string, null, or an empty object as request body but got" + this.getClass());
	}

	@Override
	public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src);
	}

}
