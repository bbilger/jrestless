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
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

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

	public static final String INTEGRATION_RESPONSE_PREFIX = "integration.response.body";
	private static final String INTEGRATION_DEFAULT_RESPONSE_HEADER_PREFIX = INTEGRATION_RESPONSE_PREFIX + ".headers";
	private static final String INTEGRATION_RESPONSE_NONDEFAULT_HEADER_CHAIN_PART = ".cause";
	private static final String INTEGRATION_RESPONSE_NONDEFAULT_HEADER_VALUE_PART = ".errorMessage";

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

	/**
	 * Returns the expression to access headers either in the default or
	 * non-default integration response.
	 *
	 * @see {@link #getDefaultDynamicHeaderIntegrationExpression(String)}
	 * @see {@link #getNonDefaultDynamicHeaderIntegrationExpression(int)}
	 *
	 * @param headerName
	 * @param defaultResponse
	 * @param supportedNonDefaultHeadersInOrder
	 * @return
	 */
	public static String getDynamicHeaderIntegrationExpression(@Nonnull String headerName, boolean defaultResponse,
			@Nullable String[] supportedNonDefaultHeadersInOrder) {
		requireNonNull(headerName);
		if (defaultResponse) {
			return getDefaultDynamicHeaderIntegrationExpression(headerName);
		} else if (supportedNonDefaultHeadersInOrder != null) {
			for (int i = 0; i < supportedNonDefaultHeadersInOrder.length; i++) {
				if (headerName.equals(supportedNonDefaultHeadersInOrder[i])) {
					return getNonDefaultDynamicHeaderIntegrationExpression(i);
				}
			}
		}
		throw new DynamicNonDefaultHeaderNotSupportedException(headerName);
	}

	/**
	 * Returns the expression to access headers in the default integration
	 * response.
	 *
	 * @param headerName
	 * @return
	 */
	public static String getDefaultDynamicHeaderIntegrationExpression(@Nonnull String headerName) {
		requireNonNull(headerName);
		return INTEGRATION_DEFAULT_RESPONSE_HEADER_PREFIX + "." + headerName;
	}

	/**
	 * Returns the expression to access headers in the non-default integration
	 * response. Non-default means we throw an exception from lambda in
	 * order to pass 400s, for example. The outermost exception will contain
	 * nested exceptions, the so called header exceptions, that contain the
	 * actual header values in the errorMessage field.
	 * <p>
	 * The expression for the
	 * <ol>
	 * <li>1st header is: <i>integration.response.body.cause.errorMessage</i>
	 * <li>2nd header is: <i>integration.response.body.cause.cause.errorMessage</i>
	 * </ol>
	 *
	 * @param index
	 *            the index of header in the header exception chain
	 * @return
	 */
	public static String getNonDefaultDynamicHeaderIntegrationExpression(int index) {
		Preconditions.checkArgument(index >= 0, "require index >= 0");
		StringBuilder builder = new StringBuilder();
		builder.append(INTEGRATION_RESPONSE_PREFIX);
		for (int i = 0; i <= index; i++) {
			builder.append(INTEGRATION_RESPONSE_NONDEFAULT_HEADER_CHAIN_PART);
		}
		builder.append(INTEGRATION_RESPONSE_NONDEFAULT_HEADER_VALUE_PART);
		return builder.toString();
	}

	/**
	 * Checks if the header value starts with <i>integration.response.body</i>.
	 * @param headerValue
	 * @return
	 */
	public static boolean isDynamicValue(String headerValue) {
		return headerValue.startsWith(INTEGRATION_RESPONSE_PREFIX);
	}

	public static final class DynamicNonDefaultHeaderNotSupportedException extends RuntimeException {

		private static final long serialVersionUID = -5459545861887344933L;

		private final String headerName;

		private DynamicNonDefaultHeaderNotSupportedException(String headerName) {
			super("dynamic non-default header '" + headerName + "' is not supported");
			this.headerName = headerName;
		}

		public String getHeaderName() {
			return headerName;
		}

	}
}
