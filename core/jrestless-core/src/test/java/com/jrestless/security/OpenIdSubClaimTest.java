package com.jrestless.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenIdSubClaimTest extends ClaimsTest<OpenIdSubClaim> {

	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { "getSub", "sub" }
           });
    }

	public OpenIdSubClaimTest(String getterName, String key)
			throws NoSuchMethodException, SecurityException {
		super(getterName, key, "some" + key + "value");
	}

	@Override
	OpenIdSubClaim getClaims() {
		return new OpenIdSubClaim() {
			@Override
			public Object getClaim(String name) {
				return getClaimsMap().get(name);
			}
		};
	}

	@Override
	Method getGetterByName(String getterName) throws NoSuchMethodException, SecurityException {
		return OpenIdSubClaim.class.getMethod(getterName);
	}
}
