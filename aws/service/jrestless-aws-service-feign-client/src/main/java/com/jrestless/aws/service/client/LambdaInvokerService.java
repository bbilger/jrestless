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

import com.amazonaws.services.lambda.invoke.LambdaFunction;
import com.jrestless.aws.service.io.DefaultServiceResponse;
import com.jrestless.aws.service.io.ServiceRequest;

/**
 * Lambda service interface required by
 * {@link com.amazonaws.services.lambda.invoke.LambdaInvokerFactory} to create a
 * proxy to invoke a lambda function.
 *
 * @author Bjoern Bilger
 *
 */
interface LambdaInvokerService {
	@LambdaFunction
	DefaultServiceResponse execute(ServiceRequest request);
}
