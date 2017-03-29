/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.aws.gateway.security;

import static com.jrestless.aws.security.AwsAuthenticationSchemes.ALL_AWS_AUTHENTICATION_SCHEMES;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.core.security.AnonSecurityContext;

/**
 * AWS security filter that updates the
 * {@link ContainerRequestContext#setSecurityContext(SecurityContext) container
 * request's} {@link SecurityContext}. The {@link SecurityContext security
 * context's} {@link java.security.Principal principal} depends on how a user
 * authenticated against the API Gateway.
 * <p>
 * The filter supports 5 authorization types and tries to detect them in the following order:
 * <table summary="authorization types" border="1">
 *   <tr>
 *     <th>Authentication Type</th>
 *     <th>Authentication Scheme</th>
 *     <th>Principal</th>
 *   </tr>
 *   <tr>
 *     <td>Cognito Identity</td>
 *     <td>{@link com.jrestless.aws.security.AwsAuthenticationSchemes#AWS_COGNITO_IDENTITY AWS_COGNITO_IDENTITY}</td>
 *     <td>{@link com.jrestless.aws.security.CognitoIdentityPrincipal CognitoIdentityPrincipal}</td>
 *   </tr>
 *   <tr>
 *     <td>Custom Authorizater</td>
 *     <td>{@link com.jrestless.aws.security.AwsAuthenticationSchemes#AWS_CUSTOM_AUTHORIZER AWS_CUSTOM_AUTHORIZER}</td>
 *     <td>{@link com.jrestless.aws.security.CustomAuthorizerPrincipal CustomAuthorizerPrincipal}</td>
 *   </tr>
 *   <tr>
 *     <td>Cognito User Pool</td>
 *     <td>{@link com.jrestless.aws.security.AwsAuthenticationSchemes#AWS_COGNITO_USER_POOL AWS_COGNITO_USER_POOL}</td>
 *     <td>{@link com.jrestless.aws.security.CognitoUserPoolAuthorizerPrincipal CognitoUserPoolAuthorizerPrincipal}</td>
 *   </tr>
 *   <tr>
 *     <td>IAM</td>
 *     <td>{@link com.jrestless.aws.security.AwsAuthenticationSchemes#AWS_IAM AWS_IAM}</td>
 *     <td>{@link com.jrestless.aws.security.IamPrincipal IamPrincipal}</td>
 *   </tr>
 *   <tr>
 *     <td>None</td>
 *     <td>{@code null}</td>
 *     <td>{@code null}</td>
 *   </tr>
 * </table>
 * <p>
 * None of the {@link SecurityContext security contexts} implements {@link SecurityContext#isUserInRole(String)}
 * in a meaningful way and will return {@code false}, always. If you want to make use of "roles", then extend
 * this class and wrap around the the created security context or introduce another filter.
 * <p>
 * In contrast to "Cognito Identity" and "IAM" authorization, the principal for "Custom Authorizer"
 * and "Cognito User Pool" can only be injected when an endpoint is secured with such an authorizer.
 * This means that even when you send an authorization header to an unprotected endpoint this information won't be
 * passed to the request object. If you require this information, then you need to "resolve" the authorization header
 * (JWT token) manually with another filter.
 *
 *
 * @author Bjoern Bilger
 *
 */
@Priority(Priorities.AUTHORIZATION)
public class AwsSecurityContextFilter implements ContainerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ContainerRequestFilter.class);

	private final Collection<String> allowedAuthenticationSchemes;

	private GatewayRequest gatewayRequest;

	/**
	 * Creates a new filter with all authentication schemes allowed.
	 */
	public AwsSecurityContextFilter() {
		this(ALL_AWS_AUTHENTICATION_SCHEMES);
	}

	/**
	 * Creates a new filter and allows only the passed authentication schemes to
	 * be used.
	 * <p>
	 * A possible use case for disallowing certain authentication schemes is
	 * when your users authenticate using "Cognito Identity" but having the
	 * mapped IAM role information is enough for you.
	 *
	 * @param allowedAuthenticationSchemes
	 */
	public AwsSecurityContextFilter(Collection<String> allowedAuthenticationSchemes) {
		this.allowedAuthenticationSchemes = Collections
				.unmodifiableCollection(new ArrayList<>(allowedAuthenticationSchemes));
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		requestContext.setSecurityContext(createSecurityContext());
	}

	protected SecurityContext createSecurityContext() {
		GatewayRequest request = getGatewayRequest();
		AbstractSecurityContextFactory[] securityContextFactories = new AbstractSecurityContextFactory[] {
			new CognitoIdentitySecurityContextFactory(request),
			new CustomAuthorizerSecurityContextFactory(request),
			new CognitoUserPoolSecurityContextFactory(request),
			new IamSecurityContextFactory(request)
		};

		AbstractSecurityContextFactory selectedFactory = null;
		for (AbstractSecurityContextFactory factory : securityContextFactories) {
			boolean applicable = factory.isApplicable();
			boolean allowed = getAllowedAuthenticationSchemes().contains(factory.getAuthenticationScheme());
			if (applicable) {
				if (allowed) {
					selectedFactory = factory;
					break;
				} else {
					LOG.debug("found matching but disallowed authentication scheme {}",
							factory.getAuthenticationScheme());
				}
			}
		}

		if (selectedFactory == null) {
			return new AnonSecurityContext();
		} else {
			return selectedFactory.createSecurityContext();
		}
	}

	@Context
	void setGatewayRequest(GatewayRequest gatewayRequest) {
		this.gatewayRequest = gatewayRequest;
	}

	protected GatewayRequest getGatewayRequest() {
		return gatewayRequest;
	}

	protected Collection<String> getAllowedAuthenticationSchemes() {
		return allowedAuthenticationSchemes;
	}
}
