package com.jrestless.aws.gateway.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.core.SecurityContext;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerClaims;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.security.OpenIdAddressClaims;

public class CognitoUserPoolAuthorizerFilterTest extends AuthorizerFilterTest {

	@Override
	AuthorizerFilter createCognitoAuthorizerFilter(GatewayRequest gatewayRequest) {
		return new CognitoUserPoolAuthorizerFilter(gatewayRequest);
	}

	@Test
	public void nullAuthorizerDateGiven_ShouldNotSetSecurityContext() {
		filterAndVerifyNoSecurityContextSet((Map<String, Object>) null);
	}

	@Test
	public void nullClaimsDateGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet((Map<String, Object>) null);
	}

	@Test
	public void emptyClaimsDateGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet(Collections.emptyMap());
	}

	@Test
	public void noSubClaimGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet(Collections.singletonMap("whatever", "whatever"));
	}

	@Test
	public void emptySubClaimGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet(Collections.singletonMap("sub", ""));
	}

	@Test
	public void blankSubClaimGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet(Collections.singletonMap("sub", "  "));
	}

	@Test
	public void invalidTypeSubClaimGiven_ShouldNotSetSecurityContext() {
		filterWithClaimsAndVerifyNoSecurityContextSet(Collections.singletonMap("sub", new Object()));
	}

	@Test
	public void subClaimGiven_ShouldSetSecurityContext() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertNotNull(sc);
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextThatIsSecure() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertTrue(sc.isSecure());
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextWithUserCognitoPoolAuthorizerAuthenticationScheme() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertEquals("cognito_user_pool_authorizer", sc.getAuthenticationScheme());
	}

	@Test
	public void validRequestGiven_ShouldSetSecurityContextWithUserNeverInAnyRole() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertFalse(sc.isUserInRole(null));
		assertFalse(sc.isUserInRole(""));
		assertFalse(sc.isUserInRole("user"));
		assertFalse(sc.isUserInRole("USER"));
	}

	@Test
	public void subClaimGiven_ShouldSetCognitoUserPoolAuthorizerPrincipalSecurityContext() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertTrue(sc.getUserPrincipal() instanceof CognitoUserPoolAuthorizerPrincipal);
	}

	@Test
	public void subClaimGiven_ShouldSetPrincipalNameToSubClaim() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		assertEquals("123", sc.getUserPrincipal().getName());
	}

	@Test
	public void subClaimGiven_ShouldMakeSubClaimAvailableThroughClaims() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());
		assertEquals("123", principal.getClaims().getClaim("sub"));
	}

	@Test
	public void fullClaimsGiven_ShouldMakeAllClaimsAvailable() {
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

		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(claims);
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals2("someSubValue", congitoUserPoolClaims.getSub(), congitoUserPoolClaims.getClaim("sub"));
		assertEquals2("someIssValue", congitoUserPoolClaims.getIss(), congitoUserPoolClaims.getClaim("iss"));
		assertEquals2(ImmutableList.of("someAud0", "someAud1"), congitoUserPoolClaims.getAud(), congitoUserPoolClaims.getClaim("aud"));
		assertEquals2(1L, congitoUserPoolClaims.getExp(), congitoUserPoolClaims.getClaim("exp"));
		assertEquals2(2L, congitoUserPoolClaims.getIat(), congitoUserPoolClaims.getClaim("iat"));
		assertEquals2(3L, congitoUserPoolClaims.getAuthTime(), congitoUserPoolClaims.getClaim("auth_time"));
		assertEquals2("someNonceValue", congitoUserPoolClaims.getNonce(), congitoUserPoolClaims.getClaim("nonce"));
		assertEquals2("someAcrValue", congitoUserPoolClaims.getAcr(), congitoUserPoolClaims.getClaim("acr"));
		assertEquals2(ImmutableList.of("someAmr0", "someAmr1"), congitoUserPoolClaims.getAmr(), congitoUserPoolClaims.getClaim("amr"));
		assertEquals2("someAzpValue", congitoUserPoolClaims.getAzp(), congitoUserPoolClaims.getClaim("azp"));
		assertEquals2("someCognitoUsernameValue", congitoUserPoolClaims.getCognitoUserName(), congitoUserPoolClaims.getClaim("cognito:username"));
		assertEquals("someCustomBlubValue", congitoUserPoolClaims.getClaim("custom:blub"));

		assertEquals2("someNameValue", congitoUserPoolClaims.getName(), congitoUserPoolClaims.getClaim("name"));
		assertEquals2("someGivenNameValue", congitoUserPoolClaims.getGivenName(), congitoUserPoolClaims.getClaim("given_name"));
		assertEquals2("someFamilyNameValue", congitoUserPoolClaims.getFamilyName(), congitoUserPoolClaims.getClaim("family_name"));
		assertEquals2("someMiddleNameValue", congitoUserPoolClaims.getMiddleName(), congitoUserPoolClaims.getClaim("middle_name"));
		assertEquals2("somePreferredUsernameValue", congitoUserPoolClaims.getPreferredUsername(), congitoUserPoolClaims.getClaim("preferred_username"));
		assertEquals2("someProfileValue", congitoUserPoolClaims.getProfile(), congitoUserPoolClaims.getClaim("profile"));
		assertEquals2("somePictureValue", congitoUserPoolClaims.getPicture(), congitoUserPoolClaims.getClaim("picture"));
		assertEquals2("someWebsiteValue", congitoUserPoolClaims.getWebsite(), congitoUserPoolClaims.getClaim("website"));
		assertEquals2("someEmailValue", congitoUserPoolClaims.getEmail(), congitoUserPoolClaims.getClaim("email"));
		assertEquals2(true, congitoUserPoolClaims.getEmailVerified(), congitoUserPoolClaims.getClaim("email_verified"));
		assertEquals2("someGenderValue", congitoUserPoolClaims.getGender(), congitoUserPoolClaims.getClaim("gender"));
		assertEquals2("someBirthdateValue", congitoUserPoolClaims.getBirthdate(), congitoUserPoolClaims.getClaim("birthdate"));
		assertEquals2("someZoneinfoValue", congitoUserPoolClaims.getZoneinfo(), congitoUserPoolClaims.getClaim("zoneinfo"));
		assertEquals2("someLocaleValue", congitoUserPoolClaims.getLocale(), congitoUserPoolClaims.getClaim("locale"));
		assertEquals2("somePhoneNumberValue", congitoUserPoolClaims.getPhoneNumber(), congitoUserPoolClaims.getClaim("phone_number"));
		assertEquals2(true, congitoUserPoolClaims.getPhoneNumberVerified(), congitoUserPoolClaims.getClaim("phone_number_verified"));
		assertEquals2(4L, congitoUserPoolClaims.getUpdatedAt(), congitoUserPoolClaims.getClaim("updated_at"));

		OpenIdAddressClaims addressClaims = congitoUserPoolClaims.getAddress();
		assertEquals2("someFormattedValue", addressClaims.getFormatted(), addressClaims.getClaim("formatted"));
		assertEquals2("someStreetAddressValue", addressClaims.getStreetAddress(), addressClaims.getClaim("street_address"));
		assertEquals2("someLocalityValue", addressClaims.getLocality(), addressClaims.getClaim("locality"));
		assertEquals2("someRegionValue", addressClaims.getRegion(), addressClaims.getClaim("region"));
		assertEquals2("somePostalCodeValue", addressClaims.getPostalCode(), addressClaims.getClaim("postal_code"));
		assertEquals2("someCountryValue", addressClaims.getCountry(), addressClaims.getClaim("country"));
		assertEquals("someCustomMuhValue", addressClaims.getClaim("custom:muh"));
	}

	@Test
	public void minimalClaimsGiven_ShouldNotMakeUnsetClaimsAvailable() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals2("123", congitoUserPoolClaims.getSub(), congitoUserPoolClaims.getClaim("sub"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getIss, congitoUserPoolClaims.getClaim("iss"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getAud, congitoUserPoolClaims.getClaim("aud"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getSingleAud, congitoUserPoolClaims.getClaim("aud"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getExp, congitoUserPoolClaims.getClaim("exp"));
		assertNpeFirstAndNullSecond(congitoUserPoolClaims::getIat, congitoUserPoolClaims.getClaim("iat"));
		assertNull2(congitoUserPoolClaims.getAuthTime(), congitoUserPoolClaims.getClaim("auth_time"));
		assertNull2(congitoUserPoolClaims.getNonce(), congitoUserPoolClaims.getClaim("nonce"));
		assertNull2(congitoUserPoolClaims.getAcr(), congitoUserPoolClaims.getClaim("acr"));
		assertNull2(congitoUserPoolClaims.getAmr(), congitoUserPoolClaims.getClaim("amr"));
		assertNull2(congitoUserPoolClaims.getAzp(), congitoUserPoolClaims.getClaim("azp"));
		assertNull2(congitoUserPoolClaims.getCognitoUserName(), congitoUserPoolClaims.getClaim("cognito:username"));
		assertNull(congitoUserPoolClaims.getClaim("custom:blub"));

		assertNull2(congitoUserPoolClaims.getName(), congitoUserPoolClaims.getClaim("name"));
		assertNull2(congitoUserPoolClaims.getGivenName(), congitoUserPoolClaims.getClaim("given_name"));
		assertNull2(congitoUserPoolClaims.getFamilyName(), congitoUserPoolClaims.getClaim("family_name"));
		assertNull2(congitoUserPoolClaims.getMiddleName(), congitoUserPoolClaims.getClaim("middle_name"));
		assertNull2(congitoUserPoolClaims.getPreferredUsername(), congitoUserPoolClaims.getClaim("preferred_username"));
		assertNull2(congitoUserPoolClaims.getProfile(), congitoUserPoolClaims.getClaim("profile"));
		assertNull2(congitoUserPoolClaims.getPicture(), congitoUserPoolClaims.getClaim("picture"));
		assertNull2(congitoUserPoolClaims.getWebsite(), congitoUserPoolClaims.getClaim("website"));
		assertNull2(congitoUserPoolClaims.getEmail(), congitoUserPoolClaims.getClaim("email"));
		assertNull2(congitoUserPoolClaims.getEmailVerified(), congitoUserPoolClaims.getClaim("email_verified"));
		assertNull2(congitoUserPoolClaims.getGender(), congitoUserPoolClaims.getClaim("gender"));
		assertNull2(congitoUserPoolClaims.getBirthdate(), congitoUserPoolClaims.getClaim("birthdate"));
		assertNull2(congitoUserPoolClaims.getZoneinfo(), congitoUserPoolClaims.getClaim("zoneinfo"));
		assertNull2(congitoUserPoolClaims.getLocale(), congitoUserPoolClaims.getClaim("locale"));
		assertNull2(congitoUserPoolClaims.getPhoneNumber(), congitoUserPoolClaims.getClaim("phone_number"));
		assertNull2(congitoUserPoolClaims.getPhoneNumberVerified(), congitoUserPoolClaims.getClaim("phone_number_verified"));
		assertNull2(congitoUserPoolClaims.getUpdatedAt(), congitoUserPoolClaims.getClaim("updated_at"));

		assertNull(congitoUserPoolClaims.getAddress());
	}

	@Test
	public void validRequestAndMissingRequiredFieldsGiven_GetRequiredFieldShouldThrowNpe() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(Collections.singletonMap("sub", "123"));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertNpe(congitoUserPoolClaims::getIss);
		assertNpe(congitoUserPoolClaims::getAud);
		assertNpe(congitoUserPoolClaims::getSingleAud);
		assertNpe(congitoUserPoolClaims::getExp);
		assertNpe(congitoUserPoolClaims::getIat);
	}

	@Test
	public void validRequestAndStringArrayAudClaimGiven_ShouldTransformAudClaimToCollection() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "aud", new String[] { "aud0", "aud1" }));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableList.of("aud0", "aud1"), congitoUserPoolClaims.getAud());
	}

	@Test
	public void validRequestAndSetAudClaimGiven_ShouldReturnAudAsSet() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "aud", ImmutableSet.of("aud0", "aud1")));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableSet.of("aud0", "aud1"), congitoUserPoolClaims.getAud());
	}

	@Test
	public void validRequestAndListAudClaimGiven_ShouldReturnAudAsSet() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "aud", ImmutableList.of("aud0", "aud1")));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableList.of("aud0", "aud1"), congitoUserPoolClaims.getAud());
	}

	@Test
	public void validRequestAndStringArrayAmrClaimGiven_ShouldTransformAmrClaimToCollection() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "amr", new String[] { "amr0", "amr1" }));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableList.of("amr0", "amr1"), congitoUserPoolClaims.getAmr());
	}

	@Test
	public void validRequestAndSetAmrClaimGiven_ShouldReturnAmrAsSet() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "amr", ImmutableSet.of("amr0", "amr1")));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableSet.of("amr0", "amr1"), congitoUserPoolClaims.getAmr());
	}

	@Test
	public void validRequestAndListAmrClaimGiven_ShouldReturnAmrAsSet() {
		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(ImmutableMap.of("sub", "someSub", "amr", ImmutableList.of("amr0", "amr1")));
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

		CognitoUserPoolAuthorizerClaims congitoUserPoolClaims = principal.getClaims();
		assertEquals(ImmutableList.of("amr0", "amr1"), congitoUserPoolClaims.getAmr());
	}

	@Test
	public void validRequestAndInvalidTypes_ShouldThrowClassCastExceptionOnAccess() {

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

		SecurityContext sc = filterWithClaimsAndReturnSecurityContext(claims);
		CognitoUserPoolAuthorizerPrincipal principal = ((CognitoUserPoolAuthorizerPrincipal) sc.getUserPrincipal());

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

	private void filterWithClaimsAndVerifyNoSecurityContextSet(Map<String, Object> claims) {
		filterAndVerifyNoSecurityContextSet(Collections.singletonMap("claims", claims));
	}

	private SecurityContext filterWithClaimsAndReturnSecurityContext(Map<String, Object> claims) {
		return filterAndReturnSetSecurityContext(Collections.singletonMap("claims", claims));
	}

}
