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
package com.jrestless.aws.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to declare HTTP status codes on an endpoint.
 * <p>
 * The annotation deals with two limitations of AWS's API Gateway:
 * <ol>
 * <li>Only the default status code ({@link #defaultCode()} can have dynamic
 * headers. There's no way in Swagger/JAX-RS to declare a default status code.
 * <li>All status codes must be declared. This can be declared with Swagger's
 * ApiResponse, too but declaring it via {@link #additionalCodes()} is easier
 * and can be done on a class level. This is especially helpful for common error
 * responses.
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusCodes {

	int OK = 200;
	int BAD_REQUEST = 400;
	int UNAUTHORIZED = 401;
	int FORBIDDEN = 403;
	int NOT_FOUND = 404;
	int METHOD_NOT_ALLOWED = 405;
	int NOT_ACCEPTABLE = 406;
	int UNSUPPORTED_MEDIA_TYPE = 415;
	int INTERNAL_SERVER_ERROR = 500;

	/**
	 * The endpoint's default status code.
	 * <p>
	 * Only default responses can have dynamic headers.
	 *
	 * @return
	 */
	int defaultCode() default OK;

	/**
	 * Additional status codes for the endpoint.
	 * <p>
	 * All possible status codes must be listed, here or via Swagger's
	 * ApiResponse annotation.
	 *
	 * @return
	 */
	int[] additionalCodes() default { BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, METHOD_NOT_ALLOWED,
			NOT_ACCEPTABLE, UNSUPPORTED_MEDIA_TYPE, INTERNAL_SERVER_ERROR };
}
