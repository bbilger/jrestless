package com.jrestless.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class OpenIdIdTokenClaimsTransformTest {

	private Map<String, Object> claimsMaps = new HashMap<>();
	private OpenIdIdTokenClaims openIdIdTokenClaims = new OpenIdIdTokenClaims() {
		@Override
		public Object getClaim(String name) {
			return claimsMaps.get(name);
		}
	};

	@Test(expected = NullPointerException.class)
	public void getAud_AudNullGiven_ShouldThrowNpe() {
		assertNull(openIdIdTokenClaims.getAud());
	}

	@Test
	public void getAud_AudListGiven_ShouldReturnSameList() {
		List<String> auds = Collections.singletonList("aud0");
		claimsMaps.put("aud", auds);
		assertSame(auds, openIdIdTokenClaims.getAud());
	}

	@Test
	public void getAud_AudSetGiven_ShouldReturnSameSet() {
		Set<String> auds = Collections.singleton("aud0");
		claimsMaps.put("aud", auds);
		assertSame(auds, openIdIdTokenClaims.getAud());
	}

	@Test
	public void getAud_AudStringArrayGiven_ShouldReturnList() {
		String[] auds = new String[] { "aud0" };
		claimsMaps.put("aud", auds);
		assertEquals(Collections.singletonList("aud0"), openIdIdTokenClaims.getAud());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getAud_AudStringArrayGiven_ShouldReturnUnmodifiableList() {
		String[] auds = new String[] { "aud0" };
		claimsMaps.put("aud", auds);
		openIdIdTokenClaims.getAud().add("123");
	}

	@Test(expected = ClassCastException.class)
	public void getAud_AudNonCollectionOrStringArrayGiven_ShouldThrowClassCastException() {
		claimsMaps.put("aud", 123L);
		openIdIdTokenClaims.getAud();
	}

	@Test
	public void getAmr_AmrNullGiven_ShouldReturnNull() {
		assertNull(openIdIdTokenClaims.getAmr());
	}

	@Test
	public void getAmr_AmrListGiven_ShouldReturnSameList() {
		List<String> auds = Collections.singletonList("amr0");
		claimsMaps.put("amr", auds);
		assertSame(auds, openIdIdTokenClaims.getAmr());
	}

	@Test
	public void getAmr_AmrSetGiven_ShouldReturnSameSet() {
		Set<String> auds = Collections.singleton("amr0");
		claimsMaps.put("amr", auds);
		assertSame(auds, openIdIdTokenClaims.getAmr());
	}

	@Test
	public void getAmr_AmrStringArrayGiven_ShouldReturnList() {
		String[] auds = new String[] { "amr0" };
		claimsMaps.put("amr", auds);
		assertEquals(Collections.singletonList("amr0"), openIdIdTokenClaims.getAmr());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getAmr_AmrStringArrayGiven_ShouldReturnUnmodifiableList() {
		String[] auds = new String[] { "amr0" };
		claimsMaps.put("amr", auds);
		openIdIdTokenClaims.getAmr().add("123");
	}

	@Test(expected = ClassCastException.class)
	public void getAmr_AmrNonCollectionOrStringArrayGiven_ShouldThrowClassCastException() {
		claimsMaps.put("amr", 123L);
		openIdIdTokenClaims.getAmr();
	}
}
