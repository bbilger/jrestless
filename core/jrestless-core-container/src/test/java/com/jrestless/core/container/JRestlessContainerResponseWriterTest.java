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
package com.jrestless.core.container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ContainerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jrestless.core.container.JRestlessHandlerContainer.JRestlessContainerResponse;
import com.jrestless.core.container.JRestlessHandlerContainer.JRestlessContainerResponseWriter;
import com.jrestless.core.container.io.JRestlessResponseWriter;

public class JRestlessContainerResponseWriterTest {

	private JRestlessContainerResponseWriter containerResponseWriter;
	private JRestlessContainerResponse response;

	@BeforeEach
	public void setup() {
		JRestlessResponseWriter responseWriter = mock(JRestlessResponseWriter.class);
		when(responseWriter.getEntityOutputStream()).thenReturn(new ByteArrayOutputStream());
		response = spy(new JRestlessContainerResponse(responseWriter));
		containerResponseWriter = new JRestlessContainerResponseWriter(response);
	}

	@Test
	public void commit_ResponseNotYetClosed_ShouldCloseResponse() {
		containerResponseWriter.commit();
		verify(response, times(1)).close();
	}

	@Test
	public void writeResponseStatusAndHeaders_ContextHeaderAndStatusGiven_ShouldUpdateResponseStatusAndHeaders() {
		MultivaluedMap<String, String> actualHeaders = new MultivaluedHashMap<>();
		actualHeaders.add("header0", "value0_0");
		actualHeaders.add("header0", "value0_1");
		actualHeaders.add("header1", "value1_0");
		MultivaluedMap<String, String> expectedHeaders = new MultivaluedHashMap<>();
		expectedHeaders.add("header0", "value0_0");
		expectedHeaders.add("header0", "value0_1");
		expectedHeaders.add("header1", "value1_0");

		ContainerResponse context = mock(ContainerResponse.class);
		when(context.getStatusInfo()).thenReturn(Status.CONFLICT);
		when(context.getStringHeaders()).thenReturn(actualHeaders);

		containerResponseWriter.writeResponseStatusAndHeaders(-1, context);

		assertEquals(Status.CONFLICT, response.getStatusType());
		assertEquals(expectedHeaders, response.getHeaders());
	}

	@Test
	public void writeResponseStatusAndHeaders_ShouldReturnEntityOutputStreamOfResponse() {
		ContainerResponse context = mock(ContainerResponse.class);
		when(context.getStringHeaders()).thenReturn(new MultivaluedHashMap<>());
		when(context.getStatusInfo()).thenReturn(Status.OK);
		OutputStream entityOutputStream = containerResponseWriter.writeResponseStatusAndHeaders(-1, context);
		assertSame(response.getEntityOutputStream(), entityOutputStream);
	}

	@Test
	public void failure_ResponseNotYetCommitted_ShouldSetInternalServerErrorStatusOnFail() {
		ContainerResponse context = mock(ContainerResponse.class);
		when(context.getStatusInfo()).thenReturn(Status.OK);
		when(context.getStringHeaders()).thenReturn(new MultivaluedHashMap<>());
		containerResponseWriter.writeResponseStatusAndHeaders(-1, context);
		containerResponseWriter.failure(new RuntimeException());
		assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatusType());
	}

	@Test
	public void failure_ResponseNotYetCommitted_ShouldCommitOnFailure() {
		containerResponseWriter = spy(containerResponseWriter);
		containerResponseWriter.failure(new RuntimeException());
		verify(containerResponseWriter, times(1)).commit();
	}

	@Test
	public void failure_ResponseNotYetCommitted_ShouldRethrowOnCommitFailure() {
		containerResponseWriter = spy(containerResponseWriter);
		containerResponseWriter.failure(new RuntimeException());
		doThrow(CommitException.class).when(containerResponseWriter).commit();
		assertThrows(RuntimeException.class, () -> containerResponseWriter.failure(new RuntimeException()));
	}

	@Test
	public void enableResponseBuffering_Always_ShouldBeDisabled() {
		assertFalse(containerResponseWriter.enableResponseBuffering());
	}

	@Test
	public void setSuspendTimeout_Always_ShouldBeUnsupported() {
		assertThrows(UnsupportedOperationException.class, () -> containerResponseWriter.setSuspendTimeout(1, null));
	}

	@Test
	public void suspend_Always_ShouldBeUnsupported() {
		assertThrows(UnsupportedOperationException.class, () -> containerResponseWriter.suspend(1, null, null));
	}

	@SuppressWarnings("serial")
	private static class CommitException extends RuntimeException {
	}
}
