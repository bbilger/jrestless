package com.jrestless.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenIdIdTokenClaimsTest extends ClaimsTest<OpenIdIdTokenClaims> {

	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { "getIss", "iss", "someIssvalue", "someIssvalue", false },
                 { "getSub", "sub", "someSubValue", "someSubValue", false },
                 { "getAud", "aud", Collections.singletonList("aud0"), Collections.singletonList("aud0"), false },
                 { "getAud", "aud", new String[] { "aud0" }, Collections.singletonList("aud0"), false },
                 { "getSingleAud", "aud", "someSingleAud", "someSingleAud", false },
                 { "getExp", "exp", 123L, 123L, false },
                 { "getIat", "iat", 123L, 123L, false },
                 { "getAuthTime", "auth_time", 123L, 123L, true },
                 { "getNonce", "nonce", "someNonceValue", "someNonceValue", true },
                 { "getAcr", "acr", "someAcrValue", "someAcrValue", true },
                 { "getAmr", "amr", Collections.singletonList("amr0"), Collections.singletonList("amr0"), true },
                 { "getAmr", "amr", new String[] { "amr0" }, Collections.singletonList("amr0"), true },
                 { "getAzp", "azp", "someAzpValue", "someAzpValue", true }
           });
    }

	public OpenIdIdTokenClaimsTest(String getterName, String key, Object mapValue, Object transformedValue, boolean testNull)
			throws NoSuchMethodException, SecurityException {
		super(getterName, key, mapValue, transformedValue, testNull);

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

	@Override
	Method getGetterByName(String getterName) throws NoSuchMethodException, SecurityException {
		return OpenIdIdTokenClaims.class.getMethod(getterName);
	}

}
