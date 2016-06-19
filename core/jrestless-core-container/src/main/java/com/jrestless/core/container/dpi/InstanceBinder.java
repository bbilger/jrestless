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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Simple HK2 instance binder.
 * <p>
 * Use the {@link Builder builder} to create an instance binder.
 *
 * @author Bjoern Bilger
 *
 */
public final class InstanceBinder extends AbstractBinder {

	private final Collection<InstanceFactory<?>> factories;

	private InstanceBinder(Collection<InstanceFactory<?>> factories) {
		this.factories = factories;
	}

	@Override
	protected void configure() {
		for (InstanceFactory<?> factory : factories) {
			bindFactory(factory).to(factory.getType()).in(factory.getScope());
		}
	}
	/**
	 * Builder to create an {@link InstanceBinder}.
	 *
	 * @author Bjoern Bilger
	 *
	 */
	public static class Builder {

		private List<InstanceFactory<?>> factories = new ArrayList<>();

		/**
		 * Add an instance for the given type and scope.
		 *
		 * @param instanceFactory
		 * @return
		 */
		public <T> Builder addInstance(InstanceFactory<T> instanceFactory) {
			factories.add(instanceFactory);
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
			return addInstance(new InstanceFactory<T>(instance, type, scope));
		}

		/**
		 * Creates an {@link InstanceBinder} with all added instances.
		 *
		 * @return
		 */
		public InstanceBinder build() {
			return new InstanceBinder(new LinkedList<>(factories));
		}
	}

	public static class InstanceFactory<T> implements Factory<T> {

		private final T instance;
		private final Class<T> type;
		private final Class<? extends Annotation> scope;

		public InstanceFactory(T instance, Class<T> type, Class<? extends Annotation> scope) {
			Objects.requireNonNull(instance);
			Objects.requireNonNull(type);
			Objects.requireNonNull(scope);
			this.instance = instance;
			this.type = type;
			this.scope = scope;
		}

		public Class<? extends Annotation> getScope() {
			return scope;
		}

		public Class<T> getType() {
			return type;
		}

		@Override
		public void dispose(T t) {
		}

		@Override
		public T provide() {
			return instance;
		}
	}
}
