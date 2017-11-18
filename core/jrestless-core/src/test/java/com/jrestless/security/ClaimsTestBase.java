package com.jrestless.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.params.provider.Arguments;


public abstract class ClaimsTestBase<T extends Claims> {

	private final Map<String, Object> claimsMap = new HashMap<>();

	Map<String, Object> getClaimsMap() {
		return claimsMap;
	}

	abstract T getClaims();

	protected void testGetterReturnsMapValue(Function<T, ?> claimGetter, String mapKey, Object mapValue) {
		claimsMap.clear();
		claimsMap.put(mapKey, mapValue);
		assertEquals(mapValue, claimGetter.apply(getClaims()));
	}

	protected void testGetterReturnsNullIfNoValueInMap(Function<T, ?> claimGetter) {
		claimsMap.clear();
		assertNull(claimGetter.apply(getClaims()));
	}

	protected void testGetterThrowsClassCastExceptionIfInvalidTypeInMap(Function<T, ?> claimGetter,
			String mapKey) {
		claimsMap.clear();
		claimsMap.put(mapKey, new Object());
		try {
			claimGetter.apply(getClaims());
			fail("expected ClassCastException");
		} catch (ClassCastException cce) {
			// expected
		}
	}

	protected static class ClaimArguments<T extends Claims> implements Arguments {

		private final Function<T, Object> claimGetter;
		private final String claimMapKey;
		private final Object claimMapValue;

		private ClaimArguments(Function<T, Object> claimGetter, String claimMapKey, Object claimMapValue) {
			super();
			this.claimGetter = claimGetter;
			this.claimMapKey = claimMapKey;
			this.claimMapValue = claimMapValue;
		}

		@Override
		public Object[] get() {
			return new Object[] {
				claimGetter,
				claimMapKey,
				claimMapValue
			};
		}

		static <T extends Claims> ClaimArguments<T> of(Function<T, Object> claimGetter, String claimMapKey) {
			return of(claimGetter, claimMapKey, "some" + claimMapKey + "value");
		}

		static <T extends Claims> ClaimArguments<T> of(Function<T, Object> claimGetter, String claimMapKey,
				Object claimMapValue) {
			return new ClaimArguments<>(claimGetter, claimMapKey, claimMapValue);
		}
	}
}
