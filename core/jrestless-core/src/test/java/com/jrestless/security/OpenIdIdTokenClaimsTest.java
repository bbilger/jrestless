package com.jrestless.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OpenIdIdTokenClaimsTest extends ClaimsTestBase<OpenIdIdTokenClaims> {

	public static Stream<? extends Arguments> data(boolean excludeMandatory) {
		List<ClaimArguments<OpenIdIdTokenClaims>> getters = new ArrayList<>();
		if (!excludeMandatory) {
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getIss, "iss"));
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getSub, "sub"));
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getAud, "aud", Collections.singletonList("aud0")));
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getSingleAud, "aud"));
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getExp, "exp", 123L));
			getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getIat, "iat", 123L));
		}
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getAuthTime, "auth_time", 123L));
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getNonce, "nonce"));
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getAcr, "acr"));
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getNonce, "nonce"));
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getAmr, "amr", Collections.singletonList("amr0")));
		getters.add(ClaimArguments.of(OpenIdIdTokenClaims::getAzp, "azp"));


		return getters.stream();
	}

	public static Stream<? extends Arguments> dataAll() {
		return data(false);
	}

	public static Stream<? extends Arguments> dataOptional() {
		return data(true);
	}

	@Override
	OpenIdIdTokenClaims getClaims() {
		return new OpenIdIdTokenClaims() {
			@Override
			public Map<String, Object> getAllClaims() {
				return getClaimsMap();
			}
		};
	}

	@ParameterizedTest
	@MethodSource("dataAll")
	public void get_MapValueGiven_ShouldReturnMapValue(Function<OpenIdIdTokenClaims, Object> getter, String mapKey,
			Object mapValue) {
		testGetterReturnsMapValue(getter, mapKey, mapValue);
	}

	@ParameterizedTest
	@MethodSource("dataOptional")
	public void get_NoMapValueGiven_ShouldReturnNull(Function<OpenIdIdTokenClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}

	@ParameterizedTest
	@MethodSource("dataOptional")
	public void get_InvalidTypeValueGiven_ShouldThrowClassCastException(Function<OpenIdIdTokenClaims, Object> getter) {
		testGetterReturnsNullIfNoValueInMap(getter);
	}

	@Test
	public void getAud_StringArrayInMapGiven_ShouldReturnList() {
		getClaimsMap().put("aud", new String[] { "aud0" });
		assertEquals(Collections.singletonList("aud0"), getClaims().getAud());
	}

	@Test
	public void getAmr_StringArrayInMapGiven_ShouldReturnList() {
		getClaimsMap().put("amr", new String[] { "amr0" });
		assertEquals(Collections.singletonList("amr0"), getClaims().getAmr());
	}

}
