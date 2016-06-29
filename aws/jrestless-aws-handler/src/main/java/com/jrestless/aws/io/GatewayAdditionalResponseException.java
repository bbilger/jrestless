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
package com.jrestless.aws.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * The non-default response which will be thrown back to the API Gateway.
 * <p>
 * Since we want to have different return codes in AWS API Gateway, we have to
 * pass a non-default response in the form of an exception. The exception
 * message will contain the status code and the body and can be accessed in the
 * AWS API Gateway response template as "errorMessage". The response looks like
 *
 * <pre>
 * {
 *   "errorMessage": "{\"statusCode\": "STATUSCODE", \"body\": \"ESCAPED_BODY\"}",
 *   "stackTrace": [],
 *   "errorType": "com.jrestless.aws.AdditionalGatewayResponseException"
 * }
 * </pre>
 *
 * Headers aren't supported because of limitation in AWS API Gateway.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayAdditionalResponseException extends RuntimeException {

	private static final long serialVersionUID = -7108361306031768989L;

	protected GatewayAdditionalResponseException(String message) {
		super(message, null, false, false);
	}

	protected GatewayAdditionalResponseException(@Nullable String body, int statusCode) {
		this(createJsonResponse(body, statusCode));
	}

	public GatewayAdditionalResponseException(@Nullable String body, @Nonnull StatusType statusType) {
		this(body, statusType.getStatusCode());
	}


	/**
	 * Returns the response as JSON. Only the statusCode and the body will be
	 * part of the response.
	 *
	 * <pre>
	 * {
	 *   "statusCode": "STATUSCODE",
	 *   "body": "BODY" //escaped!
	 * }
	 * </pre>
	 *
	 * @return
	 */
	protected static String createJsonResponse(String body, int statusCode) {
		String escpapedBody = StringEscapeUtils.escapeJson(body);
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("{\"statusCode\":\"");
		strBuilder.append(statusCode);
		if (escpapedBody != null) {
			strBuilder.append("\",\"body\":\"");
			strBuilder.append(escpapedBody);
		}
		strBuilder.append("\"}");
		return strBuilder.toString();
	}
}
