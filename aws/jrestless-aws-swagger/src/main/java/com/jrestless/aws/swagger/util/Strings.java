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

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for working with {@link String strings}.
 *
 * @author Bjoern Bilger
 *
 */
public final class Strings {

	private Strings() {
		// no instance
	}

	/**
	 * Checks that the passed string is not blank.
	 *
	 * @param value
	 * @return
	 * 		the passed value if it's not blank
	 * @throws IllegalArgumentException
	 * 		if the passed value is blank
	 */
	public static String requireNonBlank(String value) {
		if (StringUtils.isBlank(value)) {
			throw new IllegalArgumentException("value may not be blank");
		}
		return value;
	}
}
