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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WebActionRequestBuilder {

	private static final Gson GSON = new GsonBuilder().create();

	private String httpMethod;
	private Map<String, String> headers = new HashMap<>();
	private String path;
	private String user;
	private String body;
	private String query;

	public WebActionRequestBuilder setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
		return this;
	}

	public WebActionRequestBuilder setHeaders(Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public WebActionRequestBuilder addHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public WebActionRequestBuilder setContentType(String contentType) {
		return addHeader(HttpHeaders.CONTENT_TYPE, contentType);
	}

	public WebActionRequestBuilder setContentType(MediaType contentType) {
		return addHeader(HttpHeaders.CONTENT_TYPE, contentType.getType());
	}

	public WebActionRequestBuilder addAllHeaders(Map<String, String> headers) {
		headers.putAll(headers);
		return this;
	}

	public WebActionRequestBuilder setPath(String path) {
		this.path = path;
		return this;
	}

	public WebActionRequestBuilder setUser(String user) {
		this.user = user;
		return this;
	}

	public WebActionRequestBuilder setRawBody(String body) {
		this.body = body;
		return this;
	}

	public WebActionRequestBuilder setBodyBase64Encoded(String body) {
		return setRawBody(new String(Base64.getEncoder().encode(body.getBytes()), StandardCharsets.UTF_8));
	}

	public WebActionRequestBuilder setQuery(String query) {
		this.query = query;
		return this;
	}

	public DefaultWebActionRequest build() {
		return new DefaultWebActionRequest(httpMethod, headers, path, user, body, query);
	}

	public JsonObject buildJson() {
		return (JsonObject) GSON.toJsonTree(build(), DefaultWebActionRequest.class);
	}
}
