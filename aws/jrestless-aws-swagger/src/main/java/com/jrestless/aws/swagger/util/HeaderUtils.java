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
package com.jrestless.aws.swagger.util;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;

/**
 * Utility class for dealing with HTTP headers - especially in AWS's API
 * Gateway.
 *
 * @author Bjoern Bilger
 *
 */
public final class HeaderUtils {

	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private HeaderUtils() {
		// no instance
	}
	/**
	 * Converts the passed header value into a static value.
	 * <p>
	 * Adds a leading and trailing single quote.
	 *
	 * @param headerValue
	 * @return
	 */
	public static String asStaticValue(@Nonnull String headerValue) {
		requireNonNull(headerValue);
		return "'" + headerValue + "'";
	}

	/**
	 * Checks if the passed header value is static.
	 * <p>
	 * A header value is static if it starts and ends with a single quote.
	 *
	 * @param headerValue
	 * @return
	 */
	public static boolean isStaticValue(String headerValue) {
		if (headerValue == null) {
			return false;
		}
		return headerValue.startsWith("'") && headerValue.endsWith("'");
	}
}
