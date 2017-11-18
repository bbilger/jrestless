package com.jrestless.openwhisk.webaction.io;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

public class WebActionBase64ReadInterceptorTest {

	private final WebActionBase64ReadInterceptor readInterceptor = new WebActionBase64ReadInterceptor();

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
		ReaderInterceptorContext context = mockContext(contentType);
		InputStream is = mock(InputStream.class);
		when(context.getInputStream()).thenReturn(is);

		readInterceptor.aroundReadFrom(context);

		verifyZeroInteractions(is);

		verify(context).getMediaType();
		verify(context).proceed();
		verifyNoMoreInteractions(context);
	}

	public void testWrapsInputStream(String contentType) throws WebApplicationException, IOException {
		ReaderInterceptorContext context = mockContext(contentType);
		InputStream is = mock(InputStream.class);
		when(context.getInputStream()).thenReturn(is);

		readInterceptor.aroundReadFrom(context);

		verifyZeroInteractions(is);

		ArgumentCaptor<InputStream> updatedIsCapture = ArgumentCaptor.forClass(InputStream.class);
		verify(context).setInputStream(updatedIsCapture.capture());
		verify(context).getMediaType();
		verify(context).getInputStream();
		verify(context).proceed();
		verifyNoMoreInteractions(context);

		InputStream updatedIs = updatedIsCapture.getValue();

		// just make sure we have some wrapper
		assertNotSame(is, updatedIs);
		updatedIs.close();
		verify(is).close();
	}

	private static ReaderInterceptorContext mockContext(String contentType) {
		ReaderInterceptorContext context = mock(ReaderInterceptorContext.class);
		if (contentType != null) {
			when(context.getMediaType()).thenReturn(MediaType.valueOf(contentType));
		}
		return context;
	}
}
