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

import java.util.Map;

/**
 * The deserialized request passed to the (raw) Web Action.
 * <p>
 * It can be injected into resources via {@code @Context}.
 *
 * @author Bjoern Bilger
 *
 */
public interface WebActionRequest {

	/**
	 * The HTTP method of the request.
	 */
	String getHttpMethod();

	/**
	 * The request headers.
	 */
	Map<String, String> getHeaders();

	/**
	 * The unmatched path of the request (matching stops after consuming the action extension).
	 */
	String getPath();

	/**
	 * The namespace identifying the OpenWhisk authenticated subject.
	 */
	String getUser();

	/**
	 * The request body entity, as a base64 encoded string when content is
	 * binary, or plain string otherwise.
	 */
	String getBody();

	/**
	 * The query parameters from the request as an unparsed string.
	 */
	String getQuery();
}
