package com.jrestless.aws.gateway.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.activation.DataSource;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GatewayBinaryResponseFilterTest {

	@Parameters
    public static Collection<Object[]> data() {
    	return Arrays.asList(new Object[][] {

    		// if non-binary compression is disallowed, the compression flag should get removed
    		{ true, true, "body", false, false },
    		{ null, true, "body", false, false },
    		// no compression -> nothing to do
    		{ true, false, "body", false, false },
    		{ null, false, "body", false, false },
    		// if non-binary compression is allowed and it's about to get compressed, the binary flag should be set and the compression flag retained
    		{ false, true, "body", true, true },
    		// if non-binary compression is allowed but it's not about to get compressed, neither the binary nor the compression flag should be set
    		{ false, false, "body", false, false },
    		// no entity set, nothing to do
    		{ true, false, null, false, false },
    		{ false, false, null, false, false },
    		{ null, false, null, false, false },
			// retain the compression flag => should not get compressed anyways
    		{ true, true, null, false, true },
    		{ false, true, null, false, true },
    		{ null, true, null, false, true },
    		// byte array compressed (binaryCompressionFlag should not have any impact)
        	{ true, true, new byte[0], true, true },
            { false, true, new byte[0], true, true },
            { null, true, new byte[0], true, true },
            // byte array uncompressed (binaryCompressionFlag should not have any impact)
            { true, false, new byte[0], true, false },
            { false, false, new byte[0], true, false },
            { null, false, new byte[0], true, false },
            // file compressed (binaryCompressionFlag should not have any impact)
            { true, true, new File(""), true, true },
            { false, true, new File(""), true, true },
            { null, true, new File(""), true, true },
            // file uncompressed (binaryCompressionFlag should not have any impact)
            { true, false, new File(""), true, false },
            { false, false, new File(""), true, false },
            { null, false, new File(""), true, false },
            // StreamingOutput compressed (binaryCompressionFlag should not have any impact)
            { true, true, mock(StreamingOutput.class), true, true },
            { false, true, mock(StreamingOutput.class), true, true },
            { null, true, mock(StreamingOutput.class), true, true },
            // StreamingOutput uncompressed (binaryCompressionFlag should not have any impact)
            { true, false, mock(StreamingOutput.class), true, false },
            { false, false, mock(StreamingOutput.class), true, false },
            { null, false, mock(StreamingOutput.class), true, false },
            // InputStream compressed (binaryCompressionFlag should not have any impact)
            { true, true, mock(InputStream.class), true, true },
            { false, true, mock(InputStream.class), true, true },
            { null, true, mock(InputStream.class), true, true },
            // InputStream uncompressed (binaryCompressionFlag should not have any impact)
            { true, false, mock(InputStream.class), true, false },
            { false, false, mock(InputStream.class), true, false },
            { null, false, mock(InputStream.class), true, false },
            // DataSource compressed (binaryCompressionFlag should not have any impact)
            { true, true, mock(DataSource.class), true, true },
            { false, true, mock(DataSource.class), true, true },
            { null, true, mock(DataSource.class), true, true },
            // DataSource uncompressed (binaryCompressionFlag should not have any impact)
            { true, false, mock(DataSource.class), true, false },
            { false, false, mock(DataSource.class), true, false },
            { null, false, mock(DataSource.class), true, false }
       });
    }

    private Boolean binaryCompressionOnlyIn;
    private boolean compressionEnabledIn;
    private Object entityIn;
    private boolean binaryFlagOut;
    private boolean compressionFlagOut;

    public GatewayBinaryResponseFilterTest(Boolean binaryCompressionOnlyIn, boolean compressionEnabledIn, Object entityIn, boolean binaryFlagOut, boolean compressionFlagOut) {
    	this.binaryCompressionOnlyIn = binaryCompressionOnlyIn;
    	this.compressionEnabledIn = compressionEnabledIn;
    	this.entityIn = entityIn;
    	this.binaryFlagOut = binaryFlagOut;
    	this.compressionFlagOut = compressionFlagOut;
    }

    @Test
    public void test() throws IOException {
    	MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    	if (compressionEnabledIn) {
    		headers.putSingle(HttpHeaders.CONTENT_ENCODING, "gzip");
    	}
    	ContainerResponseContext response = createResponseMock(entityIn, headers);
    	GatewayBinaryResponseFilter filter = createFilter(binaryCompressionOnlyIn);
    	filter.filter(null, response);

    	if (binaryFlagOut && compressionFlagOut) {
    		assertEquals(2, headers.size());
    	} else if (binaryFlagOut || compressionFlagOut) {
    		assertEquals(1, headers.size());
    	} else {
    		assertEquals(0, headers.size());
    	}

    	if (binaryFlagOut) {
    		assertEquals(true, headers.getFirst(GatewayBinaryResponseFilter.HEADER_BINARY_RESPONSE));
    	} else {
    		assertNull(headers.getFirst(GatewayBinaryResponseFilter.HEADER_BINARY_RESPONSE));
    	}
    	if (compressionFlagOut) {
    		assertNotNull(headers.getFirst(HttpHeaders.CONTENT_ENCODING));
    	} else {
    		assertNull(headers.getFirst(HttpHeaders.CONTENT_ENCODING));
    	}
    }

	private ContainerResponseContext createResponseMock(Object entity, MultivaluedMap<String, Object> headers) {
		ContainerResponseContext response = mock(ContainerResponseContext.class);
		when(response.hasEntity()).thenReturn(entity != null);
		when(response.getEntity()).thenReturn(entity);
		when(response.getHeaders()).thenReturn(headers);
		return response;
	}

	private static GatewayBinaryResponseFilter createFilter(Boolean binaryCompressionOnly) {
		Configuration config = mock(Configuration.class);
		when(config.getProperty(GatewayBinaryResponseFilter.BINARY_COMPRESSION_ONLY_PROPERTY))
				.thenReturn(binaryCompressionOnly);
		GatewayBinaryResponseFilter filter = new GatewayBinaryResponseFilter();
		filter.setConfiguration(config);
		return filter;
	}
}
