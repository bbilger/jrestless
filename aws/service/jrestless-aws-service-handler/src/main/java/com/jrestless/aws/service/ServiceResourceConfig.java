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
package com.jrestless.aws.service;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.service.dpi.LambdaContextFactory;
import com.jrestless.aws.service.dpi.ServiceRequestContextFactory;
import com.jrestless.aws.service.io.ServiceRequest;

/**
 * Jersey application configuration with jrestless AWS service specifics.
 * <ol>
 * <li>{@link LambdaContextFactory}
 * <li>{@link ServiceRequestContextFactory}
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
public class ServiceResourceConfig extends ResourceConfig {

	public ServiceResourceConfig() {
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(LambdaContextFactory.class)
					.to(Context.class)
					.in(RequestScoped.class);
			}
		});
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(ServiceRequestContextFactory.class)
					.to(ServiceRequest.class)
					.in(RequestScoped.class);
			}
		});
	}
}
