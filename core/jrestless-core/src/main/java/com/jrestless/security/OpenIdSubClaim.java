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
package com.jrestless.security;

import static com.jrestless.security.OpenIdClaimFieldNames.CLAIM_SUB;

interface OpenIdSubClaim extends Claims {

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
		return (String) getAllClaims().get(CLAIM_SUB);
	}
}
