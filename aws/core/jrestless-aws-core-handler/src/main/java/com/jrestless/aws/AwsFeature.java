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

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.core.container.dpi.AbstractReferencingBinder;

/**
 * Binds AWS specific values.
 *
 * <table border="1" summary="injected objects">
 * <tr>
 * <th>injectable object
 * <th>proxiable
 * <th>scope
 * </tr>
 *
 * <tr>
 * <td>{@link Context}
 * <td>true
 * <td>request
 * </tr>
 * </table>
 *
 * @author Bjoern Bilger
 *
 */
public class AwsFeature implements Feature {

	public static final Type CONTEXT_TYPE = (new TypeLiteral<Ref<Context>>() { }).getType();

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new Binder());
		return true;
	}

	private static class Binder extends AbstractReferencingBinder {
		@Override
		protected void configure() {
			bindReferencingFactory(Context.class, ReferencingContextFactory.class, new TypeLiteral<Ref<Context>>() { });
		}
	}

	private static class ReferencingContextFactory extends ReferencingFactory<Context> {
		@Inject
		ReferencingContextFactory(Provider<Ref<Context>> referenceFactory) {
			super(referenceFactory);
		}
	}

}
