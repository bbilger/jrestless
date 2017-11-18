package com.jrestless.security;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OpenIdStandardClaimsTest extends ClaimsTestBase<OpenIdStandardClaims> {

    public static Stream<Arguments> data() {
    	return Stream.of(
			ClaimArguments.of(OpenIdStandardClaims::getSub, "sub"),
			ClaimArguments.of(OpenIdStandardClaims::getName, "name"),
			ClaimArguments.of(OpenIdStandardClaims::getGivenName, "given_name"),
			ClaimArguments.of(OpenIdStandardClaims::getFamilyName, "family_name"),
			ClaimArguments.of(OpenIdStandardClaims::getMiddleName, "middle_name"),
			ClaimArguments.of(OpenIdStandardClaims::getNickname, "nickname"),
			ClaimArguments.of(OpenIdStandardClaims::getPreferredUsername, "preferred_username"),
			ClaimArguments.of(OpenIdStandardClaims::getProfile, "profile"),
			ClaimArguments.of(OpenIdStandardClaims::getPicture, "picture"),
			ClaimArguments.of(OpenIdStandardClaims::getWebsite, "website"),
			ClaimArguments.of(OpenIdStandardClaims::getEmail, "email"),
			ClaimArguments.of(OpenIdStandardClaims::getEmailVerified, "email_verified", true),
			ClaimArguments.of(OpenIdStandardClaims::getGender, "gender"),
			ClaimArguments.of(OpenIdStandardClaims::getBirthdate, "birthdate"),
			ClaimArguments.of(OpenIdStandardClaims::getZoneinfo, "zoneinfo"),
			ClaimArguments.of(OpenIdStandardClaims::getLocale, "locale"),
			ClaimArguments.of(OpenIdStandardClaims::getPhoneNumber, "phone_number"),
			ClaimArguments.of(OpenIdStandardClaims::getPhoneNumberVerified, "phone_number_verified", true),
			ClaimArguments.of(OpenIdStandardClaims::getUpdatedAt, "updated_at", 123L));
    }

	@Override
	OpenIdStandardClaims getClaims() {
		return new OpenIdStandardClaims() {
			@Override
			public OpenIdAddressClaims getAddress() {
				return null;
			}

			@Override
			public Map<String, Object> getAllClaims() {
				return getClaimsMap();
			}
		};
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_MapValueGiven_ShouldReturnMapValue(Function<OpenIdStandardClaims, Object> getter, String mapKey,
			Object mapValue) {
		testGetterReturnsMapValue(getter, mapKey, mapValue);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_NoMapValueGiven_ShouldReturnNull(Function<OpenIdStandardClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_InvalidTypeValueGiven_ShouldThrowClassCastException(Function<OpenIdStandardClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}
}
