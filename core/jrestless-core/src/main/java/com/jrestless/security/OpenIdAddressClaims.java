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

import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_COUNTRY;
import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_FORMATTED;
import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_LOCALITY;
import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_POSTAL_CODE;
import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_REGION;
import static com.jrestless.security.OpenIdClaimFieldNames.ADDRESS_CLAIM_STREET_ADDRESS;

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

	/**
	 * Full mailing address, formatted for display or use on a mailing label.
	 * This field MAY contain multiple lines, separated by newlines. Newlines
	 * can be represented either as a carriage return/line feed pair ("\r\n") or
	 * as a single line feed character ("\n").
	 */
	default String getFormatted() {
		return (String) getClaim(ADDRESS_CLAIM_FORMATTED);
	}

	/**
	 * Full street address component, which MAY include house number, street
	 * name, Post Office Box, and multi-line extended street address
	 * information. This field MAY contain multiple lines, separated by
	 * newlines. Newlines can be represented either as a carriage return/line
	 * feed pair ("\r\n") or as a single line feed character ("\n").
	 */
	default String getStreetAddress() {
		return (String) getClaim(ADDRESS_CLAIM_STREET_ADDRESS);
	}

	/**
	 * City or locality component.
	 */
	default String getLocality() {
		return (String) getClaim(ADDRESS_CLAIM_LOCALITY);
	}

	/**
	 * State, province, prefecture, or region component.
	 */
	default String getRegion() {
		return (String) getClaim(ADDRESS_CLAIM_REGION);
	}

	/**
	 * Zip code or postal code component.
	 */
	default String getPostalCode() {
		return (String) getClaim(ADDRESS_CLAIM_POSTAL_CODE);
	}

	/**
	 * Country name component.
	 */
	default String getCountry() {
		return (String) getClaim(ADDRESS_CLAIM_COUNTRY);
	}

}
