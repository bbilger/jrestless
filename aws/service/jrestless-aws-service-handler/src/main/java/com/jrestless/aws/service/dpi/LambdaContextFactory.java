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
package com.jrestless.aws.service.dpi;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.core.container.dpi.ContainerRequestContextPropertyFactory;

/**
 * Factory for fetching {@link Context} from the {@link ContainerRequestContext}
 * via the property name {@code 'awsLambdaContext'}.
 *
 * @author Bjoern Bilger
 *
 */
public class LambdaContextFactory extends ContainerRequestContextPropertyFactory<Context> {

	public static final String PROPERTY_NAME = "awsLambdaContext";

	@Inject
	public LambdaContextFactory(ContainerRequestContext context) {
		super(context, PROPERTY_NAME);
	}
}
