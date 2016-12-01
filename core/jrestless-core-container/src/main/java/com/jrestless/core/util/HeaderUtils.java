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
package com.jrestless.core.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.ext.RuntimeDelegate;

/**
 * Utility class to convert between headers in the form
 * {@code Map<String, String>} and
 * {@code Map<String, List<String>>} (used by Jersey).
 *
 * @author Bjoern Bilger
 *
 */
public final class HeaderUtils {

	private HeaderUtils() {
	}

	/**
	 * See {@link #flattenHeaders(Map, Predicate)} with no headerNameFilter.
	 */
	public static Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
		return flattenHeaders(headers, headerName -> true);
	}

	/**
	 * Flattens headers.
	 * <ol>
	 * <li>headers having a null key are filtered out
	 * <li>headers having a null value instead of a list object are filtered out
	 * <li>headers having an empty list value are filtered out
	 * <li>headers no passing the headerNameFilter
	 * <li>null header values (within the list) are filtered out
	 * <li>lists of values are merged into a single string (in most cases
	 * separated by a comma)
	 * </ol>
	 *
	 * @param headers
	 * @param headerNameFilter
	 * @return flattened headers (unmodifiable!)
	 */
	public static Map<String, String> flattenHeaders(Map<String, List<String>> headers,
			Predicate<String> headerNameFilter) {
		return headers.entrySet().stream()
				.filter(h -> h.getKey() != null)
				.filter(h -> h.getValue() != null)
				.filter(h -> !h.getValue().isEmpty())
				.filter(h -> headerNameFilter.test(h.getKey()))
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(
								Map.Entry::getKey,
								h -> asHeaderString(h.getValue())),
						Collections::unmodifiableMap));
	}

	private static String asHeaderString(List<String> headerValues) {
		return org.glassfish.jersey.message.internal.HeaderUtils.asHeaderString(
				headerValues.stream().filter(Objects::nonNull).collect(Collectors.toList()),
				RuntimeDelegate.getInstance());
	}

	/**
	 * Expands headers.
	 * <ol>
	 * <li>headers having a null key are filtered out
	 * <li>headers having a null value are filtered out
	 * <li>header values are put into a list
	 * </ol>
	 * @param headers
	 * @return expanded headers (unmodifiable!)
	 */
	public static Map<String, List<String>> expandHeaders(Map<String, String> headers) {
		return headers.entrySet().stream()
				.filter(h -> h.getKey() != null)
				.filter(h -> h.getValue() != null)
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(
								Map.Entry::getKey,
								HeaderUtils::expandHeaders),
						Collections::unmodifiableMap));
	}

	private static List<String> expandHeaders(Map.Entry<String, String> entry) {
		return Collections.singletonList(entry.getValue());
	}
}
