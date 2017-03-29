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
package com.jrestless.aws.security;

import java.security.Principal;

import javax.annotation.Nonnull;

/**
 * User authenticated by IAM.
 *
 * @author Bjoern Bilger
 *
 */
public interface IamPrincipal extends Principal {

	/**
	 * Returns
	 * {@link com.jrestless.aws.gateway.io.GatewayIdentity#getUserArn()}.
	 *
	 * @return userArn
	 */
	@Nonnull
	String getUserArn();

	/**
	 * Returns {@link com.jrestless.aws.gateway.io.GatewayIdentity#getUser()}.
	 *
	 * @return user
	 */
	@Nonnull
	String getUser();

	/**
	 * Returns
	 * {@link com.jrestless.aws.gateway.io.GatewayIdentity#getAccessKey()}.
	 *
	 * @return accessKey
	 */
	@Nonnull
	String getAccessKey();

	/**
	 * Returns {@link com.jrestless.aws.gateway.io.GatewayIdentity#getCaller()}.
	 *
	 * @return caller
	 */
	String getCaller();

	/**
	 * Returns {@link #getUserArn()}.
	 *
	 * @return name
	 */
	@Override
	default String getName() {
		return getUserArn();
	}
}
