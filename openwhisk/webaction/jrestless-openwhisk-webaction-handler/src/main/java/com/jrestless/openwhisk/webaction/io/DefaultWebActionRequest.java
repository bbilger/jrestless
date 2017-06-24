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

import java.util.Map;
import java.util.Objects;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * Implementation of {@link WebActionRequest}.
 * <p>
 * The implementation makes sure that the request object can get de-serialized
 * into this representation (using GSON).
 *
 * @author Bjoern Bilger
 *
 */
public final class DefaultWebActionRequest implements WebActionRequest {

	static final String SERIALIZED_METHOD_NAME = "__ow_method";
	static final String SERIALIZED_HEADERS_NAME = "__ow_headers";
	static final String SERIALIZED_PATH_NAME = "__ow_path";
	static final String SERIALIZED_USER_NAME = "__ow_user";
	static final String SERIALIZED_BODY_NAME = "__ow_body";
	static final String SERIALIZED_QUERY_NAME = "__ow_query";

	@SerializedName(SERIALIZED_METHOD_NAME)
	private String method;
	@SerializedName(SERIALIZED_HEADERS_NAME)
	private Map<String, String> headers;
	@SerializedName(SERIALIZED_PATH_NAME)
	private String path;
	@SerializedName(SERIALIZED_USER_NAME)
	private String user;
	@JsonAdapter(WebActionRequestBodyAdapter.class)
	@SerializedName(SERIALIZED_BODY_NAME)
	private String body;
	@SerializedName(SERIALIZED_QUERY_NAME)
	private String query;

	public DefaultWebActionRequest() {
	}
	// for testing, only
	DefaultWebActionRequest(String method, Map<String, String> headers, String path, String user, String body,
			String query) {
		this.method = method;
		this.headers = headers;
		this.path = path;
		this.user = user;
		this.body = body;
		this.query = query;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		DefaultWebActionRequest castOther = (DefaultWebActionRequest) other;
		return Objects.equals(method, castOther.method) && Objects.equals(headers, castOther.headers)
				&& Objects.equals(path, castOther.path) && Objects.equals(user, castOther.user)
				&& Objects.equals(body, castOther.body) && Objects.equals(query, castOther.query);
	}

	@Override
	public int hashCode() {
		return Objects.hash(method, headers, path, user, body, query);
	}
}
