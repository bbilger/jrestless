package com.jrestless.openwhisk.webaction;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.jrestless.core.filter.ApplicationPathFilter;
import com.jrestless.openwhisk.webaction.io.WebActionBase64ReadInterceptor;
import com.jrestless.openwhisk.webaction.io.WebActionBase64WriteInterceptor;


public class WebActionHttpConfigTest {

	@Test
	public void testRegistersReadAndWriteInterceptor() {
		WebActionHttpConfig config = new WebActionHttpConfig();
		assertEquals(ImmutableSet.of(WebActionBase64ReadInterceptor.class, WebActionBase64WriteInterceptor.class,
				ApplicationPathFilter.class), config.getClasses());
		assertEquals(Collections.emptySet(), config.getInstances());
	}
}
