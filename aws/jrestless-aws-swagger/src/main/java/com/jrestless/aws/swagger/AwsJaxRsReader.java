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

import com.github.kongchen.swagger.docgen.LogAdapter;

import io.swagger.models.Swagger;

/**
 * <p>
 * Enhanced {@link com.github.kongchen.swagger.docgen.reader.JaxrsReader
 * JaxrsReader} reader that writes the AWS API Gateway specific Swagger
 * extension "x-amazon-apigateway-integration" and (iff configured)
 * "x-amazon-apigateway-auth".
 * <p>
 * It must be configured via
 * {@link com.jrestless.aws.swagger.models.AwsSwaggerConfiguration
 * AwsSwaggerConfiguration}. A file path to a serialized JSON object of it must
 * be passed as property "aws-swagger-configuration".
 *
 * @author Bjoern Bilger
 *
 */
public class AwsJaxRsReader extends EnhancedJaxrsReader {

	public AwsJaxRsReader(Swagger swagger, LogAdapter log) {
		super(swagger, log, new AwsSwaggerEnhancer(log));
	}

}
