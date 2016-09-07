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

import static java.util.Objects.requireNonNull;
import static jersey.repackaged.com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.commons.lang3.StringEscapeUtils;
import org.glassfish.jersey.message.internal.HeaderUtils;

import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayResponseFactoryImpl implements GatewayResponseFactory {

	private final List<String> nonDefaultHeaderOrder;

	public GatewayResponseFactoryImpl() {
		this(ImmutableList.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.LOCATION));
	}

	public GatewayResponseFactoryImpl(List<String> nonDefaultHeaderOrder) {
		checkArgument(nonDefaultHeaderOrder.contains(HttpHeaders.CONTENT_TYPE),
				"the list must contain the 'Content-Type' header");
		this.nonDefaultHeaderOrder = ImmutableList.copyOf(nonDefaultHeaderOrder);
	}

	@Override
	public GatewayDefaultResponse createResponse(@Nullable String body, @Nonnull Map<String, List<String>> headers,
			@Nonnull StatusType statusType, boolean defaultResponse) {
		requireNonNull(headers);
		requireNonNull(statusType);
		Map<String, String> flattenedHeaders = flattenHeaders(headers);
		if (defaultResponse) {
			return createDefaultResponse(body, flattenedHeaders, statusType);
		} else {
			throw createAdditionalResponse(body, flattenedHeaders, statusType);
		}
	}

	protected GatewayAdditionalResponseException createAdditionalResponse(String body, Map<String, String> headers,
			StatusType statusType) {
		return new GatewayAdditionalResponseException(createJsonResponse(body, statusType), createHeaderChain(headers));
	}

	protected GatewayNonDefaultHeaderException createHeaderChain(Map<String, String> headers) {
		ListIterator<String> lit = nonDefaultHeaderOrder.listIterator(nonDefaultHeaderOrder.size());
		GatewayNonDefaultHeaderException header = null;
		while (lit.hasPrevious()) {
			String consideredHeader = lit.previous();
			String headerValue = headers.get(consideredHeader);
			headerValue = headerValue == null ? "" : headerValue;
			header = new GatewayNonDefaultHeaderException(headerValue, header);
		}
		return header;
	}

	/**
	 * Returns the response as JSON.
	 * <pre>
	 * {
	 *   "statusCode": "STATUSCODE",
	 *   "body": "BODY" //escaped!
	 * }
	 * </pre>
	 *
	 * @return
	 */
	protected String createJsonResponse(String body, StatusType statusType) {
		String escpapedBody = StringEscapeUtils.escapeJson(body);
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("{\"statusCode\":\"");
		strBuilder.append(statusType.getStatusCode());
		if (escpapedBody != null) {
			strBuilder.append("\",\"body\":\"");
			strBuilder.append(escpapedBody);
		}
		strBuilder.append("\"}");
		return strBuilder.toString();
	}

	protected GatewayDefaultResponse createDefaultResponse(String body, Map<String, String> headers,
			StatusType statusType) {
		return new GatewayDefaultResponse(body, headers, statusType);
	}

	protected Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
		return headers.entrySet().stream()
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(
						e -> e.getKey(),
						e -> asHeaderString(e.getValue())
				));
	}

	protected String asHeaderString(List<String> headerValues) {
		return HeaderUtils.asHeaderString(headerValues.stream().filter(Objects::nonNull).collect(Collectors.toList()),
				RuntimeDelegate.getInstance());
	}

}
