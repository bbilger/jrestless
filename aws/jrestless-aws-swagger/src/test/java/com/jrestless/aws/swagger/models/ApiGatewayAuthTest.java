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
package com.jrestless.aws.swagger.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.testing.EqualsTester;
import com.jrestless.aws.swagger.models.ApiGatewayAuth.AuthType;

public class ApiGatewayAuthTest {
	@Test
	public void instantiate_NoArgs_ShouldSetTypeToIam() {
		assertEquals(AuthType.aws_iam, new ApiGatewayAuth().getType());
	}

	@Test(expected = NullPointerException.class)
	public void instantiate_NullAuthType_ShouldThrowNpe() {
		new ApiGatewayAuth(null);
	}

	@Test
	public void instantiate_NoAuthType_ShouldSetTypeToGiven() {
		assertEquals(AuthType.none, new ApiGatewayAuth(AuthType.none).getType());
	}

	@Test
	public void testEquals() {
		new EqualsTester()
			.addEqualityGroup(new ApiGatewayAuth(AuthType.none))
			.addEqualityGroup(new ApiGatewayAuth(AuthType.aws_iam))
			.testEquals();
	}
}
