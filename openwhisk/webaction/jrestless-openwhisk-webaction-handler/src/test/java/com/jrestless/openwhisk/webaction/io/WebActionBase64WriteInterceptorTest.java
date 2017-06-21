package com.jrestless.openwhisk.webaction.io;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

@RunWith(Parameterized.class)
public class WebActionBase64WriteInterceptorTest {

	private final WebActionBase64WriteInterceptor writeInterceptor = new WebActionBase64WriteInterceptor();

	@Parameters
    public static Collection<Object[]> data() {
    	return Arrays.asList(new Object[][] {
			{ "text/plain", false },
			{ "text/html", false },
			{ "application/json", true },
			{ "image/png", true },
			{ "whatever/whatever", true },
			{ null, true }
		});
    };

    private final String contentType;
    private final boolean base64Encoded;

	public WebActionBase64WriteInterceptorTest(String contentType, boolean base64Encoded) {
		this.contentType = contentType;
		this.base64Encoded = base64Encoded;
    }

	@Test
	public void test() throws WebApplicationException, IOException {
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
