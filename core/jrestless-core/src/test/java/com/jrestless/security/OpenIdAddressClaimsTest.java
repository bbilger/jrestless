package com.jrestless.security;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


public class OpenIdAddressClaimsTest extends ClaimsTestBase<OpenIdAddressClaims> {

    public static Stream<Arguments> data() {
    	return Stream.of(
			ClaimArguments.of(OpenIdAddressClaims::getFormatted, "formatted"),
			ClaimArguments.of(OpenIdAddressClaims::getStreetAddress, "street_address"),
			ClaimArguments.of(OpenIdAddressClaims::getLocality, "locality"),
			ClaimArguments.of(OpenIdAddressClaims::getRegion, "region"),
			ClaimArguments.of(OpenIdAddressClaims::getPostalCode, "postal_code"),
			ClaimArguments.of(OpenIdAddressClaims::getCountry, "country"));
    }

	@Override
	OpenIdAddressClaims getClaims() {
		return new OpenIdAddressClaims() {
			@Override
			public Map<String, Object> getAllClaims() {
				return getClaimsMap();
			}
		};
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_MapValueGiven_ShouldReturnMapValue(Function<OpenIdAddressClaims, Object> getter, String mapKey,
			Object mapValue) {
		testGetterReturnsMapValue(getter, mapKey, mapValue);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_NoMapValueGiven_ShouldReturnNull(Function<OpenIdAddressClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_InvalidTypeValueGiven_ShouldThrowClassCastException(Function<OpenIdAddressClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}
}
