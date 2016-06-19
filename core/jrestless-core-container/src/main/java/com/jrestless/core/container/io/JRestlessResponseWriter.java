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
