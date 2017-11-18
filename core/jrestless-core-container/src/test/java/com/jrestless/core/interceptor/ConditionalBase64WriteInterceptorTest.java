package com.jrestless.core.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Random;

import javax.ws.rs.ext.WriterInterceptorContext;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ConditionalBase64WriteInterceptorTest {

	private final NeverBase64WriteInterceptor neverBase64WriteInterceptor = spy(new NeverBase64WriteInterceptor());
	private final AlwaysBase64WriteInterceptor alwaysBase64WriteInterceptor = spy(new AlwaysBase64WriteInterceptor());

	@Test
	public void testWrapsOutputStreamNever() throws IOException {

		WriterInterceptorContext context = mock(WriterInterceptorContext.class);
		OutputStream os = mock(OutputStream.class);
		when(context.getOutputStream()).thenReturn(os);

		neverBase64WriteInterceptor.aroundWriteTo(context);

		verify(neverBase64WriteInterceptor).isBase64(context);

		verify(context).proceed();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testWrapsOutputStreamAlways() throws IOException {

		WriterInterceptorContext context = mock(WriterInterceptorContext.class);
		OutputStream os = mock(OutputStream.class);
		when(context.getOutputStream()).thenReturn(os);

		ArgumentCaptor<OutputStream> updatedOsCapture = ArgumentCaptor.forClass(OutputStream.class);

		alwaysBase64WriteInterceptor.aroundWriteTo(context);

		verify(alwaysBase64WriteInterceptor).isBase64(context);

		verifyZeroInteractions(os);

		verify(context).setOutputStream(updatedOsCapture.capture());
		verify(context).proceed();
		verify(context).getOutputStream();
		verifyNoMoreInteractions(context);
		OutputStream updatedOs = updatedOsCapture.getValue();

		// just make sure we have some wrapper
		assertNotSame(os, updatedOs);
		updatedOs.close();
		verify(os).close();
	}

	@Test
	public void testDoesNotWrapOutputStreamWithBase64UrlEncoder() throws IOException {
		// a URL encoder would give "KUra8-qaMAL-Kpv0_5pR6zm8_d4="
		final String base64Bytes = "KUra8+qaMAL+Kpv0/5pR6zm8/d4=";
		testBase64Encoding(Base64.getDecoder().decode(base64Bytes), base64Bytes);
	}

	@Test
	public void testDoesNotWrapOutputStreamWithBase64MimeEncoder() throws IOException {
		/*
		 * a mime encoder is usually limited in size and would add newlines when the line limit is hit
		 * => let's generate a large string or rather byte array
		 */
		final byte[] bytes = new byte[200];
		new Random().nextBytes(bytes);
		final String base64Bytes = Base64.getEncoder().encodeToString(bytes);
		testBase64Encoding(Base64.getDecoder().decode(base64Bytes), base64Bytes);
	}

	@Test
	public void testBase64Encoding() throws IOException {
		final byte[] byteToEncode = "test".getBytes();
		testBase64Encoding(byteToEncode, new String(Base64.getEncoder().encode(byteToEncode)));
	}

	private void testBase64Encoding(byte[] bytes, String expectedBase64) throws IOException {

		WriterInterceptorContext context = mock(WriterInterceptorContext.class);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		when(context.getOutputStream()).thenReturn(baos);

		ArgumentCaptor<OutputStream> updatesOsCapture = ArgumentCaptor.forClass(OutputStream.class);

		alwaysBase64WriteInterceptor.aroundWriteTo(context);

		verify(context).setOutputStream(updatesOsCapture.capture());
		OutputStream updatedOs = updatesOsCapture.getValue();

		updatedOs.write(bytes);
		updatedOs.close();
		assertEquals(expectedBase64, baos.toString());
	}


	private static class NeverBase64WriteInterceptor extends ConditionalBase64WriteInterceptor {
		@Override
		protected boolean isBase64(WriterInterceptorContext context) {
			return false;
		}
	}

	private static class AlwaysBase64WriteInterceptor extends ConditionalBase64WriteInterceptor {
		@Override
		protected boolean isBase64(WriterInterceptorContext context) {
			return true;
		}
	}
}
