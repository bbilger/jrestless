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
package com.jrestless.aws.gateway.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.jrestless.aws.gateway.io.DefaultGatewayRequest;
import com.jrestless.aws.gateway.io.GatewayResponse;

/**
 * AWS lambda request (object) handler that uses
 * {@link com.jrestless.core.container.JRestlessHandlerContainer} and so Jersey
 * to process incoming requests from AWS API Gateay.
 * <p>
 * Implementations must provide a no-args constructor.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class GatewayRequestObjectHandler extends GatewayRequestHandler
		implements RequestHandler<DefaultGatewayRequest, GatewayResponse> {

	protected GatewayRequestObjectHandler() {
		super();
	}

	@Override
	public GatewayResponse handleRequest(DefaultGatewayRequest request, Context lambdaContext) {
		return delegateRequest(new GatewayRequestAndLambdaContext(request, lambdaContext));
	}
}
