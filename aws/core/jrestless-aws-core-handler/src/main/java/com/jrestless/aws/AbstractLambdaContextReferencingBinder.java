/*
 * Copyright 2017 Bjoern Bilger
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
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.core.container.dpi.AbstractReferencingBinder;

/**
 * This {@link AbstractReferencingBinder} allows to easily bind a
 * {@link ReferencingFactory} for {@link Context} via
 * {@link #bindReferencingLambdaContextFactory()}. This method should be called
 * by subclasses in {@link #configure()}.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class AbstractLambdaContextReferencingBinder extends AbstractReferencingBinder {

	public static final Type LAMBDA_CONTEXT_TYPE = (new GenericType<Ref<Context>>() { }).getType();

	protected final void bindReferencingLambdaContextFactory() {
		bindReferencingFactory(Context.class, ReferencingLambdaContextFactory.class,
				new GenericType<Ref<Context>>() { });
	}

	private static class ReferencingLambdaContextFactory extends ReferencingFactory<Context> {
		@Inject
		ReferencingLambdaContextFactory(Provider<Ref<Context>> referenceFactory) {
			super(referenceFactory);
		}
	}
}
