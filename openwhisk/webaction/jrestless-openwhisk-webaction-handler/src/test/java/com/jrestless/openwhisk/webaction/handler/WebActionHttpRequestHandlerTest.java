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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WebActionHttpRequestHandlerTest {

	private static final Gson GSON = new GsonBuilder().create();

	private WebActionHttpRequestHandler handler = new WebActionHttpRequestHandler();

	@Test
	public void createJsonResponse_NullHeadersGiven_ShouldFailNpe() {
		assertThrows(NullPointerException.class, () -> handler.createJsonResponse(null, null, Status.OK));
	}

	@Test
	public void createJsonResponse_NullStatusTypeGiven_ShouldFailNpe() {
		assertThrows(NullPointerException.class, () -> handler.createJsonResponse(null, Collections.emptyMap(), null));
	}

	@Test
	public void createJsonResponse_NoBodyGiven_ShouldCreateResponseWithoutBody() {
		final Map<String, String> headers = Collections.singletonMap("headerName", "headerValue");
		testResponse(null, headers, Status.NOT_FOUND);
	}

	@Test
	public void createJsonResponse_BodyGiven_ShouldCreateResponseWithBody() {
		final Map<String, String> headers = Collections.singletonMap("headerName", "headerValue");
		testResponse("someBody", headers, Status.NOT_FOUND);
	}

	@Test
	public void createJsonResponse_EmptyHeadersGiven_ShouldCreateResponseWithEmptyHeader() {
		testResponse("someBody", Collections.emptyMap(), Status.NOT_FOUND);
	}

	@Test
	public void createJsonResponse_SingleHeaderGiven_ShouldCreateResponseWithSingleHeader() {
		final Map<String, String> headers = Collections.singletonMap("header", "value");
		testResponse(null, headers, Status.OK);
	}

	@Test
	public void createJsonResponse_MultipleHeadersGiven_ShouldCreateResponseWithAllHeaders() {
		final Map<String, String> headers = new HashMap<>();
		headers.put("header1", "value1");
		headers.put("header2", "value2");
		testResponse(null, headers, Status.OK);
	}

	private void testResponse(String body, Map<String, String> headers, StatusType status) {
		JsonObject actualResponse = handler.createJsonResponse(body, headers, status);
		JsonObject expectedResponse = createResponse(body, headers, status);
		assertEquals(expectedResponse, actualResponse);
	}

	private static JsonObject createResponse(String body, Map<String, String> headers, StatusType status) {
		return createResponse(body, headers, status.getStatusCode());
	}

	private static JsonObject createResponse(String body, Map<String, String> headers, int statusCode) {
		JsonObject response = new JsonObject();
		if (body != null) {
			response.addProperty("body", body);
		}
		response.addProperty("statusCode", statusCode);
		response.add("headers", GSON.toJsonTree(headers, Map.class));
		return response;
	}
}
