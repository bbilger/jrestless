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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * AWS (API Gateway) authentication scheme constants.
 *
 * @author Bjoern Bilger
 *
 */
public final class AwsAuthenticationSchemes {

	public static final String AWS_CUSTOM_AUTHORIZER = "AWS_CUSTOM_AUTHORIZER";
	public static final String AWS_COGNITO_USER_POOL = "AWS_COGNITO_USER_POOL";
	public static final String AWS_IAM = "AWS_IAM";
	public static final String AWS_COGNITO_IDENTITY = "AWS_COGNITO_IDENTITY";

	public static final Collection<String> ALL_AWS_AUTHENTICATION_SCHEMES;
	static {
		List<String> schemes = new ArrayList<>();
		schemes.add(AWS_CUSTOM_AUTHORIZER);
		schemes.add(AWS_COGNITO_USER_POOL);
		schemes.add(AWS_IAM);
		schemes.add(AWS_COGNITO_IDENTITY);
		ALL_AWS_AUTHENTICATION_SCHEMES = Collections.unmodifiableCollection(schemes);
	}

	private AwsAuthenticationSchemes() {
	}
}
