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
package com.jrestless.aws.gateway;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;

/**
 * Filter to determine if the response is a binary response or not. If the
 * filter detects a binary response, the header
 * {@value GatewayBinaryResponseCheckFilter#HEADER_BINARY_RESPONSE} with the
 * value true will be set (previous values will be overwritten).
 * <p>
 * The set priority makes sure that the filters is invoked after
 * {@link org.glassfish.jersey.server.filter.EncodingFilter}
 * <p>
 * A response is flagged as binary if the response is either compressed i.e. the
 * header "Content-Encoding" is set or if the entity response
 * type is {@code byte[]}, {@link File}, {@link StreamingOutput},
 * {@link InputStream} or {@link DataSource}.
 *
 * @author Bjoern Bilger
 *
 */
// make sure this gets invoked after org.glassfish.jersey.server.filter.EncodingFilter
@Priority(Priorities.HEADER_DECORATOR - GatewayBinaryResponseCheckFilter.PRIORITY_OFFSET)
public class GatewayBinaryResponseCheckFilter implements ContainerResponseFilter {

	public static final String HEADER_BINARY_RESPONSE = "X-JRestlessAwsApiGatewayBinaryResponse";
	static final int PRIORITY_OFFSET = 100;

	@Override
	public final void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		if (!responseContext.hasEntity()) {
			return;
		}
		if (isBinaryEntity(requestContext, responseContext)
				|| isCompressed(requestContext, responseContext)) {
			responseContext.getHeaders().putSingle(HEADER_BINARY_RESPONSE, true);
		}
	}

	protected boolean isCompressed(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		return responseContext.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING);
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
