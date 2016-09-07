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

import static com.jrestless.aws.swagger.util.HeaderUtils.asStaticValue;
import static com.jrestless.aws.swagger.util.HeaderUtils.getDefaultDynamicHeaderIntegrationExpression;
import static com.jrestless.aws.swagger.util.HeaderUtils.getDynamicHeaderIntegrationExpression;
import static com.jrestless.aws.swagger.util.HeaderUtils.getNonDefaultDynamicHeaderIntegrationExpression;
import static com.jrestless.aws.swagger.util.HeaderUtils.isDynamicValue;
import static com.jrestless.aws.swagger.util.HeaderUtils.isStaticValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.jrestless.aws.swagger.util.HeaderUtils.DynamicNonDefaultHeaderNotSupportedException;

public class HeaderUtilsTest {

	@Test(expected = NullPointerException.class)
	public void asStaticValue_NullGiven_ShouldThrowNpe() {
		asStaticValue(null);
	}

	@Test
	public void asStaticValue_NonNullGiven_ShouldPrependAndAppendSingleQuote() {
		assertEquals("'value'", asStaticValue("value"));
	}

	@Test
	public void isStaticValue_StaticValueGiven_ShouldBeStatic() {
		assertTrue(isStaticValue("'value'"));
	}

	@Test
	public void isStaticValue_UntrimmedStaticValueGiven0_ShouldNotBeStatic() {
		assertFalse(isStaticValue(" 'value'"));
	}

	@Test
	public void isStaticValue_UntrimmedStaticValueGiven1_ShouldNotBeStatic() {
		assertFalse(isStaticValue("'value' "));
	}

	@Test
	public void isStaticValue_UntrimmedStaticValueGiven2_ShouldNotBeStatic() {
		assertFalse(isStaticValue(" 'value' "));
	}

	@Test
	public void isStaticValue_UnbalanceStaticValueGiven0_ShouldNotBeStatic() {
		assertFalse(isStaticValue("'value"));
	}

	@Test
	public void isStaticValue_UnbalanceStaticValueGiven1_ShouldNotBeStatic() {
		assertFalse(isStaticValue("value'"));
	}

	@Test
	public void isStaticValue_NullValueGiven_ShouldNotBeStatic() {
		assertFalse(isStaticValue(null));
	}

	@Test
	public void isDynamicHeaderValue_StaticHeaderValueGiven_ShouldNotBeDynamic() {
		assertFalse(isDynamicValue("'test'"));
	}

	@Test
	public void isDynamicHeaderValue_DynamicHeaderValueGiven_ShouldBeDynamic() {
		assertTrue(isDynamicValue("integration.response.body.test"));
	}

	@Test
	public void isDynamicHeaderValue_NonStaticAndNonDynamicHeaderValueGiven_ShouldNotBeDynamic() {
		assertFalse(isDynamicValue("test"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNonDefaultHeaderExpression_NegativeIndexGiven_ShouldFail() {
		getNonDefaultDynamicHeaderIntegrationExpression(-1);
	}

	@Test
	public void getNonDefaultDynamicHeaderExpression_ZeroIndexGiven_ShouldCreateWithFirstElementInChain() {
		String headerExpression = getNonDefaultDynamicHeaderIntegrationExpression(0);
		assertEquals("integration.response.body.cause.errorMessage", headerExpression);
	}

	@Test
	public void getNonDefaultDynamicHeaderExpression_OneIndexGiven_ShouldCreateWithSecondElementInChain() {
		String headerExpression = getNonDefaultDynamicHeaderIntegrationExpression(1);
		assertEquals("integration.response.body.cause.cause.errorMessage", headerExpression);
	}

	@Test(expected = NullPointerException.class)
	public void getDefaultDynamicHeaderIntegrationExpression_NoHeaderValueGiven_ShouldFail() {
		getDefaultDynamicHeaderIntegrationExpression(null);
	}

	@Test
	public void getDefaultDynamicHeaderIntegrationExpression_HeaderValueGiven_ShouldCreate() {
		String headerExpression = getDefaultDynamicHeaderIntegrationExpression("someHeaderName");
		assertEquals("integration.response.body.headers.someHeaderName", headerExpression);
	}

	@Test(expected = NullPointerException.class)
	public void getDynamicHeaderIntegrationExpression_Default_NoHeaderValueGiven_ShouldFail() {
		getDynamicHeaderIntegrationExpression(null, true, null);
	}

	@Test
	public void getDynamicHeaderIntegrationExpression_Default_HeaderGiven_ShouldFail() {
		String headerExpression = getDynamicHeaderIntegrationExpression("someHeaderName", true, null);
		assertEquals("integration.response.body.headers.someHeaderName", headerExpression);
	}

	@Test(expected = NullPointerException.class)
	public void getDynamicHeaderIntegrationExpression_NonDefault_NoHeaderValueGiven_ShouldFail() {
		getDynamicHeaderIntegrationExpression(null, false, null);
	}

	@Test(expected = DynamicNonDefaultHeaderNotSupportedException.class)
	public void getDynamicHeaderIntegrationExpression_NonDefault_NoSupportedHeaderGiven_ShouldFail() {
		getDynamicHeaderIntegrationExpression("someHeaderName", false, null);
	}

	@Test(expected = DynamicNonDefaultHeaderNotSupportedException.class)
	public void getDynamicHeaderIntegrationExpression_NonDefault_EmptySupportedHeaderGiven_ShouldFail() {
		getDynamicHeaderIntegrationExpression("someHeaderName", false, new String[0]);
	}

	@Test(expected = DynamicNonDefaultHeaderNotSupportedException.class)
	public void getDynamicHeaderIntegrationExpression_NonDefault_UnsupportedHeaderGiven_ShouldFail() {
		getDynamicHeaderIntegrationExpression("someHeaderName", false, new String[] { "what", "eveŕ" });
	}

	@Test
	public void getDynamicHeaderIntegrationExpression_NonDefault_MissingHeaderGiven_ShouldFailWithExceptionContainingHeaderName() {
		try {
			getDynamicHeaderIntegrationExpression("someHeaderName", false, null);
			fail("expected 'DynamicNonDefaultHeaderNotSupportedException' to be thrown");
		} catch (DynamicNonDefaultHeaderNotSupportedException e) {
			assertEquals("someHeaderName", e.getHeaderName());
		}
	}

	@Test
	public void getDynamicHeaderIntegrationExpression_NonDefault_SupportedHeaderAtIndex0Given_ShouldFail() {
		String headerExpression = getDynamicHeaderIntegrationExpression("someHeaderName", false,
				new String[] { "someHeaderName", "someOtherHeader" });
		assertEquals("integration.response.body.cause.errorMessage", headerExpression);
	}

	@Test
	public void getDynamicHeaderIntegrationExpression_NonDefault_SupportedHeaderAtIndex1Given_ShouldFail() {
		String headerExpression = getDynamicHeaderIntegrationExpression("someHeaderName", false,
				new String[] { "someOtherHeader", "someHeaderName" });
		assertEquals("integration.response.body.cause.cause.errorMessage", headerExpression);
	}
}
