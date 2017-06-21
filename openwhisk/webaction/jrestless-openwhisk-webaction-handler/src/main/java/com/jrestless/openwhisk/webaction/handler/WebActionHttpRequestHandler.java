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
package com.jrestless.openwhisk.webaction.handler;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * OpenWhisk Web Action request handler suitable for "http response types".
 * <p>
 * Notes:
 * <ol>
 * <li>The request handler depends on
 * {@link com.jrestless.openwhisk.webaction.io.WebActionBase64WriteInterceptor
 * WebActionBase64WriteInterceptor} which must be registered on the
 * {@link org.glassfish.jersey.server.ResourceConfig ResourceConfig} - for
 * example using {@link com.jrestless.openwhisk.webaction.WebActionHttpConfig
 * WebActionHttpConfig}
 * </ol>
 * <p>
 * JSON schema of the response:
 *
 * <pre>
 * {
 *   "type": "object",
 *   "properties": {
 *     "headers": {
 *       "type": "object",
 *       "additionalProperties": {
 *         "type": "string"
 *       }
 *     },
 *     "body": {
 *       "type": "string"
 *     },
 *     "satusCode": {
 *       "type": "integer"
 *     }
 *   }
 * }
 * </pre>
 *
 * @author Bjoern Bilger
 *
 */
public class WebActionHttpRequestHandler extends WebActionRequestHandler {

	private static final Gson GSON = new GsonBuilder().create();

	@Override
	protected JsonObject createJsonResponse(@Nullable String body, @Nonnull Map<String, String> responseHeaders,
			@Nonnull StatusType statusType) {
		requireNonNull(responseHeaders);
		requireNonNull(statusType);
		JsonObject response = new JsonObject();
		if (body != null) {
			response.addProperty("body", body);
		}
		response.addProperty("statusCode", statusType.getStatusCode());
		response.add("headers", GSON.toJsonTree(responseHeaders, Map.class));
		return response;
	}
}
