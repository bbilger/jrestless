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
 * Claim field name constants.
 *
 * @author Bjoern Bilger
 *
 */
public final class OpenIdClaimFieldNames {

	private OpenIdClaimFieldNames() {
	}

	static final String CLAIM_SUB = "sub";

	public static final String ID_TOKEN_CLAIM_ISS = "iss";
	public static final String ID_TOKEN_CLAIM_SUB = CLAIM_SUB;
	public static final String ID_TOKEN_CLAIM_AUD = "aud";
	public static final String ID_TOKEN_CLAIM_EXP = "exp";
	public static final String ID_TOKEN_CLAIM_IAT = "iat";
	public static final String ID_TOKEN_CLAIM_AUTH_TIME = "auth_time";
	public static final String ID_TOKEN_CLAIM_NONCE = "nonce";
	public static final String ID_TOKEN_CLAIM_ACR = "acr";
	public static final String ID_TOKEN_CLAIM_AMR = "amr";
	public static final String ID_TOKEN_CLAIM_AZP = "azp";

	public static final String STANDARD_CLAIM_SUB = CLAIM_SUB;
	public static final String STANDARD_CLAIM_NAME = "name";
	public static final String STANDARD_CLAIM_GIVEN_NAME = "given_name";
	public static final String STANDARD_CLAIM_FAMILY_NAME = "family_name";
	public static final String STANDARD_CLAIM_MIDDLE_NAME = "middle_name";
	public static final String STANDARD_CLAIM_NICKNAME = "nickname";
	public static final String STANDARD_CLAIM_PREFERRED_USERNAME = "preferred_username";
	public static final String STANDARD_CLAIM_PROFILE = "profile";
	public static final String STANDARD_CLAIM_PICTURE = "picture";
	public static final String STANDARD_CLAIM_WEBSITE = "website";
	public static final String STANDARD_CLAIM_EMAIL = "email";
	public static final String STANDARD_CLAIM_EMAIL_VERIFIED = "email_verified";
	public static final String STANDARD_CLAIM_GENDER = "gender";
	public static final String STANDARD_CLAIM_BIRTHDATE = "birthdate";
	public static final String STANDARD_CLAIM_ZONEINFO = "zoneinfo";
	public static final String STANDARD_CLAIM_LOCALE = "locale";
	public static final String STANDARD_CLAIM_PHONE_NUMBER = "phone_number";
	public static final String STANDARD_CLAIM_PHONE_NUMBER_VERIFIED = "phone_number_verified";
	public static final String STANDARD_CLAIM_ADDRESS = "address";
	public static final String STANDARD_CLAIM_UPDATED_AT = "updated_at";

	public static final String ADDRESS_CLAIM_FORMATTED = "formatted";
	public static final String ADDRESS_CLAIM_STREET_ADDRESS = "street_address";
	public static final String ADDRESS_CLAIM_LOCALITY = "locality";
	public static final String ADDRESS_CLAIM_REGION = "region";
	public static final String ADDRESS_CLAIM_POSTAL_CODE = "postal_code";
	public static final String ADDRESS_CLAIM_COUNTRY = "country";
}
