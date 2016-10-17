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

import java.util.Objects;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

/**
 * Equality check and hash code generation for {@link StatusType}.
 *
 * @author Bjoern Bilger
 *
 */
public final class StatusTypeEquality {

	private StatusTypeEquality() {
		// no instance
	}

	public static boolean isEqual(StatusType s1, StatusType s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null || s2 == null) {
			return false;
		}
		if (s1.getStatusCode() != s2.getStatusCode()) {
			return false;
		}
		if (!Objects.equals(s1.getFamily(), s2.getFamily())) {
			return false;
		}
		if (!Objects.equals(s1.getReasonPhrase(), s2.getReasonPhrase())) {
			return false;
		}
		return true;
	}

	public static int hashCode(StatusType statusType) {
		if (statusType == null) {
			return 0;
		}
		final int prime = 31;
		int result = 0;
		Status.Family statusFamily = statusType.getFamily();
		String reasonPhrase = statusType.getReasonPhrase();
		result = prime * result + statusType.getStatusCode();
		result = prime * result + ((statusFamily == null) ? 0 : statusFamily.hashCode());
		result = prime * result + ((reasonPhrase == null) ? 0 : reasonPhrase.hashCode());
		return result;
	}
}
