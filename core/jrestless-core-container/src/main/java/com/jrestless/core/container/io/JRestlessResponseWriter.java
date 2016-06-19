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
package com.jrestless.core.container.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Response.StatusType;


/**
 * A simplified response writer that can be injected into
 * {@link com.jrestless.core.container.JRestlessHandlerContainer.JRestlessContainerResponseWriter}
 * to provide a {@link org.glassfish.jersey.server.spi.ContainerResponseWriter}
 * required by Jersey.
 *
 * @author Bjoern Bilger
 *
 */
public interface JRestlessResponseWriter {
	/**
	 *
	 * @return the entity output stream where the response body will be written
	 *         to
	 */
	OutputStream getEntityOutputStream();

	/**
	 * Writes the response.
	 *
	 * @param statusType
	 *            response status
	 * @param headers
	 *            response headers
	 * @param entityOutputStream
	 *            response entity
	 */
	void writeResponse(@Nonnull StatusType statusType, @Nonnull Map<String, List<String>> headers,
			@Nonnull OutputStream entityOutputStream) throws IOException;
}
