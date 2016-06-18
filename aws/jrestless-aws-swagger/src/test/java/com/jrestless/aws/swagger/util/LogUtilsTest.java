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

import static com.jrestless.aws.swagger.util.LogUtils.createLogIdentifier;
import static com.jrestless.aws.swagger.util.LogUtils.logAndReturn;
import static com.jrestless.aws.swagger.util.LogUtils.logOnSupply;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.Test;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.jrestless.aws.swagger.OperationContext;

import io.swagger.models.Operation;

public class LogUtilsTest {

	@Test
	public void logOnSupply_OnRequest_ShouldConsumeMsg() {
		@SuppressWarnings("unchecked")
		Consumer<String> logMethod = mock(Consumer.class);
		logOnSupply("msg", logMethod).get();
		verify(logMethod, times(1)).accept("msg");
	}

	@Test
	public void logOnSupply_NoRequest_ShouldNotConsumeMsg() {
		@SuppressWarnings("unchecked")
		Consumer<String> logMethod = mock(Consumer.class);
		logOnSupply("msg", logMethod);
		verifyZeroInteractions(logMethod);
	}

	@Test
	public void logOnSupply_OnRequest_ShouldLogOnError() {
		LogAdapter log = mock(LogAdapter.class);
		logOnSupply("msg", log).get();
		verify(log, times(1)).error("msg");
	}

	@Test
	public void logOnSupply_NoRequest_ShouldNotLogToError() {
		LogAdapter log = mock(LogAdapter.class);
		logOnSupply("msg", log);
		verifyZeroInteractions(log);
	}

	@Test
	public void logAndReturn_ConsumerAndMsgGiven_ShouldConsumeMsg() {
		@SuppressWarnings("unchecked")
		Consumer<String> logMethod = mock(Consumer.class);
		logAndReturn("msg", logMethod);
		verify(logMethod, times(1)).accept("msg");
	}

	@Test
	public void logAndReturn_ConsumerAndMsgGiven_ShouldReturnsMsg() {
		@SuppressWarnings("unchecked")
		Consumer<String> logMethod = mock(Consumer.class);
		assertEquals("msg", logAndReturn("msg", logMethod));
	}

	@Test
	public void logAndReturn_LogAndMsgGiven_ShouldLogMsgToError() {
		LogAdapter log = mock(LogAdapter.class);
		logAndReturn("msg", log);
		verify(log, times(1)).error("msg");
	}

	@Test
	public void logAndReturn_LogAndMsgGiven_ShouldReturnsMsg() {
		LogAdapter log = mock(LogAdapter.class);
		assertEquals("msg", logAndReturn("msg", log));
	}

	@Test
	public void createLogIdentifier_ContextGiven_ShouldReturnEndpointIdentifier() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Operation operation = mock(Operation.class);
		when(context.getEndpointMethod()).thenReturn(SomeResource.class.getMethod("someEndpoint"));
		when(operation.getOperationId()).thenReturn("someId");
		when(context.getOperation()).thenReturn(operation);
		assertEquals("[operationId='someId' resourceClass='SomeResource' endpointMethod='someEndpoint']", createLogIdentifier(context));
	}

	@Test
	public void createLogIdentifier_ContextAndAdditionalValuesGiven_ShouldReturnExtendedEndpointIdentifier() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Operation operation = mock(Operation.class);
		when(context.getEndpointMethod()).thenReturn(SomeResource.class.getMethod("someEndpoint"));
		when(operation.getOperationId()).thenReturn("someId");
		when(context.getOperation()).thenReturn(operation);
		assertEquals("[operationId='someId' resourceClass='SomeResource' endpointMethod='someEndpoint' a='b' c='d']", createLogIdentifier(context, "a='b'", "c='d'"));
	}

	private static class SomeResource {
		@SuppressWarnings("unused")
		public void someEndpoint() {
			;
		}
	}
}
