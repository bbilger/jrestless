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

import java.io.IOException;
import java.util.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;


/**
 * Write interceptor that encodes the response in base64 if the first
 * {@link GatewayBinaryResponseCheckFilter#HEADER_BINARY_RESPONSE
 * X-JRestlessAwsApiGatewayBinaryResponse header} is set to true.
 * <p>
 * The set priority makes sure that the interceptor is called after any other
 * entity coder.
 * <p>
 * Note: AWS API Gateway will convert the base64 encoded response into binary
 * data only if
 * <ol>
 * <li>Binary media types are configured.
 * <li>The {@code Accept} header send together with the request matches one of
 * the configured binary media types.
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
// make sure this gets invoked after any encoding WriteInterceptor
@Priority(Priorities.ENTITY_CODER - GatewayBinaryWriteInterceptor.PRIORITY_OFFSET)
public class GatewayBinaryWriteInterceptor implements WriterInterceptor {

	static final int PRIORITY_OFFSET = 100;

	@Override
	public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
		Object headerValue = context.getHeaders().getFirst(GatewayBinaryResponseCheckFilter.HEADER_BINARY_RESPONSE);
		if (Boolean.TRUE.equals(headerValue)) {
			context.setOutputStream(Base64.getEncoder().wrap(context.getOutputStream()));
		}
		context.proceed();
	}
}
