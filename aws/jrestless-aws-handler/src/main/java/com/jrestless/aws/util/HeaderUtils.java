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
package com.jrestless.aws.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.ext.RuntimeDelegate;

/**
 *
 * @author Bjoern Bilger
 *
 */
public final class HeaderUtils {

	private HeaderUtils() {
	}

	public static Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
		return headers.entrySet().stream()
				.filter(h -> h.getKey() != null)
				.filter(h -> h.getValue() != null)
				.filter(h -> !h.getValue().isEmpty())
				.collect(Collectors.toMap(
						h -> h.getKey(),
						h -> asHeaderString(h.getValue())
				));
	}

	private static String asHeaderString(List<String> headerValues) {
		return org.glassfish.jersey.message.internal.HeaderUtils.asHeaderString(
				headerValues.stream().filter(Objects::nonNull).collect(Collectors.toList()),
				RuntimeDelegate.getInstance());
	}

	public static Map<String, List<String>> expandHeaders(Map<String, String> headers) {
		Function<Map.Entry<String, String>, List<String>> toList = h -> Collections.singletonList(h.getValue());
		return headers.entrySet().stream()
				.filter(h -> h.getKey() != null)
				.filter(h -> h.getValue() != null)
				.collect(Collectors.toMap(h -> h.getKey(), toList));
	}
}
