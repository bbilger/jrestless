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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WebActionHttpResponseBuilder {

	private static final Gson GSON = new GsonBuilder().create();

	private int statusCode = Status.OK.getStatusCode();
	private Map<String, String> headers = new HashMap<>();
	private String body = "";

	public WebActionHttpResponseBuilder setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public WebActionHttpResponseBuilder setStatusType(StatusType statusType) {
		return setStatusCode(statusType.getStatusCode());
	}

	public WebActionHttpResponseBuilder setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public WebActionHttpResponseBuilder addHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public WebActionHttpResponseBuilder setContentType(String contentType) {
		return addHeader(HttpHeaders.CONTENT_TYPE, contentType);
	}

	public WebActionHttpResponseBuilder setContentType(MediaType contentType) {
		return addHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());
	}

	public WebActionHttpResponseBuilder addHeaders(Map<String, String> headers) {
		this.headers.putAll(headers);
		return this;
	}

	public WebActionHttpResponseBuilder setBody(String body) {
		this.body = body;
		return this;
	}

	public WebActionHttpResponseBuilder setBodyBase64Encoded(String body) {
		return setBody(new String(Base64.getEncoder().encode(body.getBytes()), StandardCharsets.UTF_8));
	}

	public JsonObject build() {
		JsonObject response = new JsonObject();
		response.addProperty("body", body);
		response.addProperty("statusCode", statusCode);
		if (headers != null) {
			response.add("headers", GSON.toJsonTree(headers, Map.class));
		}
		return response;
	}

	public static JsonObject noContent() {
		return new WebActionHttpResponseBuilder().setStatusType(Status.NO_CONTENT).build();
	}

}
