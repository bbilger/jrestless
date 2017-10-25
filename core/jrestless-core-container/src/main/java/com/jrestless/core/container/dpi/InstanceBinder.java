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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Simple instance binder.
 * <p>
 * Use the {@link Builder builder} to create an instance binder.
 *
 * @author Bjoern Bilger
 *
 */
public final class InstanceBinder extends AbstractBinder {

	private final Collection<InstanceFactoryDefinition<?>> factoryDefinitions;

	private InstanceBinder(Collection<InstanceFactoryDefinition<?>> factoryDefinitions) {
		this.factoryDefinitions = factoryDefinitions;
	}

	@Override
	protected void configure() {
		for (InstanceFactoryDefinition<?> factory : factoryDefinitions) {
			bindFactory(factory.getInstanceFactory()).to(factory.getType()).in(factory.getScope());
		}
	}
	/**
	 * Builder to create an {@link InstanceBinder}.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	public static class Builder {

		private List<InstanceFactoryDefinition<?>> factoryDefinitions = new ArrayList<>();

		/**
		 * Add an instance for the given type and scope.
		 *
		 * @param instanceFactory
		 * @return
		 */
		public <T> Builder addInstance(InstanceFactoryDefinition<T> instanceFactory) {
			factoryDefinitions.add(instanceFactory);
			return this;
		}

		/**
		 * Add a request-scoped instance for the given type.
		 *
		 * @param instance
		 * @param type
		 * @return
		 */
		public <T> Builder addInstance(T instance, Class<T> type) {
			return addInstance(instance, type, RequestScoped.class);
		}

		/**
		 * Add an instance for the given type and scope.
		 *
		 * @param instance
		 * @param type
		 * @param scope
		 * @return
		 */
		public <T> Builder addInstance(T instance, Class<T> type, Class<? extends Annotation> scope) {
			return addInstance(new InstanceFactoryDefinition<T>(instance, type, scope));
		}

		/**
		 * Creates an {@link InstanceBinder} with all added instances.
		 *
		 * @return
		 */
		public InstanceBinder build() {
			return new InstanceBinder(new LinkedList<>(factoryDefinitions));
		}
	}

	static class InstanceFactoryDefinition<T> {

		private final Supplier<T> instanceFactory;
		private final Class<T> type;
		private final Class<? extends Annotation> scope;

		InstanceFactoryDefinition(T instance, Class<T> type, Class<? extends Annotation> scope) {
			this(wrap(instance), type, scope);
		}

		private static <T> Supplier<T> wrap(T instance) {
			requireNonNull(instance);
			return () -> instance;
		}

		InstanceFactoryDefinition(Supplier<T> instanceSupplier, Class<T> type, Class<? extends Annotation> scope) {
			this.instanceFactory = requireNonNull(instanceSupplier);
			this.type = requireNonNull(type);
			this.scope = requireNonNull(scope);
		}

		Class<? extends Annotation> getScope() {
			return scope;
		}

		Class<T> getType() {
			return type;
		}

		Supplier<T> getInstanceFactory() {
			return instanceFactory;
		}
	}
}
