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
package com.jrestless.aws.io;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;

public class GatewayAdditionalResponseExceptionTest {

	@Test
	public void init_StatusGiven_MsgShouldContainStatusCodeOnly() {
		assertEquals("{\"statusCode\":\"200\"}", new GatewayAdditionalResponseException(null, Status.OK).getMessage());
	}

	@Test
	public void init_TextBodyAndStatusCodeGiven_MsgShouldContainBodyAndStatusCode() {
		assertEquals("{\"statusCode\":\"200\",\"body\":\"te\\n\\\"st\"}",
				new GatewayAdditionalResponseException("te\n\"st", Status.OK).getMessage());
	}

	@Test
	public void init_ValidJsonBodyAndStatusCodeGiven_MsgShouldContainEscapedBodyAndStatusCode() {
		assertEquals("{\"statusCode\":\"200\",\"body\":\"{\\\"prop0\\\":1,\\n\\\"prop1\\\":\\\"value\\\"}\"}",
				new GatewayAdditionalResponseException("{\"prop0\":1,\n\"prop1\":\"value\"}", Status.OK).getMessage());
	}

	@Test
	public void init_InvalidJsonBodyAndStatusCodeGiven_MsgShouldContainEscapedBodyAndStatusCode() {
		assertEquals("{\"statusCode\":\"200\",\"body\":\"{\"}",
				new GatewayAdditionalResponseException("{", Status.OK).getMessage());
	}

	@Test
	public void init_ValidXmlBodyAndStatusCodeGiven_MsgShouldContainEscapedBodyAndStatusCode() {
		assertEquals(
				"{\"statusCode\":\"200\",\"body\":\"<prop0 attr1=\\\"a\\\">val0<\\/prop0>\\n<prop1>val1<\\/prop1>\"}",
				new GatewayAdditionalResponseException("<prop0 attr1=\"a\">val0</prop0>\n<prop1>val1</prop1>",
						Status.OK).getMessage());
	}

	@Test
	public void init_InvalidXmlBodyAndStatusCodeGiven_MsgShouldContainEscapedBodyAndStatusCode() {
		assertEquals("{\"statusCode\":\"200\",\"body\":\"<prop\"}",
				new GatewayAdditionalResponseException("<prop", Status.OK).getMessage());
	}
}
