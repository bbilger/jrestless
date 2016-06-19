package com.jrestless.core.container.io;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Request definition to feed Jersey.
 *
 * @author Bjoern Bilger
 *
 */
public interface JRestlessRequestContext {

	/**
	 *
	 * @return the base uri; either the request base uri, of if none is given that of the server.
	 */
	@Nonnull
	URI getBaseUri();

	/**
	 *
	 * @return the request uri
	 */
	@Nonnull
	URI getRequestUri();

	/**
	 *
	 * @return the HTTP method (upper-case) e.g. GET, PUT, POST, DELETE, ...
	 */
	@Nonnull
	String getHttpMethod();

	/**
	 * @return the request body
	 */
	@Nonnull
	InputStream getEntityStream();

	/**
	 *
	 * @return the headers of the request.
	 */
	@Nonnull
	Map<String, List<String>> getHeaders();
}
