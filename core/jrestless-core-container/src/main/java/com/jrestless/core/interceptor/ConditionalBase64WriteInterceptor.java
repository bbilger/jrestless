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

import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * Wraps the {@link WriterInterceptorContext context's} output stream with a
 * base64 encoder (RFC4648; not URL-safe) if {@link #isBase64(WriterInterceptorContext)}
 * returns true.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class ConditionalBase64WriteInterceptor implements WriterInterceptor {

	@Override
	public final void aroundWriteTo(WriterInterceptorContext context) throws IOException {
		if (isBase64(context)) {
			context.setOutputStream(Base64.getEncoder().wrap(context.getOutputStream()));
		}
		context.proceed();
	}

	/**
	 * Returns true if the {@link WriterInterceptorContext context's}
	 * output stream should be wrapped by a base64 encoder.
	 *
	 * @param context
	 *            the response context
	 * @return {@code true} in case the context's output stream must be wrapped
	 *         by a base64 encoder; {@code false} otherwise
	 */
	protected abstract boolean isBase64(WriterInterceptorContext context);
}
