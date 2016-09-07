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
package com.jrestless.aws.io;

/**
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayNonDefaultHeaderException extends Exception {

	private static final long serialVersionUID = 4966841797589606234L;

	protected GatewayNonDefaultHeaderException(String headerValue, GatewayNonDefaultHeaderException nextHeader) {
		super(headerValue, nextHeader, false, false);
	}
}
