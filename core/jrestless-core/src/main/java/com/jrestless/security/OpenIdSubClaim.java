package com.jrestless.security;

interface OpenIdSubClaim extends Claims {

	String OPEN_ID_CLAIM_SUB = "sub";

	/**
	 * <dl>
	 * 	<dt>For {@link OpenIdStandardClaims}
	 * 	<dd>Subject - Identifier for the End-User at the Issuer.
	 * <dt>For {@link OpenIdIdTokenClaims}
	 * <dd>REQUIRED. Subject Identifier. A locally unique and never reassigned
	 * identifier within the Issuer for the End-User, which is intended to be
	 * consumed by the Client, e.g., 24400320 or
	 * AItOawmwtWwcT0k51BayewNvutrJUqsvl6qs7A4. It MUST NOT exceed 255 ASCII
	 * characters in length. The sub value is a case sensitive string.
	 * </dl>
	 */
	default String getSub() {
		return (String) getClaim(OPEN_ID_CLAIM_SUB);
	}
}
