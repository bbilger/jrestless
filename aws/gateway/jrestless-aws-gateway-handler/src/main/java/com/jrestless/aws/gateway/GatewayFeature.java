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
package com.jrestless.aws.gateway;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import com.jrestless.aws.AwsFeature;
import com.jrestless.aws.gateway.io.GatewayBinaryReadInterceptor;
import com.jrestless.aws.gateway.io.GatewayBinaryResponseCheckFilter;
import com.jrestless.aws.gateway.io.GatewayBinaryWriteInterceptor;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.security.CognitoUserPoolAuthorizerFilter;
import com.jrestless.aws.gateway.security.CustomAuthorizerFilter;
import com.jrestless.core.container.dpi.AbstractReferencingBinder;

/**
 * Binds Gateway specific values and registers Gateway specific features.
 *
 * Injected objects:
 *
 * <table border="1" summary="injected objects">
 * <tr>
 * <th>injectable object
 * <th>proxiable
 * <th>scope
 * </tr>
 *
 * <tr>
 * <td>{@link GatewayRequest}
 * <td>true
 * <td>request
 * </tr>
 *
 * </table>
 * <p>
 * Registers features:
 * <ul>
 * <li>{@link AwsFeature}
 * <li>{@link GatewayBinaryReadInterceptor}
 * <li>{@link GatewayBinaryResponseCheckFilter}
 * <li>{@link GatewayBinaryWriteInterceptor}
 * <li>{@link CustomAuthorizerFilter}
 * <li>{@link CognitoUserPoolAuthorizerFilter}
 * </ul>
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayFeature implements Feature {

	public static final Type GATEWAY_REQUEST_TYPE = (new TypeLiteral<Ref<GatewayRequest>>() { }).getType();

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new Binder());
		context.register(GatewayBinaryReadInterceptor.class);
		context.register(GatewayBinaryResponseCheckFilter.class);
		context.register(GatewayBinaryWriteInterceptor.class);
		context.register(CustomAuthorizerFilter.class);
		context.register(CognitoUserPoolAuthorizerFilter.class);
		context.register(AwsFeature.class);
		return true;
	}

	private static class Binder extends AbstractReferencingBinder {
		@Override
		protected void configure() {
			bindReferencingFactory(GatewayRequest.class, ReferencingGatewayRequestFactory.class,
					new TypeLiteral<Ref<GatewayRequest>>() { });
		}
	}

	private static class ReferencingGatewayRequestFactory extends ReferencingFactory<GatewayRequest> {
		@Inject
		ReferencingGatewayRequestFactory(final Provider<Ref<GatewayRequest>> referenceFactory) {
			super(referenceFactory);
		}
	}
}
