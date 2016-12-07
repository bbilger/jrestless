package com.jrestless.aws.service.io;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class ServiceDto {
	private String body;
	private Map<String, List<String>> headers;

	ServiceDto(Map<String, List<String>> defaultHeaders) {
		headers = defaultHeaders;
	}

	ServiceDto(@Nullable String body, @Nonnull Map<String, List<String>> headers,
			Map<String, List<String>> defaultHeaders) {
		this(defaultHeaders);
		setBody(body);
		setHeaders(headers);
	}

	public String getBody() {
		// we don't copy the value for performance reasons
		return body;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setBody(@Nullable String body) {
		// we don't copy the value for performance reasons
		this.body = body;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setHeaders(@Nonnull Map<String, List<String>> headers) {
		requireNonNull(headers);
		this.headers = headers.entrySet().stream()
				.filter(e -> e.getKey() != null)
				.filter(e -> e.getValue() != null)
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(
								Map.Entry::getKey,
								ServiceDto::copyList),
						Collections::unmodifiableMap));
	}

	private static List<String> copyList(Map.Entry<String, List<String>> header) {
		return Collections.unmodifiableList(new ArrayList<>(header.getValue()));
	}
}
