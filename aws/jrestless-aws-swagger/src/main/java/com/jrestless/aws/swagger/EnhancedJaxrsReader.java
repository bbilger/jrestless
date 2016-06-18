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
package com.jrestless.aws.swagger;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;

/**
 * Extension of {@link JaxrsReader} providing hooks after operation and Swagger
 * creation finished.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class EnhancedJaxrsReader extends JaxrsReader {

	private final Map<Operation, Method> operationMethodTracker = new IdentityHashMap<>();
	private final SwaggerEnhancer swaggerEnhancer;

	public EnhancedJaxrsReader(Swagger swagger, LogAdapter log, SwaggerEnhancer swaggerEnhancer) {
		super(swagger, log);
		requireNonNull(swaggerEnhancer);
		this.swaggerEnhancer = swaggerEnhancer;
	}

	@Override
	public final Operation parseMethod(Method endpointMethod) {
		Operation operation = super.parseMethod(endpointMethod);
		/*
		 * There's no hook in JaxrsReader that allows us to operate on the
		 * Operation and the Method when the Operation creation is finished
		 * completely. For example Operation#produces gets updated after
		 * invocation of this method. So keep track of them and handle them in
		 * #onOperationCreationFinished after JaxrsReader#read is finished.
		 * (note: we want the method, too in case we introduce some more
		 * annotations)
		 */
		operationMethodTracker.put(operation, endpointMethod);
		return operation;
	}

	@Override
	public final Swagger read(Set<Class<?>> classes) {
		operationMethodTracker.clear();
		Swagger swagger = super.read(classes);
		swaggerEnhancer.onSwaggerCreationFinished(swagger, (o) -> operationMethodTracker.get(o));
		return swagger;
	}

	@Override
	protected final Swagger read(Class<?> cls, String parentPath, String parentMethod, boolean readHidden,
			String[] parentConsumes, String[] parentProduces, Map<String, Tag> parentTags,
			List<Parameter> parentParameters) {
		Swagger swagger = super.read(cls, parentPath, parentMethod, readHidden, parentConsumes, parentProduces,
				parentTags, parentParameters);

		for (Map.Entry<Operation, Method> opMethEntry : operationMethodTracker.entrySet()) {
			Method endpointMethod = opMethEntry.getValue();
			if (cls.equals(endpointMethod.getDeclaringClass())) {
				swaggerEnhancer.onOperationCreationFinished(
						new OperationContext(opMethEntry.getKey(), endpointMethod, swagger));
			}
		}
		return swagger;
	}

}
