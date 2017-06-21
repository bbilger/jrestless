package com.jrestless.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class IOUtilsTest {

	private static final String UTF8_SYMBOL = "☺";

	@Test
	public void testToBytes() throws IOException {
		assertArrayEquals("test".getBytes(), IOUtils.toBytes(new ByteArrayInputStream("test".getBytes())));
	}

	@Test
	public void testToBytesWithEmptyByteArray() throws IOException {
		assertArrayEquals(new byte[0], IOUtils.toBytes(new ByteArrayInputStream(new byte[0])));
	}

	@Test
	public void testToBytesRethrowsIOExceptionWrapped() throws IOException {
		InputStream is = mock(InputStream.class);
		IOException thrownException = new IOException("whatever");
		when(is.read(any(), anyInt(), anyInt())).thenThrow(thrownException);
		try {
			IOUtils.toBytes(is);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
			assertSame(thrownException, re.getCause());
		}
	}

	@Test
	public void testToBytesRethrowsRuntimeExceptionAsIs() throws IOException {
		InputStream is = mock(InputStream.class);
		RuntimeException thrownException = new RuntimeException("whatever");
		when(is.read(any(), anyInt(), anyInt())).thenThrow(thrownException);
		try {
			IOUtils.toBytes(is);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
			assertSame(thrownException, re);
		}
	}

	@Test
	public void testToStringUsesUtf8() {
		assertEquals(UTF8_SYMBOL, IOUtils.toString(new ByteArrayInputStream(UTF8_SYMBOL.getBytes())));
	}

	@Test
	public void testToStringCharsets() {
		assertEquals(UTF8_SYMBOL, IOUtils.toString(new ByteArrayInputStream(UTF8_SYMBOL.getBytes())));
		assertNotEquals(UTF8_SYMBOL,
				IOUtils.toString(new ByteArrayInputStream(UTF8_SYMBOL.getBytes()), StandardCharsets.ISO_8859_1));
	}

	@Test
	public void bumpCodeCoverageByInvokingThePrivateConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(IOUtils.class);
	}
}
