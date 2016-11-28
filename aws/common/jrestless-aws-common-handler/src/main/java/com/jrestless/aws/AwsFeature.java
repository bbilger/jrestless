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

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.dpi.LambdaContextFactory;

/**
 * Binds AWS specific values.
 *
 * <table border="1" summary="injected objects">
 * <tr>
 * <th>injectable object
 * <th>proxiable
 * <th>scope
 * <th>factory
 * </tr>
 *
 * <tr>
 * <td>{@link Context}
 * <td>true
 * <td>request
 * <td>{@link LambdaContextFactory}
 * </tr>
 * </table>
 *
 * @author Bjoern Bilger
 *
 */
public class AwsFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(LambdaContextFactory.class)
					.to(Context.class)
					.proxy(true)
					.proxyForSameScope(false)
					.in(RequestScoped.class);
			}
		});
		return true;
	}

}
