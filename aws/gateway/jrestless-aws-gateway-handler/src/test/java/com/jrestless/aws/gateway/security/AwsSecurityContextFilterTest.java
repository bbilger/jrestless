package com.jrestless.aws.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.CognitoIdentityPrincipal;
import com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal;
import com.jrestless.aws.security.CustomAuthorizerPrincipal;
import com.jrestless.aws.security.IamPrincipal;

public class AwsSecurityContextFilterTest {

	private static final String TEST_IAM_ACCESS_KEY = "iamAccessKey";
	private static final String TEST_IAM_USER = "iamUser";
	private static final String TEST_IAM_USER_ARN = "iamUserArn";

	private static final String TEST_COGNITO_IDENTITY_AUTH_TYPE = "cognitoIdentityAuthenticationType";
	private static final String TEST_COGNITO_IDENTITY_IDENTITY_ID = "cognitoIdentityId";

	private static final String TEST_CUSTOM_AUTHORIZER_PRINCIPAL_ID = "customAuthorizerPrincipalId";

	private static final String TEST_COGNITO_USER_POOL_SUB = "cognitoUserPoolSub";

	@Test
	public void filter_ValidIamGivenAndAllowed_ShouldSetScWithIamPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertIamPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_InvalidIamGivenAndAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareIamRequest(false)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_ValidIamGivenAndDisallowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareIamRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_IAM);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertNull(sc.getUserPrincipal());
	}

	private static void assertIamPrincipal(Principal principal) {
		IamPrincipal iamPrincipal = (IamPrincipal) principal;
		assertEquals(TEST_IAM_ACCESS_KEY, iamPrincipal.getAccessKey());
		assertEquals(TEST_IAM_USER, iamPrincipal.getUser());
		assertEquals(TEST_IAM_USER_ARN, iamPrincipal.getUserArn());
	}

	@Test
	public void filter_ValidCogIdentityGivenAndAllowed_ShouldSetScWithCogIdentityPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCognitoIdentityPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_InvalidCogIdentityGivenAndAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(false)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_ValidCogIdentityGivenAndDisallowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_COGNITO_IDENTITY);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertNull(sc.getUserPrincipal());
	}

	private static void assertCognitoIdentityPrincipal(Principal principal) {
		CognitoIdentityPrincipal cognitoIdentityPrincipal = (CognitoIdentityPrincipal) principal;
		assertEquals(TEST_COGNITO_IDENTITY_AUTH_TYPE, cognitoIdentityPrincipal.getCognitoAuthenticationType());
		assertEquals(TEST_COGNITO_IDENTITY_IDENTITY_ID, cognitoIdentityPrincipal.getCognitoIdentityId());
	}

	@Test
	public void filter_ValidCustomAuthGivenAndAllowed_ShouldSetScWithCustomAuthPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCustomAuthorizerPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_InvalidCustomAuthGivenAndAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(false)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_ValidCustomAuthGivenAndDisallowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_CUSTOM_AUTHORIZER);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertNull(sc.getUserPrincipal());
	}

	private static void assertCustomAuthorizerPrincipal(Principal principal) {
		CustomAuthorizerPrincipal customAuthorizerPrincipal = (CustomAuthorizerPrincipal) principal;
		assertEquals(TEST_CUSTOM_AUTHORIZER_PRINCIPAL_ID, customAuthorizerPrincipal.getClaims().getPrincipalId());
		assertEquals(TEST_CUSTOM_AUTHORIZER_PRINCIPAL_ID, customAuthorizerPrincipal.getClaims().getAllClaims().get("principalId"));
	}

	@Test
	public void filter_ValidCogUserPoolGivenAndAllowed_ShouldSetScWithCogUserPoolPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCognitoUserPoolPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_InvalidCogUserPoolGivenAndAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(false)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_ValidCogUserPoolGivenAndDisallowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_COGNITO_USER_POOL);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertNull(sc.getUserPrincipal());
	}

	private static void assertCognitoUserPoolPrincipal(Principal principal) {
		CognitoUserPoolAuthorizerPrincipal customAuthorizerPrincipal = (CognitoUserPoolAuthorizerPrincipal) principal;
		assertEquals(TEST_COGNITO_USER_POOL_SUB, customAuthorizerPrincipal.getClaims().getSub());
		assertEquals(TEST_COGNITO_USER_POOL_SUB, customAuthorizerPrincipal.getClaims().getAllClaims().get("sub"));
	}

	@Test
	public void filter_AllAuthsPossibleAndAllAllowed_ShouldSetScWithCogIdentityPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(true)
				.prepareCustomAuthorizerRequest(true)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCognitoIdentityPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_AllAuthsPossibleButCogIdentityInvalidAndAllAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(false)
				.prepareCustomAuthorizerRequest(true)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_AllAuthsPossibleAndAllAllowedButCognitoIdentity_ShouldSetScWithCustomAuthPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoIdentityRequest(true)
				.prepareCustomAuthorizerRequest(true)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_COGNITO_IDENTITY);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertCustomAuthorizerPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_AllAuthsButCogIdentityPossibleAndAllAllowed_ShouldSetScWithCustomAuthPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(true)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCustomAuthorizerPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_AllAuthsButCogIdentityPossibleAndCustomAuthInvalidAndAllAllowed_ShouldSetScNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(false)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_AllAuthsButCogIdentityPossibleAndAllAllowedButCustomAuth_ShouldSetScWithCogUserPoolPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCustomAuthorizerRequest(true)
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_CUSTOM_AUTHORIZER);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertCognitoUserPoolPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_CogUserPoolAndIamPossibleAndAllAllowed_ShouldSetScWithCogUserPoolPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertCognitoUserPoolPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_CogUserPoolAndIamPossibleAndCogUserPoolInvalidAndAllAllowed_ShouldSetScWithNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(false)
				.prepareIamRequest(true)
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	@Test
	public void filter_CogUserPoolAndIamPossibleAndAllAllowedButCogUserPool_ShouldSetScWithIamPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(true)
				.prepareIamRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_COGNITO_USER_POOL);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertIamPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_CogUserPoolAndIamPossibleAndCogUserPoolInvalidAndAllAllowedButCogUserPool_ShouldSetScWithIamPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.prepareCognitoUserPoolRequest(false)
				.prepareIamRequest(true)
				.build();
		List<String> allowedAuthSchemes = new ArrayList<>(AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES);
		allowedAuthSchemes.remove(AwsAuthenticationSchemes.AWS_COGNITO_USER_POOL);
		SecurityContext sc = filter(request, allowedAuthSchemes);
		assertIamPrincipal(sc.getUserPrincipal());
	}

	@Test
	public void filter_NonePossibleAndAllAllowed_ShouldSetScNoPrincipal() {
		GatewayRequest request = new GatewayRequestBuilder()
				.build();
		SecurityContext sc = filter(request);
		assertNull(sc.getUserPrincipal());
	}

	private static SecurityContext filter(GatewayRequest request, List<String> allowedAuthenticationSchemes) {
		return filter(request, new AwsSecurityContextFilter(allowedAuthenticationSchemes));
	}

	private static SecurityContext filter(GatewayRequest request) {
		return filter(request, new AwsSecurityContextFilter());
	}

	private static SecurityContext filter(GatewayRequest request, AwsSecurityContextFilter filter) {
		filter.setGatewayRequest(request);
		ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		ArgumentCaptor<SecurityContext> securityContextCapture = ArgumentCaptor.forClass(SecurityContext.class);
		try {
			filter.filter(containerRequestContext);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		verify(containerRequestContext).setSecurityContext(securityContextCapture.capture());
		return securityContextCapture.getValue();
	}

	private static class GatewayRequestBuilder {
		private RequestType iamRequestType;
		private RequestType customAuthorizerRequestType;
		private RequestType cognitoIdentityRequestType;
		private RequestType cognitoUserPoolRequestType;

		public GatewayRequestBuilder prepareIamRequest(boolean valid) {
			iamRequestType = valid ? RequestType.APPLICABLE_VALID : RequestType.APPLICABLE_INVALID;
			return this;
		}

		public GatewayRequestBuilder prepareCustomAuthorizerRequest(boolean valid) {
			customAuthorizerRequestType = valid ? RequestType.APPLICABLE_VALID : RequestType.APPLICABLE_INVALID;
			return this;
		}

		public GatewayRequestBuilder prepareCognitoUserPoolRequest(boolean valid) {
			cognitoUserPoolRequestType = valid ? RequestType.APPLICABLE_VALID : RequestType.APPLICABLE_INVALID;
			return this;
		}

		public GatewayRequestBuilder prepareCognitoIdentityRequest(boolean valid) {
			cognitoIdentityRequestType = valid ? RequestType.APPLICABLE_VALID : RequestType.APPLICABLE_INVALID;
			return this;
		}

		private void mockIamRequest(GatewayIdentity identity) {
			if (iamRequestType == null) {
				return;
			}
			when(identity.getAccessKey()).thenReturn(TEST_IAM_ACCESS_KEY);
			if (RequestType.APPLICABLE_VALID.equals(iamRequestType)) {
				when(identity.getUserArn()).thenReturn(TEST_IAM_USER_ARN);
				when(identity.getUser()).thenReturn(TEST_IAM_USER);
			}
		}

		private void mockCognitoIdentityRequest(GatewayIdentity identity) {
			if (cognitoIdentityRequestType == null) {
				return;
			}
			when(identity.getCognitoAuthenticationType()).thenReturn(TEST_COGNITO_IDENTITY_AUTH_TYPE);
			if (RequestType.APPLICABLE_VALID.equals(cognitoIdentityRequestType)) {
				when(identity.getCognitoIdentityId()).thenReturn(TEST_COGNITO_IDENTITY_IDENTITY_ID);
			}
		}

		private void mockCustomAuthorizerRequest(Map<String, Object> authorizerData) {
			if (customAuthorizerRequestType == null) {
				return;
			}
			if (RequestType.APPLICABLE_VALID.equals(customAuthorizerRequestType)) {
				authorizerData.put("principalId", TEST_CUSTOM_AUTHORIZER_PRINCIPAL_ID);
			} else {
				authorizerData.put("principalId", 1);
			}
		}

		private void mockCognitoUserPoolRequest(Map<String, Object> authorizerData) {
			if (cognitoUserPoolRequestType == null) {
				return;
			}
			Map<String, Object> claims = new HashMap<>();
			authorizerData.put("claims", claims);
			if (RequestType.APPLICABLE_VALID.equals(cognitoUserPoolRequestType)) {
				claims.put("sub", TEST_COGNITO_USER_POOL_SUB);
			}
		}

		public GatewayRequest build() {
			GatewayRequest request = mock(GatewayRequest.class);
			GatewayRequestContext context = null;
			GatewayIdentity identity = null;
			Map<String, Object> authorizerData = null;
			if (iamRequestType != null || customAuthorizerRequestType != null || cognitoIdentityRequestType != null
					|| cognitoUserPoolRequestType != null) {
				context = mock(GatewayRequestContext.class);
				when(request.getRequestContext()).thenReturn(context);
				if (iamRequestType != null || cognitoIdentityRequestType != null) {
					identity = mock(GatewayIdentity.class);
					when(context.getIdentity()).thenReturn(identity);
				}
				if (customAuthorizerRequestType != null || cognitoUserPoolRequestType != null) {
					authorizerData = new HashMap<>();
					when(context.getAuthorizer()).thenReturn(authorizerData);
				}
			}
			mockIamRequest(identity);
			mockCognitoIdentityRequest(identity);
			mockCustomAuthorizerRequest(authorizerData);
			mockCognitoUserPoolRequest(authorizerData);
			return request;
		}

		private static enum RequestType {
			APPLICABLE_VALID,
			APPLICABLE_INVALID;
		}
	}
}
