package com.jrestless.aws.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jrestless.test.UtilityClassCodeCoverageBumper;

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

	@Test
	public void bumpCodeCoverageByInvokingThePrivateConstructor() {
		 UtilityClassCodeCoverageBumper.invokePrivateConstructor(AwsAuthenticationSchemes.class);
	}
}
