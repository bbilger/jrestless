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

import java.lang.reflect.Method;
import java.util.function.Function;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

/**
 * Strategy for enhancing a Swagger configuration after it has been created by a
 * {@link com.github.kongchen.swagger.docgen.reader.AbstractReader reader}.
 * <p>
 * Note: At the moment only the {@link EnhancedJaxrsReader} supports this.
 *
 * @author Bjoern Bilger
 *
 */
public interface SwaggerEnhancer {
	/**
	 * Invoked after the operation has been created completely.
	 *
	 * @param operationContext
	 */
	void onOperationCreationFinished(OperationContext operationContext);
	/**
	 * Invoked after the {@link Swagger} object has been created completely.
	 *
	 * @param swagger
	 * @param operationMethodMapper
	 */
	void onSwaggerCreationFinished(Swagger swagger, Function<Operation, Method> operationMethodMapper);
}
