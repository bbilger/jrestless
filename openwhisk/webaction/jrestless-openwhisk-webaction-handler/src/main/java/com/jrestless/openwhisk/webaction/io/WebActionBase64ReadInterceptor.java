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
import javax.ws.rs.ext.ReaderInterceptorContext;

import com.jrestless.core.interceptor.ConditionalBase64ReadInterceptor;

/**
 * Read interceptor that decodes a potentially base64 encoded request body.
 * <p>
 * Whether a body of a (raw) Web Action request is base64 encoded or not, is
 * determined by {@link ReaderInterceptorContext#getMediaType()}. According to
 * the OpenWhisk specification, the Spray framework is used to determine whether
 * a body needs to be encoded with base64 or not when passed to the handler.
 * see: <a href=
 * "https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282">
 * https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282</a>
 * Thus we wrap {@link ReaderInterceptorContext#getInputStream()} with a base64
 * decoder if it's a binary media type.
 *
 * @author Bjoern Bilger
 *
 */
//make sure this gets invoked before any encoding ReaderInterceptor
@Priority(Priorities.ENTITY_CODER - WebActionBase64ReadInterceptor.PRIORITY_OFFSET)
public class WebActionBase64ReadInterceptor extends ConditionalBase64ReadInterceptor {

	static final int PRIORITY_OFFSET = 100;

	@Override
	protected boolean isBase64(ReaderInterceptorContext context) {
		return BinaryMediaTypeDetector.isBinaryMediaType(context.getMediaType());
	}
}
