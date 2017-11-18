package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.core.SecurityContext;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerClaims;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.security.OpenIdAddressClaims;

public class CognitoUserPoolSecurityContextFactoryTest extends SecurityContextFactoryTestBase {

	@Test
	public void isApplicable_NoContextGiven_ShouldNotBeApplicable() {
		GatewayRequest request = mock(GatewayRequest.class);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_NoAuthorizerGiven_ShouldNotBeApplicable() {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(null);
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_NoClaimsGiven_ShouldNotBeApplicable() {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(Collections.emptyMap());
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		assertFalse(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_InvalidClaimsGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock(null, null);
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isApplicable_ValidClaimsGiven_ShouldBeApplicable() {
		GatewayRequest request = createRequestMock("subClaim", null);
		assertTrue(createSecurityContextFactory(request).isApplicable());
	}

	@Test
	public void isValid_InvalidClaimTypeGiven_ShouldNotBeValid() {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(Collections.singletonMap("claims", 1));
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_NoSubClaimGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock(null, null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_InvalidSubClaimTypeGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock(1, null);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_InvalidAddressClaimTypeGiven_ShouldNotBeValid() {
		GatewayRequest request = createRequestMock("subClaim", 1);
		assertFalse(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_SubClaimAndNoAddressGiven_ShouldBeValid() {
		GatewayRequest request = createRequestMock("subClaim", null);
		assertTrue(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void isValid_SubClaimAndAddressGiven_ShouldBeValid() {
		GatewayRequest request = createRequestMock("subClaim", Collections.emptyMap());
		assertTrue(createSecurityContextFactory(request).isValid());
	}

	@Test
	public void createSecurityContext_MinimalClaimsGiven_ShouldCreateSecurityContextWithCognitoUserPoolPrincipal() {
		GatewayRequest request = createRequestMock(Collections.singletonMap("sub", "subClaim"));
		SecurityContext sc = createSecurityContextFactory(request).createSecurityContext();
		CognitoUserPoolAuthorizerPrincipal principal = (CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal();
		assertEquals("subClaim", principal.getName());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals2("subClaim", congitoUserPoolClaims.getSub(), congitoUserPoolClaims.getAllClaims().get("sub"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getIss, congitoUserPoolClaims.getAllClaims().get("iss"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getAud, congitoUserPoolClaims.getAllClaims().get("aud"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getSingleAud, congitoUserPoolClaims.getAllClaims().get("aud"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getExp, congitoUserPoolClaims.getAllClaims().get("exp"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getIat, congitoUserPoolClaims.getAllClaims().get("iat"));
		assertNull2(congitoUserPoolClaims.getAuthTime(), congitoUserPoolClaims.getAllClaims().get("auth_time"));
		assertNull2(congitoUserPoolClaims.getNonce(), congitoUserPoolClaims.getAllClaims().get("nonce"));
		assertNull2(congitoUserPoolClaims.getAcr(), congitoUserPoolClaims.getAllClaims().get("acr"));
		assertNull2(congitoUserPoolClaims.getAmr(), congitoUserPoolClaims.getAllClaims().get("amr"));
		assertNull2(congitoUserPoolClaims.getAzp(), congitoUserPoolClaims.getAllClaims().get("azp"));
		assertNull2(congitoUserPoolClaims.getCognitoUserName(), congitoUserPoolClaims.getAllClaims().get("cognito:username"));
		assertNull(congitoUserPoolClaims.getAllClaims().get("custom:blub"));

		assertNull2(congitoUserPoolClaims.getName(), congitoUserPoolClaims.getAllClaims().get("name"));
		assertNull2(congitoUserPoolClaims.getGivenName(), congitoUserPoolClaims.getAllClaims().get("given_name"));
		assertNull2(congitoUserPoolClaims.getFamilyName(), congitoUserPoolClaims.getAllClaims().get("family_name"));
		assertNull2(congitoUserPoolClaims.getMiddleName(), congitoUserPoolClaims.getAllClaims().get("middle_name"));
		assertNull2(congitoUserPoolClaims.getPreferredUsername(), congitoUserPoolClaims.getAllClaims().get("preferred_username"));
		assertNull2(congitoUserPoolClaims.getProfile(), congitoUserPoolClaims.getAllClaims().get("profile"));
		assertNull2(congitoUserPoolClaims.getPicture(), congitoUserPoolClaims.getAllClaims().get("picture"));
		assertNull2(congitoUserPoolClaims.getWebsite(), congitoUserPoolClaims.getAllClaims().get("website"));
		assertNull2(congitoUserPoolClaims.getEmail(), congitoUserPoolClaims.getAllClaims().get("email"));
		assertNull2(congitoUserPoolClaims.getEmailVerified(), congitoUserPoolClaims.getAllClaims().get("email_verified"));
		assertNull2(congitoUserPoolClaims.getGender(), congitoUserPoolClaims.getAllClaims().get("gender"));
		assertNull2(congitoUserPoolClaims.getBirthdate(), congitoUserPoolClaims.getAllClaims().get("birthdate"));
		assertNull2(congitoUserPoolClaims.getZoneinfo(), congitoUserPoolClaims.getAllClaims().get("zoneinfo"));
		assertNull2(congitoUserPoolClaims.getLocale(), congitoUserPoolClaims.getAllClaims().get("locale"));
		assertNull2(congitoUserPoolClaims.getPhoneNumber(), congitoUserPoolClaims.getAllClaims().get("phone_number"));
		assertNull2(congitoUserPoolClaims.getPhoneNumberVerified(), congitoUserPoolClaims.getAllClaims().get("phone_number_verified"));
		assertNull2(congitoUserPoolClaims.getUpdatedAt(), congitoUserPoolClaims.getAllClaims().get("updated_at"));

		assertNull(congitoUserPoolClaims.getAddress());
	}

	@Test
	public void createSecurityContext_FullClaimsGiven_ShouldCreateSecurityContextWithCognitoUserPoolPrincipal() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", "someSubValue");
		claims.put("iss", "someIssValue");
		claims.put("aud", ImmutableList.of("someAud0", "someAud1"));
		claims.put("exp", 1L);
		claims.put("iat", 2L);
		claims.put("auth_time", 3L);
		claims.put("nonce", "someNonceValue");
		claims.put("acr", "someAcrValue");
		claims.put("amr", ImmutableList.of("someAmr0", "someAmr1"));
		claims.put("azp", "someAzpValue");
		claims.put("name", "someNameValue");
		claims.put("given_name", "someGivenNameValue");
		claims.put("family_name", "someFamilyNameValue");
		claims.put("middle_name", "someMiddleNameValue");
		claims.put("nickname", "someNickNameValue");
		claims.put("preferred_username", "somePreferredUsernameValue");
		claims.put("profile", "someProfileValue");
		claims.put("picture", "somePictureValue");
		claims.put("website", "someWebsiteValue");
		claims.put("email", "someEmailValue");
		claims.put("email_verified", true);
		claims.put("gender", "someGenderValue");
		claims.put("birthdate", "someBirthdateValue");
		claims.put("zoneinfo", "someZoneinfoValue");
		claims.put("locale", "someLocaleValue");
		claims.put("phone_number", "somePhoneNumberValue");
		claims.put("phone_number_verified", true);
		claims.put("updated_at", 4L);
		claims.put("cognito:username", "someCognitoUsernameValue");
		claims.put("custom:blub", "someCustomBlubValue");

		Map<String, Object> addressClaimsMap = new HashMap<>();
		addressClaimsMap.put("formatted", "someFormattedValue");
		addressClaimsMap.put("street_address", "someStreetAddressValue");
		addressClaimsMap.put("locality", "someLocalityValue");
		addressClaimsMap.put("region", "someRegionValue");
		addressClaimsMap.put("postal_code", "somePostalCodeValue");
		addressClaimsMap.put("country", "someCountryValue");
		addressClaimsMap.put("custom:muh", "someCustomMuhValue");

		claims.put("address", addressClaimsMap);

		GatewayRequest request = createRequestMock(claims);
		SecurityContext sc = createSecurityContextFactory(request).createSecurityContext();
		CognitoUserPoolAuthorizerPrincipal principal = (CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal();

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals2("someSubValue", congitoUserPoolClaims.getSub(), congitoUserPoolClaims.getAllClaims().get("sub"));
		assertEquals2("someIssValue", congitoUserPoolClaims.getIss(), congitoUserPoolClaims.getAllClaims().get("iss"));
		assertEquals2(ImmutableList.of("someAud0", "someAud1"), congitoUserPoolClaims.getAud(), congitoUserPoolClaims.getAllClaims().get("aud"));
		assertEquals2(1L, congitoUserPoolClaims.getExp(), congitoUserPoolClaims.getAllClaims().get("exp"));
		assertEquals2(2L, congitoUserPoolClaims.getIat(), congitoUserPoolClaims.getAllClaims().get("iat"));
		assertEquals2(3L, congitoUserPoolClaims.getAuthTime(), congitoUserPoolClaims.getAllClaims().get("auth_time"));
		assertEquals2("someNonceValue", congitoUserPoolClaims.getNonce(), congitoUserPoolClaims.getAllClaims().get("nonce"));
		assertEquals2("someAcrValue", congitoUserPoolClaims.getAcr(), congitoUserPoolClaims.getAllClaims().get("acr"));
		assertEquals2(ImmutableList.of("someAmr0", "someAmr1"), congitoUserPoolClaims.getAmr(), congitoUserPoolClaims.getAllClaims().get("amr"));
		assertEquals2("someAzpValue", congitoUserPoolClaims.getAzp(), congitoUserPoolClaims.getAllClaims().get("azp"));
		assertEquals2("someCognitoUsernameValue", congitoUserPoolClaims.getCognitoUserName(), congitoUserPoolClaims.getAllClaims().get("cognito:username"));
		assertEquals("someCustomBlubValue", congitoUserPoolClaims.getAllClaims().get("custom:blub"));

		assertEquals2("someNameValue", congitoUserPoolClaims.getName(), congitoUserPoolClaims.getAllClaims().get("name"));
		assertEquals2("someGivenNameValue", congitoUserPoolClaims.getGivenName(), congitoUserPoolClaims.getAllClaims().get("given_name"));
		assertEquals2("someFamilyNameValue", congitoUserPoolClaims.getFamilyName(), congitoUserPoolClaims.getAllClaims().get("family_name"));
		assertEquals2("someMiddleNameValue", congitoUserPoolClaims.getMiddleName(), congitoUserPoolClaims.getAllClaims().get("middle_name"));
		assertEquals2("somePreferredUsernameValue", congitoUserPoolClaims.getPreferredUsername(), congitoUserPoolClaims.getAllClaims().get("preferred_username"));
		assertEquals2("someProfileValue", congitoUserPoolClaims.getProfile(), congitoUserPoolClaims.getAllClaims().get("profile"));
		assertEquals2("somePictureValue", congitoUserPoolClaims.getPicture(), congitoUserPoolClaims.getAllClaims().get("picture"));
		assertEquals2("someWebsiteValue", congitoUserPoolClaims.getWebsite(), congitoUserPoolClaims.getAllClaims().get("website"));
		assertEquals2("someEmailValue", congitoUserPoolClaims.getEmail(), congitoUserPoolClaims.getAllClaims().get("email"));
		assertEquals2(true, congitoUserPoolClaims.getEmailVerified(), congitoUserPoolClaims.getAllClaims().get("email_verified"));
		assertEquals2("someGenderValue", congitoUserPoolClaims.getGender(), congitoUserPoolClaims.getAllClaims().get("gender"));
		assertEquals2("someBirthdateValue", congitoUserPoolClaims.getBirthdate(), congitoUserPoolClaims.getAllClaims().get("birthdate"));
		assertEquals2("someZoneinfoValue", congitoUserPoolClaims.getZoneinfo(), congitoUserPoolClaims.getAllClaims().get("zoneinfo"));
		assertEquals2("someLocaleValue", congitoUserPoolClaims.getLocale(), congitoUserPoolClaims.getAllClaims().get("locale"));
		assertEquals2("somePhoneNumberValue", congitoUserPoolClaims.getPhoneNumber(), congitoUserPoolClaims.getAllClaims().get("phone_number"));
		assertEquals2(true, congitoUserPoolClaims.getPhoneNumberVerified(), congitoUserPoolClaims.getAllClaims().get("phone_number_verified"));
		assertEquals2(4L, congitoUserPoolClaims.getUpdatedAt(), congitoUserPoolClaims.getAllClaims().get("updated_at"));

		OpenIdAddressClaims addressClaims = congitoUserPoolClaims.getAddress();
		assertEquals2("someFormattedValue", addressClaims.getFormatted(), addressClaims.getAllClaims().get("formatted"));
		assertEquals2("someStreetAddressValue", addressClaims.getStreetAddress(), addressClaims.getAllClaims().get("street_address"));
		assertEquals2("someLocalityValue", addressClaims.getLocality(), addressClaims.getAllClaims().get("locality"));
		assertEquals2("someRegionValue", addressClaims.getRegion(), addressClaims.getAllClaims().get("region"));
		assertEquals2("somePostalCodeValue", addressClaims.getPostalCode(), addressClaims.getAllClaims().get("postal_code"));
		assertEquals2("someCountryValue", addressClaims.getCountry(), addressClaims.getAllClaims().get("country"));
		assertEquals("someCustomMuhValue", addressClaims.getAllClaims().get("custom:muh"));
	}

	@Test
	public void createSecurityContext_FullClaimsWithInvalidTypesGiven_ShouldCreateSecurityContextWithCognitoUserPoolPrincipal() {

		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", "123");
		claims.put("iss", new Object());
		claims.put("aud", new Object());
		claims.put("exp", new Object());
		claims.put("iat", new Object());
		claims.put("auth_time", new Object());
		claims.put("nonce", new Object());
		claims.put("acr", new Object());
		claims.put("amr", new Object());
		claims.put("azp", new Object());
		claims.put("name", new Object());
		claims.put("given_name", new Object());
		claims.put("family_name", new Object());
		claims.put("middle_name", new Object());
		claims.put("nickname", new Object());
		claims.put("preferred_username", new Object());
		claims.put("profile", new Object());
		claims.put("picture", new Object());
		claims.put("website", new Object());
		claims.put("email", new Object());
		claims.put("email_verified", new Object());
		claims.put("gender", new Object());
		claims.put("birthdate", new Object());
		claims.put("zoneinfo", new Object());
		claims.put("locale", new Object());
		claims.put("phone_number", new Object());
		claims.put("phone_number_verified", new Object());
		claims.put("updated_at", new Object());
		claims.put("cognito:username", new Object());

		Map<String, Object> addressClaimsMap = new HashMap<>();
		addressClaimsMap.put("formatted", new Object());
		addressClaimsMap.put("street_address", new Object());
		addressClaimsMap.put("locality", new Object());
		addressClaimsMap.put("region", new Object());
		addressClaimsMap.put("postal_code", new Object());
		addressClaimsMap.put("country", new Object());

		claims.put("address", addressClaimsMap);

		GatewayRequest request = createRequestMock(claims);
		SecurityContext sc = createSecurityContextFactory(request).createSecurityContext();
		CognitoUserPoolAuthorizerPrincipal principal = (CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal();

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();

		assertClassCastException(congitoUserPoolClaims::getIss);
		assertClassCastException(congitoUserPoolClaims::getAud);
		assertClassCastException(congitoUserPoolClaims::getExp);
		assertClassCastException(congitoUserPoolClaims::getIat);
		assertClassCastException(congitoUserPoolClaims::getAuthTime);
		assertClassCastException(congitoUserPoolClaims::getNonce);
		assertClassCastException(congitoUserPoolClaims::getAcr);
		assertClassCastException(congitoUserPoolClaims::getAmr);
		assertClassCastException(congitoUserPoolClaims::getAzp);
		assertClassCastException(congitoUserPoolClaims::getCognitoUserName);

		assertClassCastException(congitoUserPoolClaims::getName);
		assertClassCastException(congitoUserPoolClaims::getGivenName);
		assertClassCastException(congitoUserPoolClaims::getFamilyName);
		assertClassCastException(congitoUserPoolClaims::getMiddleName);
		assertClassCastException(congitoUserPoolClaims::getPreferredUsername);
		assertClassCastException(congitoUserPoolClaims::getProfile);
		assertClassCastException(congitoUserPoolClaims::getPicture);
		assertClassCastException(congitoUserPoolClaims::getWebsite);
		assertClassCastException(congitoUserPoolClaims::getEmail);
		assertClassCastException(congitoUserPoolClaims::getEmailVerified);
		assertClassCastException(congitoUserPoolClaims::getGender);
		assertClassCastException(congitoUserPoolClaims::getBirthdate);
		assertClassCastException(congitoUserPoolClaims::getZoneinfo);
		assertClassCastException(congitoUserPoolClaims::getLocale);
		assertClassCastException(congitoUserPoolClaims::getPhoneNumber);
		assertClassCastException(congitoUserPoolClaims::getPhoneNumberVerified);
		assertClassCastException(congitoUserPoolClaims::getUpdatedAt);

		OpenIdAddressClaims addressClaims = congitoUserPoolClaims.getAddress();
		assertClassCastException(addressClaims::getFormatted);
		assertClassCastException(addressClaims::getStreetAddress);
		assertClassCastException(addressClaims::getLocality);
		assertClassCastException(addressClaims::getRegion);
		assertClassCastException(addressClaims::getPostalCode);
		assertClassCastException(addressClaims::getCountry);
	}

	private static GatewayRequest createRequestMock(Object subClaim, Object addressClaim) {
		Map<String, Object> claims = new HashMap<>();
		if (subClaim != null) {
			claims.put("sub", subClaim);
		}
		if (addressClaim != null) {
			claims.put("address", addressClaim);
		}
		return createRequestMock(claims);
	}

	private static GatewayRequest createRequestMock(Map<String, Object> claims) {
		GatewayRequestContext context = mock(GatewayRequestContext.class);
		when(context.getAuthorizer()).thenReturn(Collections.singletonMap("claims", claims));
		GatewayRequest request = mock(GatewayRequest.class);
		when(request.getRequestContext()).thenReturn(context);
		return request;
	}

	private static void assertEquals2(Object actual, Object expected1, Object expected2) {
		assertEquals(actual, expected1);
		assertEquals(actual, expected2);
	}

	private static void assertNull2(Object actual1, Object actual2) {
		assertNull(actual1);
		assertNull(actual2);
	}

	private static void assertNpeFirstAndNullSecond(Supplier<?> getter, Object actual) {
		assertNpe(getter);
		assertNull(actual);
	}

	private static void assertNpe(Supplier<?> getter) {
		try {
			getter.get();
			fail("expected npe");
		} catch (NullPointerException npe) {
			// expected
		}
	}

	private static void assertClassCastException(Supplier<?> getter) {
		try {
			getter.get();
			fail("expected classcastexception");
		} catch (ClassCastException cce) {
			// expected
		}
	}

	@Override
	protected AbstractSecurityContextFactory createSecurityContextFactory(GatewayRequest request) {
		return new CognitoUserPoolSecurityContextFactory(request);
	}

	@Override
	protected String getAuthenticationScheme() {
		return AwsAuthenticationSchemes.AWS_COGNITO_USER_POOL;
	}

	@Override
	protected GatewayRequest createInapplicableInvlidRequest() {
		return mock(GatewayRequest.class);
	}

	@Override
	protected GatewayRequest createApplicableInvalidRequest() {
		return createRequestMock(1, 1);
	}

	@Override
	protected GatewayRequest createApplicableValidRequest() {
		return createRequestMock("subClaim", null);
	}

}
