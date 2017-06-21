package com.jrestless.openwhisk.webaction.io;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

@RunWith(Parameterized.class)
public class WebActionBase64ReadInterceptorTest {

	private final WebActionBase64ReadInterceptor readInterceptor = new WebActionBase64ReadInterceptor();

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

	public WebActionBase64ReadInterceptorTest(String contentType, boolean base64Encoded) {
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
