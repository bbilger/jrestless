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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;

/**
 * Container for the sns record and the lambda context of a request.
 *
 * @author Bjoern Bilger
 *
 */
public class SnsRecordAndLambdaContext {
	private final SNSRecord snsRecord;
	private final Context lambdaContext;

	public SnsRecordAndLambdaContext(SNSRecord snsRecord, Context lambdaContext) {
		this.snsRecord = snsRecord;
		this.lambdaContext = lambdaContext;
	}

	public SNSRecord getSnsRecord() {
		return snsRecord;
	}

	public Context getLambdaContext() {
		return lambdaContext;
	}

}
