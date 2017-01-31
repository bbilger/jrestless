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
package com.jrestless.core.filter;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Request filter that appends the {@link ApplicationPath#value() application's
 * path} to the base URI - iff an application path is configured.
 * <p>
 * Leading and trailing slashes and spaces are removed
 *
 * @author Bjoern Bilger
 *
 */
@PreMatching
public class ApplicationPathFilter implements ContainerRequestFilter {

	private final String applicationPath;

	@Inject
	ApplicationPathFilter(Application applicationConfig) {
		this.applicationPath = getApplicationPath(applicationConfig);
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (applicationPath != null) {
			UriInfo requestUriInfo = requestContext.getUriInfo();
			UriBuilder baseUriBuilder = requestUriInfo.getBaseUriBuilder();
			baseUriBuilder.path(applicationPath);
			// the base URI must end with a trailing slash
			baseUriBuilder.path("/");
			URI updatedBaseUri = baseUriBuilder.build();
			URI requestUri = requestUriInfo.getRequestUri();
			requestContext.setRequestUri(updatedBaseUri, requestUri);
		}
	}

	private static String getApplicationPath(Application applicationConfig) {
		if (applicationConfig == null) {
			return null;
		}
		ApplicationPath ap = applicationConfig.getClass().getAnnotation(ApplicationPath.class);
		if (ap == null) {
			return null;
		}
		String applicationPath = ap.value();
		if (isBlank(applicationPath)) {
			return null;
		}
		while (applicationPath.startsWith("/")) {
			applicationPath = applicationPath.substring(1);
		}
		// support Servlet configs
		if (applicationPath.endsWith("/*")) {
			applicationPath = applicationPath.substring(0, applicationPath.length() - 2);
		}
		while (applicationPath.endsWith("/")) {
			applicationPath = applicationPath.substring(0, applicationPath.length() - 1);
		}
		if (isBlank(applicationPath)) {
			return null;
		}
		return applicationPath;
	}

	private static boolean isBlank(String s) {
		if (s == null) {
			return true;
		}
		String trimmed = s.trim();
		if (trimmed.isEmpty()) {
			return true;
		}
		return false;
	}
}
