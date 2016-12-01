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

import java.io.IOException;
import java.util.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/**
 * Read interceptor that decodes the response from base64 if the property
 * {@link GatewayBinaryReadInterceptor#PROPERTY_BASE_64_ENCODED_REQUEST
 * base64EncodedAwsApiGatewayRequest} is set to true.
 * <p>
 * The set priority makes sure that the interceptor is called before any other
 * entity coder.
 * <p>
 * Note: the property
 * {@value GatewayBinaryReadInterceptor#PROPERTY_BASE_64_ENCODED_REQUEST} should
 * be set to true if the request is flagged as being base64-encoded; see
 * {@link com.jrestless.aws.gateway.io.GatewayRequest#isBase64Encoded()}
 * <p>
 * Note: AWS API Gateway will set
 * {@link com.jrestless.aws.gateway.io.GatewayRequest#isBase64Encoded()} to true
 * only if
 * <ol>
 * <li>Binary media types are configured.
 * <li>The {@code Content-Type} header send together with the request matches
 * one of the configured binary media types.
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
// make sure this gets invoked before any encoding ReaderInterceptor
@Priority(Priorities.ENTITY_CODER - GatewayBinaryReadInterceptor.PRIORITY_OFFSET)
public class GatewayBinaryReadInterceptor implements ReaderInterceptor {

	public static final String PROPERTY_BASE_64_ENCODED_REQUEST = "base64EncodedAwsApiGatewayRequest";
	static final int PRIORITY_OFFSET = 100;

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		if (Boolean.TRUE.equals(context.getProperty(PROPERTY_BASE_64_ENCODED_REQUEST))) {
			context.setInputStream(Base64.getDecoder().wrap(context.getInputStream()));
		}
		return context.proceed();
	}
}
