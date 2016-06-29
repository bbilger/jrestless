/*
 * Copyright 2016 Bjoern Bilger
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
package com.jrestless.aws.io;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.io.GatewayRequest.MissingPathParamException;
import com.jrestless.aws.io.GatewayRequest.UnmatchedPathParamException;

public class GatewayRequestTest {

	@Test(expected = IllegalStateException.class)
	public void getHttpMethod_NoContextSet_ShouldThrowIse() {
		GatewayRequest request = new GatewayRequest();
		request.getHttpMethod();
	}

	@Test(expected = IllegalStateException.class)
	public void getHttpMethod_NoHttpMethodSetInContext_ShouldThrowIse() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		request.setContext(context);
		request.getHttpMethod();
	}

	@Test
	public void getHttpMethod_HttpMethodSetInContext_ShouldReturnHttpMethod() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setHttpMethod("something");
		request.setContext(context);
		assertEquals("something", request.getHttpMethod());
	}

	@Test
	public void getEntityStream_NoBodyGiven_ShouldReturnEmptyBais() {
		GatewayRequest request = new GatewayRequest();
		assertThat(request.getEntityStream(), instanceOf(ByteArrayInputStream.class));
		assertEquals("", toString(request.getEntityStream()));
	}

	@Test
	public void getEntityStream_BodyGiven_ShouldBodyInBais() {
		GatewayRequest request = new GatewayRequest();
		request.setBody("someBody");
		assertThat(request.getEntityStream(), instanceOf(ByteArrayInputStream.class));
		assertEquals("someBody", toString(request.getEntityStream()));
	}

	private static String toString(InputStream inputStream) {
		try (Reader reader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()))) {
			StringBuilder builder = new StringBuilder();
			int c = 0;
			while ((c = reader.read()) != -1) {
				builder.append((char) c);
			}
			return builder.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void getRequestUri_NoContextSet_ShouldThrowIse() {
		GatewayRequest request = new GatewayRequest();
		request.getRequestUri();
	}

	@Test(expected = IllegalStateException.class)
	public void getRequestUri_NoResourcePathSetInContext_ShouldThrowIse() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		request.setContext(context);
		request.getRequestUri();
	}

	@Test
	public void getRequestUri_QueryParamsGiven_ShouldAppendQueryParamsToResourcePath() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity");
		request.setContext(context);
		Map<String, String> expectedQueryParams = ImmutableMap.of("key0", "val0", "key1", "val1");
		request.setQueryParams(expectedQueryParams);
		Map<String, String> actualQueryParams = Arrays.asList(request.getRequestUri().getQuery().split("&")).stream()
				.map(s -> s.split("="))
				.collect(Collectors.toMap(s -> s[0], s -> s[1]));
		assertEquals(expectedQueryParams, actualQueryParams);
	}

	@Test
	public void getRequestUri_UnencodedQueryParamsGiven_ShouldAppendEncodedQueryParams() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity");
		request.setContext(context);
		request.setQueryParams(ImmutableMap.of("ö", "some spaced text"));
		assertEquals(URI.create("/entity?%C3%B6=some+spaced+text"), request.getRequestUri());
	}

	@Test
	public void setQueryParams_NullQueryParamsGiven_ShouldResetQueryParams() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity");
		request.setContext(context);
		request.setQueryParams(ImmutableMap.of("key0", "val0"));
		request.setQueryParams(null);
		assertEquals(URI.create("/entity"), request.getRequestUri());
	}

	@Test
	public void getRequestUri_NotAllPathParamsGiven_ShouldThrowMppe() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/{entityId}/subentitiy/{subEntityId}");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of("entityId", "1"));
		try {
			request.getRequestUri();
			fail("expected MissingPathParamException to be thrown");
		} catch (MissingPathParamException mppe) {
			assertEquals("/entity/{entityId}/subentitiy/{subEntityId}", mppe.getTemplateRequestUri());
			assertEquals("subEntityId", mppe.getParam());
		}
	}

	@Test
	public void getRequestUri_TooManyPathParamsGiven_ShouldThrowUppe() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/{entityId}");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of("entityId", "1", "subEntityId", "2"));
		try {
			request.getRequestUri();
			fail("expected UnmatchedPathParamException to be thrown");
		} catch (UnmatchedPathParamException uppe) {
			assertEquals("/entity/{entityId}", uppe.getTemplateRequestUri());
			assertEquals("subEntityId", uppe.getParam());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRequestUri_InvalidResourceTemplateGiven0_ShouldThrowIae() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/{entityId");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of());
		request.getRequestUri();
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRequestUri_InvalidResourceTemplateGiven1_ShouldThrowIae() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/entityId}");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of());
		request.getRequestUri();
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRequestUri_InvalidResourceTemplateGiven2_ShouldThrowIae() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/}entityId{");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of());
		request.getRequestUri();
	}

	@Test(expected = IllegalArgumentException.class)
	public void getRequestUri_InvalidResourceTemplateGiven3_ShouldThrowIae() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/entityId{");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of());
		request.getRequestUri();
	}

	@Test
	public void getRequestUri_PathParamsGiven_ShouldReplacePathParams() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/{entityId}/subentitiy/{subEntityId}");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of("entityId", "1", "subEntityId", "2"));
		assertEquals(URI.create("/entity/1/subentitiy/2"), request.getRequestUri());
	}

	@Test
	public void getRequestUri_PathAndQueryParamsGiven_ShouldReplacePathAndAppendQueryParams() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity/{entityId}");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of("entityId", "1"));
		request.setQueryParams(ImmutableMap.of("key0", "val0"));
		assertEquals(URI.create("/entity/1?key0=val0"), request.getRequestUri());
	}

	@Test
	public void setPathParams_NullPathParamsGiven_ShouldResetPathParams() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity");
		request.setContext(context);
		request.setPathParams(ImmutableMap.of("entityId", "1", "subEntityId", "2"));
		request.setPathParams(null);
		assertEquals(URI.create("/entity"), request.getRequestUri());
	}

	@Test
	public void getHeaders_HeaderParamsGiven_ShouldReturnHeaders() {
		GatewayRequest request = new GatewayRequest();
		request.setHeaderParams(ImmutableMap.of("key0", "val0", "key1", "val1"));
		Map<String, List<String>> actualHeaders = request.getHeaders();
		assertEquals(2, actualHeaders.size());
		assertEquals(Collections.singletonList("val0"), actualHeaders.get("key0"));
		assertEquals(Collections.singletonList("val1"), actualHeaders.get("key1"));
	}

	@Test
	public void setHeaderParams_NullHeaderGiven_ShouldClearHeaders() {
		GatewayRequest request = new GatewayRequest();
		request.setHeaderParams(ImmutableMap.of("key0", "val0", "key1", "val1"));
		request.setHeaderParams(null);
		Map<String, List<String>> actualHeaders = request.getHeaders();
		assertEquals(0, actualHeaders.size());
	}

	@Test
	public void getHeaders_NullHeaderGiven_ShouldReturnNonNullHeadersOnly() {
		GatewayRequest request = new GatewayRequest();
		Map<String, String> headers = new HashMap<>();
		headers.put("key0", "val0");
		headers.put("key1", null);
		headers.put("key2", "val2");
		request.setHeaderParams(headers);
		Map<String, List<String>> actualHeaders = request.getHeaders();
		assertEquals(2, actualHeaders.size());
		assertEquals(Collections.singletonList("val0"), actualHeaders.get("key0"));
		assertEquals(Collections.singletonList("val2"), actualHeaders.get("key2"));
	}

	@Test
	public void getBaseUri_ResourcePathGiven_ShouldAlwaysReturnRoot() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setResourcePath("/entity");
		request.setContext(context);
		assertEquals(URI.create("/"), request.getBaseUri());
	}

	@Test
	public void getBaseUri_NothingGiven_ShouldAlwaysReturnRoot() {
		GatewayRequest request = new GatewayRequest();
		assertEquals(URI.create("/"), request.getBaseUri());
	}

	@Test
	public void getContext_NothingGiven_ShouldBeNull() {
		GatewayRequest request = new GatewayRequest();
		assertNull(request.getContext());
	}

	@Test
	public void getContext_ContextSetGiven_ShouldReturnContext() {
		GatewayRequest request = new GatewayRequest();
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		request.setContext(context);
		assertSame(context, request.getContext());
		request.toString();
	}
}
