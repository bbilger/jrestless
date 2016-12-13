package com.jrestless.security;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenIdIdTokenClaimsNpeTest {
	@Parameters
    public static Collection<Object[]> data() {
    	return Arrays.asList(new Object[][] {
			{ "getIss" },
			{ "getSub" },
			{ "getAud" },
			{ "getSingleAud" },
			{ "getExp" },
			{ "getIat" }
    	});
    }

	private OpenIdIdTokenClaims openIdIdTokenClaims = new OpenIdIdTokenClaims() {
		@Override
		public Map<String, Object> getAllClaims() {
			return Collections.emptyMap();
		}
	};

	private final String getterName;

	public OpenIdIdTokenClaimsNpeTest(String getterName) {
		this.getterName = getterName;
	}


	@Test
	public void noValueGiven_ShouldThrowNpe() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		try {
			OpenIdIdTokenClaims.class.getMethod(getterName).invoke(openIdIdTokenClaims);
		} catch (InvocationTargetException ite) {
			assertTrue(ite.getTargetException() instanceof NullPointerException);
		}
	}
}
