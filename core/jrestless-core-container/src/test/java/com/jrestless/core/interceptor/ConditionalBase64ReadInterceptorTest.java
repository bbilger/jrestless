package com.jrestless.core.interceptor;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Random;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.jrestless.test.IOUtils;

public class ConditionalBase64ReadInterceptorTest {

	private final NeverBase64ReadInterceptor neverBase64ReadInterceptor = spy(new NeverBase64ReadInterceptor());
	private final AlwaysBase64ReadInterceptor alwaysBase64ReadInterceptor = spy(new AlwaysBase64ReadInterceptor());

	@Test
	public void testWrapsInputStreamNever() throws WebApplicationException, IOException {
		ReaderInterceptorContext context = mock(ReaderInterceptorContext.class);
		InputStream is = mock(InputStream.class);
		when(context.getInputStream()).thenReturn(is);

		neverBase64ReadInterceptor.aroundReadFrom(context);

		verify(neverBase64ReadInterceptor).isBase64(context);
		verify(context).proceed();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testWrapsInputStreamAlways() throws WebApplicationException, IOException {
		ReaderInterceptorContext context = mock(ReaderInterceptorContext.class);
		InputStream is = mock(InputStream.class);
		when(context.getInputStream()).thenReturn(is);

		alwaysBase64ReadInterceptor.aroundReadFrom(context);

		verifyZeroInteractions(is);

		ArgumentCaptor<InputStream> updatedIsCapture = ArgumentCaptor.forClass(InputStream.class);
		verify(context).setInputStream(updatedIsCapture.capture());
		verify(context).proceed();
		verify(context).getInputStream();
		verifyNoMoreInteractions(context);

		InputStream updatedIs = updatedIsCapture.getValue();

		verify(alwaysBase64ReadInterceptor).isBase64(context);

		// just make sure we have some wrapper
		assertNotSame(is, updatedIs);
		updatedIs.close();
		verify(is).close();
	}

	@Test(expected = Base64DecodingFailedException.class)
	public void testDoesNotWrapInputStreamWithBase64UrlEncoder() throws IOException {
		// should be KUra8+qaMAL+Kpv0/5pR6zm8/d4=
		final String base64Bytes = "KUra8-qaMAL-Kpv0_5pR6zm8_d4=";
		testBase64DecodingFails(base64Bytes);
	}

	@Test(expected = Base64DecodingFailedException.class)
	public void testDoesNotWrapInputStreamWithBase64MimeEncoder() throws IOException {
		final byte[] bytes = new byte[200];
		new Random().nextBytes(bytes);
		testBase64DecodingFails(Base64.getMimeEncoder().encodeToString(bytes));
	}

	@Test
	public void testBase64Decoding() throws IOException {
		final byte[] actualBytes = "test".getBytes();
		testBase64Decoding(Base64.getEncoder().encodeToString(actualBytes), actualBytes);
	}

	private void testBase64Decoding(String base64InputString, byte[] expectedReadBytes) throws IOException {
		assertArrayEquals(expectedReadBytes, IOUtils.toBytes(fetchWrappedInputStream(base64InputString)));
	}

	private void testBase64DecodingFails(String base64InputString) throws WebApplicationException, IOException {
		InputStream is = fetchWrappedInputStream(base64InputString);
		try {
			IOUtils.toBytes(is);
		} catch (Exception e) {
			throw new Base64DecodingFailedException(e);
		}
	}

	private InputStream fetchWrappedInputStream(String base64InputString) throws WebApplicationException, IOException {

		ReaderInterceptorContext context = mock(ReaderInterceptorContext.class);
		ByteArrayInputStream bais = new ByteArrayInputStream(base64InputString.getBytes());
		when(context.getInputStream()).thenReturn(bais);

		ArgumentCaptor<InputStream> updatesIsCapture = ArgumentCaptor.forClass(InputStream.class);

		alwaysBase64ReadInterceptor.aroundReadFrom(context);

		verify(context).setInputStream(updatesIsCapture.capture());
		return updatesIsCapture.getValue();
	}

	private static class NeverBase64ReadInterceptor extends ConditionalBase64ReadInterceptor {
		@Override
		protected boolean isBase64(ReaderInterceptorContext context) {
			return false;
		}
	}

	private static class AlwaysBase64ReadInterceptor extends ConditionalBase64ReadInterceptor {
		@Override
		protected boolean isBase64(ReaderInterceptorContext context) {
			return true;
		}
	}

	private static final class Base64DecodingFailedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		Base64DecodingFailedException(Exception e) {
			super(e);
		}

	}
}
