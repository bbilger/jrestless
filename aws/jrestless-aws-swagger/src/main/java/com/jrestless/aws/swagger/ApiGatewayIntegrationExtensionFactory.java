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
package com.jrestless.aws.swagger;

import javax.annotation.Nonnull;

import com.jrestless.aws.swagger.models.ApiGatewayIntegrationExtension;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration;

/**
 * Factory for {@link ApiGatewayIntegrationExtension AWS's API Gateway
 * Integration Extensions}.
 *
 * @author Bjoern Bilger
 *
 */
public interface ApiGatewayIntegrationExtensionFactory {
	/**
	 * Creates an {@link ApiGatewayIntegrationExtension} for the given endpoint.
	 *
	 * @param operation
	 * 		the operation to create the extension for
	 * @param endpointMethod
	 * 		the operation's/endpoint's method
	 * @param configuration
	 * 		the configuration
	 * @param swagger
	 * 		the swagger object
	 * @return
	 */
	ApiGatewayIntegrationExtension createApiGatewayExtension(@Nonnull OperationContext operationContext,
			@Nonnull AwsSwaggerConfiguration configuration);
}
