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
package com.jrestless.aws.service.client;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceRequestImpl;
import com.jrestless.aws.service.io.ServiceResponse;

import feign.Client;

/**
 * Feign client that redirects "http" requests to lambda functions implementing
 * {@code com.amazonaws.services.lambda.runtime.RequestHandler<ServiceRequest, ServiceResponse>}.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class AbstractFeignLambdaServiceClient implements Client {

	protected abstract ServiceResponse execute(ServiceRequest serviceRequest, feign.Request.Options feignOptions);

	@Override
	public final feign.Response execute(feign.Request feignRequest, feign.Request.Options feignOptions)
			throws IOException {
		ServiceRequest serviceRequest = toServiceRequest(feignRequest);
		ServiceResponse serviceResponse = execute(serviceRequest, feignOptions);
		return toFeignResponse(serviceResponse);
	}

	private static ServiceRequest toServiceRequest(feign.Request feignRequest) {
		return new ServiceRequestImpl(toServiceBody(feignRequest.body()), toServiceHeaders(feignRequest.headers()),
				URI.create(feignRequest.url()), feignRequest.method());
	}

	private static feign.Response toFeignResponse(ServiceResponse serviceResponse) {
		return feign.Response.builder()
				.body(toFeignBody(serviceResponse.getBody()))
				.headers(toFeignHeaders(serviceResponse.getHeaders()))
				.status(serviceResponse.getStatusCode())
				.reason(serviceResponse.getReasonPhrase())
				.build();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, Collection<String>> toFeignHeaders(Map<String, List<String>> map) {
		if (map == null) {
			return Collections.emptyMap();
		}
		// no need to copy the map, here
		return (Map) map;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, List<String>> toServiceHeaders(Map<String, Collection<String>> map) {
		if (map == null) {
			return Collections.emptyMap();
		}
		// no need to copy the map, here
		return (Map) map;
	}

	private static String toServiceBody(byte[] body) {
		return (body == null) ? null : new String(body, StandardCharsets.UTF_8);
	}

	private static byte[] toFeignBody(String body) {
		return (body == null) ? null : body.getBytes(StandardCharsets.UTF_8);
	}

}
