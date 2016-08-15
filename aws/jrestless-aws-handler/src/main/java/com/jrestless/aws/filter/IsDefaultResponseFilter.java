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
package com.jrestless.aws.filter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.ApiOperation;
import jersey.repackaged.com.google.common.cache.Cache;
import jersey.repackaged.com.google.common.cache.CacheBuilder;

/**
 * Filter that adds an additional header "{@code X-Is-Default-Response}" indicating
 * whether the response is the default response or not.
 * <p>
 * The distinction between the default response {@link com.jrestless.aws.io.GatewayDefaultResponse}
 * and the non-default response {@link com.jrestless.aws.io.GatewayAdditionalResponseException} is
 * important since non-default responses cannot return any headers.
 * <p>
 * Note: the header is never exposed to the outside world. It's just for internal usage.
 *
 * @author Bjoern Bilger
 *
 */
public class IsDefaultResponseFilter implements ContainerResponseFilter {

	public static final String IS_DEFAULT_RESPONSE_HEADER_NAME = "X-Is-Default-Response";
	private static final int CACHE_SIZE = 100;

	private Cache<Method, Integer> defaultResponseCodeCache;

	@Context
	private ResourceInfo resourceInfo;

	public IsDefaultResponseFilter() {
		defaultResponseCodeCache = CacheBuilder.newBuilder().maximumSize(CACHE_SIZE).build();
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		Method endpoint = resourceInfo.getResourceMethod();
		if (endpoint == null) {
			return;
		}
		try {
			int defaulResponseCode = defaultResponseCodeCache.get(endpoint, new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return getDefaultResponseCode(endpoint);
				}
			});
			boolean defaultResponse = defaulResponseCode == responseContext.getStatus();
			responseContext.getHeaders().add(IS_DEFAULT_RESPONSE_HEADER_NAME, defaultResponse ? "1" : "0");
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private int getDefaultResponseCode(Method endpoint) {
		Integer defaultResponseCode = getDefaultResponseCodeFromAnnotatedElement(endpoint);
		if (defaultResponseCode == null) {
			defaultResponseCode = Status.OK.getStatusCode();
		}
		return defaultResponseCode;
	}

	private Integer getDefaultResponseCodeFromAnnotatedElement(AnnotatedElement annotatedElement) {
		ApiOperation apiOperation = annotatedElement.getAnnotation(ApiOperation.class);
		if (apiOperation != null) {
			return apiOperation.code();
		}
		return null;
	}

}
