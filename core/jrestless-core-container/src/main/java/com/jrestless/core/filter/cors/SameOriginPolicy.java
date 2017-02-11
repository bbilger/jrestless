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
package com.jrestless.core.filter.cors;

import javax.annotation.Nonnull;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Policy to determine if a request should be considered as a same-origin
 * request or not.
 * <p>
 * Definitions of what a same-origin request is may vary. It might be for example
 * desirable to just check the host and ignore the scheme and port.
 * <p>
 * Containers may also have different ways to determine the "target".
 *
 * @author Bjoern Bilger
 *
 */
@FunctionalInterface
public interface SameOriginPolicy {
	/**
	 * Checks if it's a same origin request.
	 * <p>
	 * Implementations may <b>not</b> change any values in the passed
	 * {@link ContainerRequestContext requestContext}.
	 *
	 * @param requestContext
	 *            the request context of the current request may be accessed
	 *            read-only!
	 * @param origin
	 *            The origin of the request. It's guaranteed that the origin has
	 *            a scheme, host and does not have a path, fragment, query or
	 *            user info - a port can be present.
	 * @return true if the request should be considered as a same origin
	 *         request; false otherwise
	 */
	boolean isSameOrigin(@Nonnull ContainerRequestContext requestContext, @Nonnull String origin);
}
