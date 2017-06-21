/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.openwhisk.webaction.io;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.jrestless.core.interceptor.ConditionalBase64WriteInterceptor;

/**
 * Write interceptor that encodes "binary" response bodies.
 * <p>
 * Whether a body of a (raw) Web Action http response needs to be base64 encoded
 * or not, is determined by {@link WriterInterceptorContext#getMediaType()}.
 * According to the OpenWhisk specification, the Spray framework is used to
 * determine whether a body needs to be encoded with base64 or not when passed
 * to the handler. see: <a href=
 * "https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282">
 * https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282</a>
 * Thus we wrap {@link WriterInterceptorContext#getOutputStream()} with a base64
 * encoder if it's a binary media type.
 *
 * @author Bjoern Bilger
 *
 */
//make sure this gets invoked after any encoding WriteInterceptor
@Priority(Priorities.ENTITY_CODER - WebActionBase64WriteInterceptor.PRIORITY_OFFSET)
public class WebActionBase64WriteInterceptor extends ConditionalBase64WriteInterceptor {

	static final int PRIORITY_OFFSET = 100;

	@Override
	protected boolean isBase64(WriterInterceptorContext context) {
		return BinaryMediaTypeDetector.isBinaryMediaType(context.getMediaType());
	}
}
