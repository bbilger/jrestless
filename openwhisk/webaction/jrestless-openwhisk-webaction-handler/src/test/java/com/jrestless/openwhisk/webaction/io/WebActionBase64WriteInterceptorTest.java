package com.jrestless.openwhisk.webaction.io;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

public class WebActionBase64WriteInterceptorTest {

	private final WebActionBase64WriteInterceptor writeInterceptor = new WebActionBase64WriteInterceptor();

    public static Stream<Arguments> data() {
    	return Stream.of(
			Arguments.of("text/plain", false),
			Arguments.of("text/html", false),
			Arguments.of("application/json", true),
			Arguments.of("image/png", true),
			Arguments.of("whatever/whatever", true),
			Arguments.of(null, true)
		);
    };

	@ParameterizedTest
	@MethodSource("data")
	public void test(String contentType, boolean base64Encoded) throws WebApplicationException, IOException {
		if (base64Encoded) {
			testWrapsInputStream(contentType);
		} else {
			testDoesNotWrapInputStream(contentType);
		}
	}

	public void testDoesNotWrapInputStream(String contentType) throws WebApplicationException, IOException {
		WriterInterceptorContext context = mockContext(contentType);
		OutputStream os = mock(OutputStream.class);
		when(context.getOutputStream()).thenReturn(os);

		writeInterceptor.aroundWriteTo(context);

		verifyZeroInteractions(os);

		verify(context).getMediaType();
		verify(context).proceed();
		verifyNoMoreInteractions(context);
	}

	public void testWrapsInputStream(String contentType) throws WebApplicationException, IOException {
		WriterInterceptorContext context = mockContext(contentType);
		OutputStream os = mock(OutputStream.class);
		when(context.getOutputStream()).thenReturn(os);

		writeInterceptor.aroundWriteTo(context);

		verifyZeroInteractions(os);

		ArgumentCaptor<OutputStream> updatedOsCapture = ArgumentCaptor.forClass(OutputStream.class);
		verify(context).setOutputStream(updatedOsCapture.capture());
		verify(context).getMediaType();
		verify(context).getOutputStream();
		verify(context).proceed();
		verifyNoMoreInteractions(context);

		OutputStream updatedOs = updatedOsCapture.getValue();

		// just make sure we have some wrapper
		assertNotSame(os, updatedOs);
		updatedOs.close();
		verify(os).close();
	}

	private static WriterInterceptorContext mockContext(String contentType) {
		WriterInterceptorContext context = mock(WriterInterceptorContext.class);
		if (contentType != null) {
			when(context.getMediaType()).thenReturn(MediaType.valueOf(contentType));
		}
		return context;
	}
}
