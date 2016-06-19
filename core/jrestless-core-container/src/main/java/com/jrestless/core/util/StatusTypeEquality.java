package com.jrestless.core.util;

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

	public static boolean equals(StatusType s1, StatusType s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null || s2 == null) {
			return false;
		}
		if (s1.getStatusCode() != s2.getStatusCode()) {
			return false;
		}
		Status.Family statusFamily = s1.getFamily();
		Status.Family otherStatusFamily = s2.getFamily();
		if (statusFamily == null) {
			if (otherStatusFamily != null) {
				return false;
			}
		} else if (!statusFamily.equals(otherStatusFamily)) {
			return false;
		}
		String reasonPhrase = s1.getReasonPhrase();
		String otherReasonPhrase = s2.getReasonPhrase();
		if (reasonPhrase == null) {
			if (otherReasonPhrase != null) {
				return false;
			}
		} else if (!reasonPhrase.equals(otherReasonPhrase)) {
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
