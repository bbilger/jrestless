package com.jrestless.aws.service.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.jrestless.aws.service.client.LambdaServiceFunctionTarget;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.CopyConstructorEqualsTester;

import feign.RequestTemplate;
import feign.Target;

public class LambdaServiceFunctionTargetTest {

	@Test
	public void init_NoNameGiven_ShouldDeriveNameFromType() {
		Target<ApiInterface1> target = new LambdaServiceFunctionTarget<>(ApiInterface1.class);
		assertEquals("lambda service: ApiInterface1", target.name());
	}

	@Test
	public void apply_RequestTemplateGiven_ShouldInvokeRequestAndNothingElse() {
		Target<ApiInterface1> target = new LambdaServiceFunctionTarget<>(ApiInterface1.class);
		RequestTemplate tmpl = mock(RequestTemplate.class);
		target.apply(tmpl);
		verify(tmpl, times(1)).request();
		verifyNoMoreInteractions(tmpl);
	}

	@Test
	public void testGetters() {
		Target<ApiInterface1> target1 = new LambdaServiceFunctionTarget<>(ApiInterface1.class, "a");
		assertEquals("a", target1.name());
		assertEquals(ApiInterface1.class, target1.type());
		assertEquals("", target1.url());

		Target<ApiInterface2> target2 = new LambdaServiceFunctionTarget<>(ApiInterface2.class, "b");
		assertEquals("b", target2.name());
		assertEquals(ApiInterface2.class, target2.type());
		assertEquals("", target2.url());
	}

	@Test
	public void testPreconditions() {
		new ConstructorPreconditionsTester(getConstructor())
			.addValidArgs(0, ApiInterface1.class)
			.addInvalidNpeArg(0)
			.addValidArgs(1, "some name")
			.addInvalidNpeArg(1)
			.testPreconditions();
	}

	@Test
	public void testEquality() {
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, ApiInterface1.class, ApiInterface2.class)
			.addArguments(1, "a", "b")
			.testEquals();
	}

	@SuppressWarnings("rawtypes")
	private Constructor<LambdaServiceFunctionTarget> getConstructor() {
		try {
			return LambdaServiceFunctionTarget.class.getConstructor(Class.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private interface ApiInterface1 {
	}

	private interface ApiInterface2 {
	}
}
