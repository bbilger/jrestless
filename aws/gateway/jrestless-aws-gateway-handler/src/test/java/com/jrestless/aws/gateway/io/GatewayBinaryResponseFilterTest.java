package com.jrestless.aws.gateway.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import javax.activation.DataSource;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GatewayBinaryResponseFilterTest {

    public static Stream<Arguments> data() {
    	return Stream.of(
    		// if non-binary compression is disallowed, the compression flag should get removed
    		Arguments.of(true, true, "body", false, false),
    		Arguments.of(null, true, "body", false, false),
    		// no compression -> nothing to do
    		Arguments.of(true, false, "body", false, false),
    		Arguments.of(null, false, "body", false, false),
    		// if non-binary compression is allowed and it's about to get compressed, the binary flag should be set and the compression flag retained
    		Arguments.of(false, true, "body", true, true),
    		// if non-binary compression is allowed but it's not about to get compressed, neither the binary nor the compression flag should be set
    		Arguments.of(false, false, "body", false, false),
    		// no entity set, nothing to do
    		Arguments.of(true, false, null, false, false),
    		Arguments.of(false, false, null, false, false),
    		Arguments.of(null, false, null, false, false),
			// retain the compression flag => should not get compressed anyways
    		Arguments.of(true, true, null, false, true),
    		Arguments.of(false, true, null, false, true),
    		Arguments.of(null, true, null, false, true),
    		// byte array compressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, true, new byte[0], true, true),
    		Arguments.of(false, true, new byte[0], true, true),
    		Arguments.of(null, true, new byte[0], true, true),
            // byte array uncompressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, false, new byte[0], true, false),
    		Arguments.of(false, false, new byte[0], true, false),
    		Arguments.of(null, false, new byte[0], true, false),
            // file compressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, true, new File(""), true, true),
    		Arguments.of(false, true, new File(""), true, true),
    		Arguments.of(null, true, new File(""), true, true),
            // file uncompressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, false, new File(""), true, false),
    		Arguments.of(false, false, new File(""), true, false),
    		Arguments.of(null, false, new File(""), true, false),
            // StreamingOutput compressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, true, mock(StreamingOutput.class), true, true),
    		Arguments.of(false, true, mock(StreamingOutput.class), true, true),
    		Arguments.of(null, true, mock(StreamingOutput.class), true, true),
            // StreamingOutput uncompressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, false, mock(StreamingOutput.class), true, false),
    		Arguments.of(false, false, mock(StreamingOutput.class), true, false),
    		Arguments.of(null, false, mock(StreamingOutput.class), true, false),
            // InputStream compressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, true, mock(InputStream.class), true, true),
    		Arguments.of(false, true, mock(InputStream.class), true, true),
    		Arguments.of(null, true, mock(InputStream.class), true, true),
            // InputStream uncompressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, false, mock(InputStream.class), true, false),
    		Arguments.of(false, false, mock(InputStream.class), true, false),
    		Arguments.of(null, false, mock(InputStream.class), true, false),
            // DataSource compressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, true, mock(DataSource.class), true, true),
    		Arguments.of(false, true, mock(DataSource.class), true, true),
    		Arguments.of(null, true, mock(DataSource.class), true, true),
            // DataSource uncompressed (binaryCompressionFlag should not have any impact)
    		Arguments.of(true, false, mock(DataSource.class), true, false),
    		Arguments.of(false, false, mock(DataSource.class), true, false),
    		Arguments.of(null, false, mock(DataSource.class), true, false)
       );
    }

    @ParameterizedTest
    @MethodSource("data")
	public void test(Boolean binaryCompressionOnlyIn, boolean compressionEnabledIn, Object entityIn,
			boolean binaryFlagOut, boolean compressionFlagOut) throws IOException {
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
