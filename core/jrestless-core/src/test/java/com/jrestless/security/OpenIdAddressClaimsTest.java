package com.jrestless.security;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenIdAddressClaimsTest extends ClaimsTest<OpenIdAddressClaims> {

	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { "getFormatted", "formatted" },
                 { "getStreetAddress", "street_address" },
                 { "getLocality", "locality" },
                 { "getRegion", "region" },
                 { "getPostalCode", "postal_code" },
                 { "getCountry", "country" }
           });
    }

	public OpenIdAddressClaimsTest(String getterName, String key)
			throws NoSuchMethodException, SecurityException {
		super(getterName, key, "some" + key + "value");
	}

	@Override
	OpenIdAddressClaims getClaims() {
		return new OpenIdAddressClaims() {
			@Override
			public Object getClaim(String name) {
				return getClaimsMap().get(name);
			}
		};
	}

	@Override
	Method getGetterByName(String getterName) throws NoSuchMethodException, SecurityException {
		return OpenIdAddressClaims.class.getMethod(getterName);
	}

}
