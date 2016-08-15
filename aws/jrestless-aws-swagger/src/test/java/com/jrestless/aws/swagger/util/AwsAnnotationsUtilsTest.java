/*
 * Copyright 2016 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.aws.swagger.util;

import static com.jrestless.aws.swagger.util.AwsAnnotationsUtils.getAdditionalStatusCodes;
import static com.jrestless.aws.swagger.util.AwsAnnotationsUtils.getDefaultStatusCodeOrDefault;
import static com.jrestless.aws.swagger.util.AwsAnnotationsUtils.isCorsEnabledOrDefault;
import static com.jrestless.aws.swagger.util.AwsAnnotationsUtils.isSecured;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.jrestless.aws.annotation.Cors;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class AwsAnnotationsUtilsTest {

	@Test
	public void isCorsEnabledOrDefault_NoCorsGiven_ShouldReturnAnnotationsDefaultValue() {
		assertTrue(isCorsEnabledOrDefault(getMethod(NoCors.class)));
	}

	@Test
	public void isCorsEnabledOrDefault_CorsOnClassGiven_ShouldReturnAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnClass.class)));
	}

	@Test
	public void isCorsEnabledOrDefault_CorsOnMethodGiven_ShouldReturnAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnMethod.class)));
	}

	@Test
	public void isCorsEnabledOrDefault_CorsOnClassAndMethodGiven_ShouldReturnMethodAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnClassAndMethod.class)));
	}

	@Test
	public void isCorsEnabledOrDefaultDefaultParam_NoCorsGiven_ShouldReturnPassedDefaultValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(NoCors.class), false));
	}

	@Test
	public void isCorsEnabledOrDefaultDefaultParam_CorsOnClassGiven_ShouldReturnAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnClass.class), true));
	}

	@Test
	public void isCorsEnabledOrDefaultDefaultParam_CorsOnMethodGiven_ShouldReturnAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnMethod.class), true));
	}

	@Test
	public void isCorsEnabledOrDefaultDefaultParam_CorsOnClassAndMethodGiven_ShouldReturnMethodAnnotationValue() {
		assertFalse(isCorsEnabledOrDefault(getMethod(CorsOnClassAndMethod.class), true));
	}

	@Test
	public void getDefaultStatusCodeOrDefault_NoStatusCodeGiven_ShouldReturnAnnotationsDefaultValue() {
		assertEquals(200, getDefaultStatusCodeOrDefault(getMethod(NoStatusCodes.class)));
	}

	@Test
	public void getAdditionalStatusCodes_NoStatusCodeGiven_ShouldReturnAnnotationsDefaultValue() {
		assertEquals(Collections.emptySet(),
				getAdditionalStatusCodes(getMethod(NoStatusCodes.class)));
	}

	@Test
	public void getDefaultStatusCodeOrDefault_StatusCodesOnMethodGiven_ShouldReturnAnnotationValue() {
		assertEquals(3, getDefaultStatusCodeOrDefault(getMethod(StatusCodesOnMethod.class)));
	}

	@Test
	public void getAdditionalStatusCodes_StatusCodesOnMethodGiven_ShouldReturnAnnotationValue() {
		assertEquals(ImmutableSet.of(4, 5),
				getAdditionalStatusCodes(getMethod(StatusCodesOnMethod.class)));
	}

	@Test
	public void isSecured_NoSecurityGiven_ShouldNotBeSecured() {
		assertFalse(isSecured(getMethod(NoSecurity.class)));
	}

	@Test
	public void isSecured_DenyOnClassGiven_ShouldBeSecured() {
		assertTrue(isSecured(getMethod(DenyOnClass.class)));
	}

	@Test
	public void isSecured_RolesOnClassGiven_ShouldBeSecured() {
		assertTrue(isSecured(getMethod(RolesOnClass.class)));
	}

	@Test
	public void isSecured_DenyOnMethodGiven_ShouldBeSecured() {
		assertTrue(isSecured(getMethod(SecurityOnMethod.class, "deny")));
	}

	@Test
	public void isSecured_RolesOnMethodGiven_ShouldBeSecured() {
		assertTrue(isSecured(getMethod(SecurityOnMethod.class, "roles")));
	}

	@Test
	public void isSecured_PermitOnClassDenyOnMethod_ShouldBeSecured() {
		assertTrue(isSecured(getMethod(PermitOnClassSecurityOnMethods.class, "deny")));
	}

	@Test
	public void isSecured_PermitOnClassRoleOnMethod_ShouldNotBeSecured() {
		assertTrue(isSecured(getMethod(PermitOnClassSecurityOnMethods.class, "roles")));
	}

	@Test
	public void isSecured_DenyOnClassPermitOnMethod_ShouldNotBeSecured() {
		assertFalse(isSecured(getMethod(DenyOnClassPermitOnMethods.class)));
	}

	@Test
	public void isSecured_RolesOnClassPermitOnMethod_ShouldNotBeSecured() {
		assertFalse(isSecured(getMethod(RoleOnClassPermitOnMethods.class)));
	}

	private static Method getMethod(Class<?> clazz, String methodName) {
		try {
			return clazz.getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Method getMethod(Class<?> clazz) {
		return getMethod(clazz, "method");
	}

	static class NoCors {
		public void method() {
			;
		}
	}

	@Cors(enabled = false)
	static class CorsOnClass {
		public void method() {
			;
		}
	}

	static class CorsOnMethod {
		@Cors(enabled = false)
		public void method() {
			;
		}
	}

	@Cors(enabled = true)
	static class CorsOnClassAndMethod {
		@Cors(enabled = false)
		public void method() {
			;
		}
	}

	static class NoStatusCodes {
		public void method() {
			;
		}
	}

	static class StatusCodesOnMethod {
		@ApiOperation(value = "", code = 3)
		@ApiResponses({
			@ApiResponse(message = "", code = 4),
			@ApiResponse(message = "", code = 5)
		})
		public void method() {
			;
		}
	}

	static class NoSecurity {
		public void method() {
			;
		}
	}

	@DenyAll
	static class DenyOnClass {
		public void method() {
			;
		}
	}

	@RolesAllowed("...")
	static class RolesOnClass {
		public void method() {
			;
		}
	}

	static class SecurityOnMethod {
		@DenyAll
		public void deny() {
			;
		}
		@RolesAllowed("...")
		public void roles() {
			;
		}
	}

	@PermitAll
	static class PermitOnClassSecurityOnMethods {
		@DenyAll
		public void deny() {
			;
		}
		@RolesAllowed("...")
		public void roles() {
			;
		}
	}

	@DenyAll
	static class DenyOnClassPermitOnMethods {
		@PermitAll
		public void method() {
			;
		}
	}

	@RolesAllowed("...")
	static class RoleOnClassPermitOnMethods {
		@PermitAll
		public void method() {
			;
		}
	}
}
