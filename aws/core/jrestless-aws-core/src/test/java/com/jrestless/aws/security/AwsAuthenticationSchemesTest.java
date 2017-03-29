package com.jrestless.aws.security;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AwsAuthenticationSchemesTest {

	@Test
	public void testAllAwsAuthenticationSchemesContainsAllAuthenticationSchemes()
			throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = AwsAuthenticationSchemes.class.getDeclaredFields();
		List<String> authenticationSchemes = new ArrayList<>();
		for(Field field : fields) {
			if (field.getType().isAssignableFrom(String.class)) {
				authenticationSchemes.add((String) field.get(null));
			}
		}
		authenticationSchemes.removeAll(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		assertTrue(authenticationSchemes.isEmpty());
	}
}
