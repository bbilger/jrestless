package com.jrestless.security;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OpenIdSubClaimTest extends ClaimsTestBase<OpenIdSubClaim> {

	public static Stream<Arguments> data() {
    	return Stream.of(
			ClaimArguments.of(OpenIdSubClaim::getSub, "sub"));
    }

	@Override
	OpenIdSubClaim getClaims() {
		return new OpenIdSubClaim() {
			@Override
			public Map<String, Object> getAllClaims() {
				return getClaimsMap();
			}
		};
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_MapValueGiven_ShouldReturnMapValue(Function<OpenIdSubClaim, String> getter, String mapKey,
			Object mapValue) {
		testGetterReturnsMapValue(getter, mapKey, mapValue);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_NoMapValueGiven_ShouldReturnNull(Function<OpenIdSubClaim, String> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}

	@ParameterizedTest
	@MethodSource("data")
	public void get_InvalidTypeValueGiven_ShouldThrowClassCastException(Function<OpenIdSubClaim, String> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}
}
