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
package com.jrestless.core.interceptor;

import java.io.IOException;
import java.util.Base64;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/**
 * Wraps the {@link ReaderInterceptorContext context's} input stream with a
 * base64 decoder (RFC4648; not URL-safe) if {@link #isBase64(ReaderInterceptorContext)}
 * returns true.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class ConditionalBase64ReadInterceptor implements ReaderInterceptor {

	@Override
	public final Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		if (isBase64(context)) {
			context.setInputStream(Base64.getDecoder().wrap(context.getInputStream()));
		}
		return context.proceed();
	}

	/**
	 * Returns true if the {@link ReaderInterceptorContext context's}
	 * input stream should be wrapped by a base64 decoder.
	 *
	 * @param context
	 *            the response context
	 * @return {@code true} in case the context's input stream must be wrapped
	 *         by a base64 encoder; {@code false} otherwise
	 */
	protected abstract boolean isBase64(ReaderInterceptorContext context);
}
