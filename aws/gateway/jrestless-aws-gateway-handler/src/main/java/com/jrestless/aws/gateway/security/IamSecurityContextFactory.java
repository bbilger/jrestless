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

import java.security.Principal;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.security.AwsAuthenticationSchemes;
import com.jrestless.aws.security.IamPrincipal;

final class IamSecurityContextFactory extends AbstractSecurityContextFactory {

	private static final Logger LOG = LoggerFactory.getLogger(IamSecurityContextFactory.class);

	protected IamSecurityContextFactory(@Nonnull GatewayRequest request) {
		super(request, AwsAuthenticationSchemes.AWS_IAM);
	}

	@Override
	protected boolean isApplicable() {
		GatewayIdentity identity = getIdentitySafe();
		if (identity == null) {
			return false;
		}
		return identity.getAccessKey() != null;
	}

	@Override
	protected boolean isValid() {
		GatewayIdentity identity = getIdentitySafe();
		String userArn = identity.getUserArn();
		String user = identity.getUser();
		if (userArn == null) {
			LOG.debug("userArn may not be null");
			return false;
		} else if (user == null) {
			LOG.debug("user may not be null");
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected Principal createPrincipal() {
		GatewayIdentity identity = getIdentitySafe();
		String userArn = identity.getUserArn();
		String user = identity.getUser();
		String accessKey = identity.getAccessKey();
		String caller = identity.getCaller();
		return new IamPrincipal() {
			@Override
			public String getUser() {
				return user;
			}
			@Override
			public String getUserArn() {
				return userArn;
			}
			@Override
			public String getAccessKey() {
				return accessKey;
			}
			@Override
			public String getCaller() {
				return caller;
			}
		};
	}
}
