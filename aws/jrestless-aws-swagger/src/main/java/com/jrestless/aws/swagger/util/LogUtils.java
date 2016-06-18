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
package com.jrestless.aws.swagger.util;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.jrestless.aws.swagger.OperationContext;

import io.swagger.models.Operation;

/**
 * Utility class for logging.
 *
 * @author Bjoern Bilger
 *
 */
public final class LogUtils {

	private LogUtils() {
		// no instance
	}

	/**
	 * Logs the message when the value is requested.
	 *
	 * @param msg
	 * @param logMethod
	 * 		the log method e.g. log::error, log::warn, ...
	 * @return
	 */
	public static Supplier<String> logOnSupply(String msg, Consumer<String> logMethod) {
		return () -> {
			logMethod.accept(msg);
			return msg;
		};
	}

	/**
	 * Logs the message to error level when the value is requested.
	 *
	 * @param msg
	 * @param log
	 * @return
	 */
	public static Supplier<String> logOnSupply(String msg, LogAdapter log) {
		return logOnSupply(msg, log::error);
	}

	/**
	 * Logs the message and returns the msg.
	 *
	 * @param msg
	 * @param logMethod
	 * 		the log method e.g. log::error, log::warn, ...
	 * @return
	 */
	public static String logAndReturn(String msg, Consumer<String> logMethod) {
		return logOnSupply(msg, logMethod).get();
	}

	/**
	 * Logs the message to error level and returns the message.
	 *
	 * @param msg
	 * @param log
	 * @return
	 */
	public static String logAndReturn(String msg, LogAdapter log) {
		return logOnSupply(msg, log::error).get();
	}

	/**
	 * Creates an human readable identifier from the operation context.
	 * <p>
	 * The purpose is to be able to log on which endpoint something went wrong.
	 *
	 * @param context
	 * @param keyValues
	 * 		additional logging information (key value pairs; preferable in the format {@code key='value'})
	 * @return
	 */
	public static String createLogIdentifier(OperationContext context, String... keyValues) {
		StringBuilder strBuilder = new StringBuilder();
		Operation operation = context.getOperation();
		Method endpointMethod = context.getEndpointMethod();

		strBuilder.append("[operationId='");
		strBuilder.append(operation.getOperationId());
		strBuilder.append("' resourceClass='");
		if (endpointMethod != null) {
			strBuilder.append(endpointMethod.getDeclaringClass().getSimpleName());
		} else {
			strBuilder.append("NULL");
		}
		strBuilder.append("' endpointMethod='");
		if (endpointMethod != null) {
			strBuilder.append(endpointMethod.getName());
		} else {
			strBuilder.append("NULL");
		}

		strBuilder.append("'");

		for (String keyValue : keyValues) {
			strBuilder.append(" ");
			strBuilder.append(keyValue);
		}
		strBuilder.append("]");
		return strBuilder.toString();
	}
}
