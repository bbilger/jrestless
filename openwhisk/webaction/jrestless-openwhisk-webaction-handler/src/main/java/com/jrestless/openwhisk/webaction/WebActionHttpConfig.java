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
package com.jrestless.openwhisk.webaction;

import com.jrestless.openwhisk.webaction.io.WebActionBase64WriteInterceptor;

/**
 * This resource config registers required and recommended filters and
 * interceptors for reading (raw) Web Action requests and writing (raw) Web
 * Action responses intended for "http response types".
 * <p>
 * The following filters and interceptors are registered:
 * <ol>
 * <li>{@link com.jrestless.openwhisk.webaction.io.WebActionBase64ReadInterceptor}
 * <li>{@link WebActionBase64WriteInterceptor}
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
public class WebActionHttpConfig extends WebActionConfig {
	public WebActionHttpConfig() {
		super();
		register(WebActionBase64WriteInterceptor.class);
	}
}
