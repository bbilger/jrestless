package com.jrestless.openwhisk.webaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;
import com.jrestless.core.filter.ApplicationPathFilter;
import com.jrestless.openwhisk.webaction.io.WebActionBase64ReadInterceptor;

public class WebActionConfigTest {
	@Test
	public void testRegistersReadInterceptor() {
		WebActionConfig config = new WebActionConfig();
		assertEquals(ImmutableSet.of(WebActionBase64ReadInterceptor.class, ApplicationPathFilter.class),
				config.getClasses());
		assertEquals(Collections.emptySet(), config.getInstances());
	}
}
