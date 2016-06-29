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
package com.jrestless.aws;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.dpi.ApiGatewayContextFactory;
import com.jrestless.aws.dpi.LambdaContextFactory;
import com.jrestless.aws.filter.IsDefaultResponseFilter;

/**
 * Jersey application configuration with AWS specifics.
 * <ol>
 * <li>{@link LambdaContextFactory}
 * <li>{@link ApiGatewayContextFactory}
 * <li>{@link IsDefaultResponseFilter}
 * <li>{@link RolesAllowedDynamicFeature}
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayResourceConfig extends ResourceConfig {

	public GatewayResourceConfig() {
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(LambdaContextFactory.class).to(Context.class).in(RequestScoped.class);

			}
		});
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(ApiGatewayContextFactory.class).to(GatewayRequestContext.class).in(RequestScoped.class);

			}
		});
		register(IsDefaultResponseFilter.class);
		register(RolesAllowedDynamicFeature.class);
	}
}
