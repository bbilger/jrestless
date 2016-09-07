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

/**
 * The non-default response which will be thrown back to the API Gateway.
 * <p>
 * Since we want to have different return codes in AWS API Gateway, we have to
 * pass a non-default response in the form of an exception. The exception
 * message must contain the status code and the body and can be accessed in the
 * AWS API Gateway response template as "errorMessage".
 * TODO add explanation for headers...
 *
 * The response looks like this
 *
 * <pre>
 * {
 *   "errorMessage": "{\"statusCode\": "STATUSCODE", \"body\": \"ESCAPED_BODY\"}",
 *   "stackTrace": [],
 *   "errorType": "com.jrestless.aws.AdditionalGatewayResponseException",
 *   "cause": {
 *   	"errorMessage": "headervalue0",
 *   	"stackTrace": [],
 *   	"errorType": "com.jrestless.aws.AdditionalGatewayResponseException",
 *   	"cause": {
 *   		"errorMessage": "headervalue1",
 *   		"stackTrace": [],
 *   		"errorType": "com.jrestless.aws.AdditionalGatewayResponseException",
 *   		"cause": ...
 *   	}
 *   }
 * }
 * </pre>
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayAdditionalResponseException extends RuntimeException {

	private static final long serialVersionUID = -7108361306031768989L;

	protected GatewayAdditionalResponseException(String errorMessage, GatewayNonDefaultHeaderException headerChain) {
		super(errorMessage, headerChain, false, false);
	}
}
