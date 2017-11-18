package com.jrestless.security;


import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OpenIdIdTokenClaimsNpeTest {

    public static Stream<Arguments> data() {
    	Stream<Function<OpenIdIdTokenClaims, ?>> stream;
    	stream = Stream.of(
			OpenIdIdTokenClaims::getIss,
			OpenIdIdTokenClaims::getSub,
			OpenIdIdTokenClaims::getAud,
			OpenIdIdTokenClaims::getSingleAud,
			OpenIdIdTokenClaims::getExp,
			OpenIdIdTokenClaims::getIat);
		return stream.map(Arguments::of);
    }

	private OpenIdIdTokenClaims openIdIdTokenClaims = new OpenIdIdTokenClaims() {
		@Override
		public Map<String, Object> getAllClaims() {
			return Collections.emptyMap();
		}
	};

	@ParameterizedTest
	@MethodSource("data")
	public void noValueGiven_ShouldThrowNpe(Function<OpenIdIdTokenClaims, ?> getter) {
		try {
			getter.apply(openIdIdTokenClaims);
			fail("expected an NPE");
		} catch (NullPointerException npe) {
			// expected
		}
	}
}
