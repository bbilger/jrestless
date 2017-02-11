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
package com.jrestless.aws.gateway.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Filter to determine if the response is a binary response or not. If the
 * filter detects a binary response, the header
 * {@value GatewayBinaryResponseFilter#HEADER_BINARY_RESPONSE} with the value
 * true will be set (previous values will be overwritten).
 * <p>
 * The set priority makes sure that the filters is invoked after
 * {@link org.glassfish.jersey.server.filter.EncodingFilter}
 * <p>
 * A response is flagged as binary if the entity response type is
 * {@code byte[]}, {@link File}, {@link StreamingOutput}, {@link InputStream} or
 * {@link DataSource}.
 * <p>
 * Furthermore this filter will disallow compression of non-binary responses by
 * removing the {@code Content-Type} encoding filter unless the property
 * {@code jrestless.aws.gateway.binary.compression.only} is explicitly set to
 * true (e.g. via
 * {@link org.glassfish.jersey.server.ResourceConfig#property(String, Object)}.
 * <p>
 * If compression for non-binary responses is allowed
 * ({@code jrestless.aws.gateway.binary.compression.only=true}, then the
 * response is flagged as binary, too.
 *
 * @author Bjoern Bilger
 *
 */
// make sure this gets invoked after org.glassfish.jersey.server.filter.EncodingFilter
@Priority(Priorities.HEADER_DECORATOR - GatewayBinaryResponseFilter.PRIORITY_OFFSET)
public class GatewayBinaryResponseFilter implements ContainerResponseFilter {

	public static final String HEADER_BINARY_RESPONSE = "X-JRestlessAwsApiGatewayBinaryResponse";
	public static final String BINARY_COMPRESSION_ONLY_PROPERTY = "jrestless.aws.gateway.binary.compression.only";

	static final int PRIORITY_OFFSET = 100;

	private final boolean binaryCompressionOnly;

	@Inject
	GatewayBinaryResponseFilter(Configuration configuration) {
		Object binaryCompressionOnlyProp = configuration.getProperty(BINARY_COMPRESSION_ONLY_PROPERTY);
		binaryCompressionOnly = binaryCompressionOnlyProp == null
				|| PropertiesHelper.isProperty(binaryCompressionOnlyProp);
	}

	@Override
	public final void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		if (!responseContext.hasEntity()) {
			return;
		}
		boolean binaryResponse = isBinaryEntity(requestContext, responseContext);
		boolean compressedResponse = responseContext.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING);
		/*
		 * flag a response as binary if a) it is a binary response b) the
		 * "Content-Encoding" header is available (and thus will be compressed)
		 * and binaryCompressionOnly is disabled
		 */
		if (binaryResponse || compressedResponse && !binaryCompressionOnly) {
			responseContext.getHeaders().putSingle(HEADER_BINARY_RESPONSE, true);
		}
		/*
		 * remove "Content-Encoding" for non-binary responses to disable compression unless
		 * binaryCompressionOnly is set to false
		 */
		if (compressedResponse && !binaryResponse && binaryCompressionOnly) {
			responseContext.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
		}
	}

	protected boolean isBinaryEntity(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		Object entity = responseContext.getEntity();
		return entity instanceof byte[]
				|| entity instanceof File
				|| entity instanceof StreamingOutput
				|| entity instanceof InputStream
				|| entity instanceof DataSource;
	}
}
