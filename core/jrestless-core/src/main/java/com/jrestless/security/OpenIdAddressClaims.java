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

/**
 * OpenID Address claims.
 * <p>
 * See
 * <a href= "http://openid.net/specs/openid-connect-core-1_0.html#AddressClaim">
 * http://openid.net/specs/openid-connect-core-1_0.html#AddressClaim</a>
 *
 * @author Bjoern Bilger
 *
 */
public interface OpenIdAddressClaims extends Claims {

	String OPEN_ID_CLAIM_ADDRESS_FORMATTED = "formatted";
	String OPEN_ID_CLAIM_ADDRESS_STREET_ADDRESS = "street_address";
	String OPEN_ID_CLAIM_ADDRESS_LOCALITY = "locality";
	String OPEN_ID_CLAIM_ADDRESS_REGION = "region";
	String OPEN_ID_CLAIM_ADDRESS_POSTAL_CODE = "postal_code";
	String OPEN_ID_CLAIM_ADDRESS_COUNTRY = "country";

	/**
	 * Full mailing address, formatted for display or use on a mailing label.
	 * This field MAY contain multiple lines, separated by newlines. Newlines
	 * can be represented either as a carriage return/line feed pair ("\r\n") or
	 * as a single line feed character ("\n").
	 */
	default String getFormatted() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_FORMATTED);
	}

	/**
	 * Full street address component, which MAY include house number, street
	 * name, Post Office Box, and multi-line extended street address
	 * information. This field MAY contain multiple lines, separated by
	 * newlines. Newlines can be represented either as a carriage return/line
	 * feed pair ("\r\n") or as a single line feed character ("\n").
	 */
	default String getStreetAddress() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_STREET_ADDRESS);
	}

	/**
	 * City or locality component.
	 */
	default String getLocality() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_LOCALITY);
	}

	/**
	 * State, province, prefecture, or region component.
	 */
	default String getRegion() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_REGION);
	}

	/**
	 * Zip code or postal code component.
	 */
	default String getPostalCode() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_POSTAL_CODE);
	}

	/**
	 * Country name component.
	 */
	default String getCountry() {
		return (String) getClaim(OPEN_ID_CLAIM_ADDRESS_COUNTRY);
	}

}
