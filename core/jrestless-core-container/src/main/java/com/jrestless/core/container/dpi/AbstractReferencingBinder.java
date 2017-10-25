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
package com.jrestless.core.container.dpi;

import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Adds functionality to bind referencing factories.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class AbstractReferencingBinder extends AbstractBinder {

	/**
	 * Binds the referencingFactory to the referenceType in the request scope
	 * and allows proxying the reference type but not for the same scope.
	 * <p>
	 * It also binds a referencing factory to the referenceTypeLiteral in the
	 * requestScope.
	 *
	 * @param referenceType
	 * @param referencingFacatory
	 * @param referenceTypeLiteral
	 */
	public final <T> void bindReferencingFactory(Class<T> referenceType,
			Class<? extends ReferencingFactory<T>> referencingFacatory, GenericType<Ref<T>> referenceTypeLiteral) {
		bindFactory(referencingFacatory)
			.to(referenceType)
			.proxy(true)
			.proxyForSameScope(false)
			.in(RequestScoped.class);
		bindFactory(ReferencingFactory.<T>referenceFactory())
			.to(referenceTypeLiteral)
			.in(RequestScoped.class);
	}
}
