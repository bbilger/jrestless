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

import java.util.Arrays;
import java.util.Collection;

/**
 * OpenID ID Token claims.
 * <p>
 * See <a href= "http://openid.net/specs/openid-connect-core-1_0.html#IDToken">
 * http://openid.net/specs/openid-connect-core-1_0.html#IDToken</a>
 *
 * @author Bjoern Bilger
 *
 */
public interface OpenIdIdTokenClaims extends Claims, OpenIdSubClaim {

	String OPEN_ID_CLAIM_ID_TOKEN_ISS = "iss";
	String OPEN_ID_CLAIM_ID_TOKEN_SUB = OPEN_ID_CLAIM_SUB;
	String OPEN_ID_CLAIM_ID_TOKEN_AUD = "aud";
	String OPEN_ID_CLAIM_ID_TOKEN_EXP = "exp";
	String OPEN_ID_CLAIM_ID_TOKEN_IAT = "iat";
	String OPEN_ID_CLAIM_ID_TOKEN_AUTH_TIME = "auth_time";
	String OPEN_ID_CLAIM_ID_TOKEN_NONCE = "nonce";
	String OPEN_ID_CLAIM_ID_TOKEN_ACR = "acr";
	String OPEN_ID_CLAIM_ID_TOKEN_AMR = "amr";
	String OPEN_ID_CLAIM_ID_TOKEN_AZP = "azp";

	/**
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	@Override
	default String getSub() {
		String sub = (String) getClaim(OPEN_ID_CLAIM_SUB);
		if (sub == null) {
			throw new NullPointerException("sub not set");
		}
		return sub;
	}

	/**
	 * REQUIRED. Issuer Identifier for the Issuer of the response. The iss value
	 * is a case sensitive URL using the https scheme that contains scheme,
	 * host, and optionally, port number and path components and no query or
	 * fragment components.
	 *
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	default String getIss() {
		String iss = (String) getClaim(OPEN_ID_CLAIM_ID_TOKEN_ISS);
		if (iss == null) {
			throw new NullPointerException("iss not set");
		}
		return iss;
	}

	/**
	 * REQUIRED. Audience(s) that this ID Token is intended for. It MUST contain
	 * the OAuth 2.0 client_id of the Relying Party as an audience value. It MAY
	 * also contain identifiers for other audiences. In the general case, the
	 * aud value is an array of case sensitive strings. In the common special
	 * case when there is one audience, the aud value MAY be a single case
	 * sensitive string.
	 * <p>
	 * Note: if the actual type is a string array, then the value gets
	 * transformed to a collection.
	 *
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	@SuppressWarnings("unchecked")
	default Collection<String> getAud() {
		Object amrObj = getClaim(OPEN_ID_CLAIM_ID_TOKEN_AUD);
		if (amrObj == null) {
			throw new NullPointerException("aud is not set");
		} else if (amrObj instanceof Collection) {
			return (Collection<String>) amrObj;
		} else if (amrObj instanceof String[]) {
			return Arrays.asList((String[]) amrObj);
		} else {
			throw new ClassCastException("aud is of invalid type '" + amrObj.getClass().getName() + "'");
		}
	}

	/**
	 * See {@link #getAud()}.
	 *
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	default String getSingleAud() {
		String aud = (String) getClaim(OPEN_ID_CLAIM_ID_TOKEN_AUD);
		if (aud == null) {
			throw new NullPointerException("aud not set");
		}
		return aud;
	}

	/**
	 * REQUIRED. Expiration time on or after which the ID Token MUST NOT be
	 * accepted for processing. The processing of this parameter requires that
	 * the current date/time MUST be before the expiration date/time listed in
	 * the value. Implementers MAY provide for some small leeway, usually no
	 * more than a few minutes, to account for clock skew. Its value is a JSON
	 * number representing the number of seconds from 1970-01-01T0:0:0Z as
	 * measured in UTC until the date/time. See RFC 3339 [RFC3339] for details
	 * regarding date/times in general and UTC in particular.
	 *
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	default long getExp() {
		return (long) getClaim(OPEN_ID_CLAIM_ID_TOKEN_EXP);
	}

	/**
	 * REQUIRED. Time at which the JWT was issued. Its value is a JSON number
	 * representing the number of seconds from 1970-01-01T0:0:0Z as measured in
	 * UTC until the date/time.
	 *
	 * @throws NullPointerException
	 *             if the value is not set
	 */
	default long getIat() {
		return (long) getClaim(OPEN_ID_CLAIM_ID_TOKEN_IAT);
	}

	/**
	 * Time when the End-User authentication occurred. Its value is a JSON
	 * number representing the number of seconds from 1970-01-01T0:0:0Z as
	 * measured in UTC until the date/time. When a max_age request is made or
	 * when auth_time is requested as an Essential Claim, then this Claim is
	 * REQUIRED; otherwise, its inclusion is OPTIONAL. (The auth_time Claim
	 * semantically corresponds to the OpenID 2.0 PAPE [OpenID.PAPE] auth_time
	 * response parameter.)
	 */
	default Long getAuthTime() {
		return (Long) getClaim(OPEN_ID_CLAIM_ID_TOKEN_AUTH_TIME);
	}

	/**
	 * String value used to associate a Client session with an ID Token, and to
	 * mitigate replay attacks. The value is passed through unmodified from the
	 * Authentication Request to the ID Token. If present in the ID Token,
	 * Clients MUST verify that the nonce Claim Value is equal to the value of
	 * the nonce parameter sent in the Authentication Request. If present in the
	 * Authentication Request, Authorization Servers MUST include a nonce Claim
	 * in the ID Token with the Claim Value being the nonce value sent in the
	 * Authentication Request. Authorization Servers SHOULD perform no other
	 * processing on nonce values used. The nonce value is a case sensitive
	 * string.
	 */
	default String getNonce() {
		return (String) getClaim(OPEN_ID_CLAIM_ID_TOKEN_NONCE);
	}

	/**
	 * OPTIONAL. Authentication Context Class Reference. String specifying an
	 * Authentication Context Class Reference value that identifies the
	 * Authentication Context Class that the authentication performed satisfied.
	 * The value "0" indicates the End-User authentication did not meet the
	 * requirements of <a href=
	 * "http://openid.net/specs/openid-connect-core-1_0.html#ISO29115">ISO/IEC
	 * 29115</a> [ISO29115] level 1. Authentication using a long-lived browser
	 * cookie, for instance, is one example where the use of "level 0" is
	 * appropriate. Authentications with level 0 SHOULD NOT be used to authorize
	 * access to any resource of any monetary value. (This corresponds to the
	 * OpenID 2.0 <a href=
	 * "http://openid.net/specs/openid-connect-core-1_0.html#OpenID.PAPE">PAPE</a>
	 * [OpenID.PAPE] nist_auth_level 0.) An absolute URI or an RFC 6711
	 * [RFC6711] registered name SHOULD be used as the acr value; registered
	 * names MUST NOT be used with a different meaning than that which is
	 * registered. Parties using this claim will need to agree upon the meanings
	 * of the values used, which may be context-specific. The acr value is a
	 * case sensitive string.
	 */
	default String getAcr() {
		return (String) getClaim(OPEN_ID_CLAIM_ID_TOKEN_ACR);
	}

	/**
	 * OPTIONAL. Authentication Methods References. JSON array of strings that
	 * are identifiers for authentication methods used in the authentication.
	 * For instance, values might indicate that both password and OTP
	 * authentication methods were used. The definition of particular values to
	 * be used in the amr Claim is beyond the scope of this specification.
	 * Parties using this claim will need to agree upon the meanings of the
	 * values used, which may be context-specific. The amr value is an array of
	 * case sensitive strings.
	 * <p>
	 * Note: if the actual type is a string array, then the value gets
	 * transformed to a collection.
	 */
	@SuppressWarnings({ "unchecked" })
	default Collection<String> getAmr() {
		Object amrObj = getClaim(OPEN_ID_CLAIM_ID_TOKEN_AMR);
		if (amrObj == null) {
			return null;
		} else if (amrObj instanceof Collection) {
			return (Collection<String>) amrObj;
		} else if (amrObj instanceof String[]) {
			return Arrays.asList((String[]) amrObj);
		} else {
			throw new ClassCastException("amr is of invalid type '" + amrObj.getClass().getName() + "'");
		}
	}

	/**
	 * OPTIONAL. Authorized party - the party to which the ID Token was issued.
	 * If present, it MUST contain the OAuth 2.0 Client ID of this party. This
	 * Claim is only needed when the ID Token has a single audience value and
	 * that audience is different than the authorized party. It MAY be included
	 * even when the authorized party is the same as the sole audience. The azp
	 * value is a case sensitive string containing a StringOrURI value.
	 */
	default String getAzp() {
		return (String) getClaim(OPEN_ID_CLAIM_ID_TOKEN_AZP);
	}
}
