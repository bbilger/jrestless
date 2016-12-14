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
package com.jrestless.aws.sns.handler;

import java.net.URI;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;

/**
 * AWS lambda request (object) handler that uses
 * {@link com.jrestless.core.container.JRestlessHandlerContainer} and so Jersey
 * to process incoming requests from other Lambda functions.
 * <p>
 * Implementations must provide a no-args constructor.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class SnsRequestObjectHandler extends SnsRequestHandler implements RequestHandler<SNSEvent, Void> {

	protected SnsRequestObjectHandler() {
		super();
	}

	protected SnsRequestObjectHandler(URI baseUri) {
		super(baseUri);
	}

	@Override
	public Void handleRequest(SNSEvent snsEvent, Context context) {
		for (SNSRecord snsRecord : snsEvent.getRecords()) {
			delegateRequest(new SnsRecordAndLambdaContext(snsRecord, context));
		}
		return null;
	}
}
