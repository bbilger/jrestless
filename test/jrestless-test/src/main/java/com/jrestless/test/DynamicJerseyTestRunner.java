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
package com.jrestless.test;

import org.glassfish.jersey.test.JerseyTest;

/**
 * Utility class to dynamically invoke a {@link JerseyTest}.
 * <p>
 * The problem with extending {@link JerseyTest} is that the registered
 * {@link org.glassfish.jersey.server.ResourceConfig} cannot be changed between tests.
 *
 * @author Bjoern Bilger
 *
 */
public class DynamicJerseyTestRunner {

	/**
	 * <ol>
	 * <li>calls {@link JerseyTest#setUp()}
	 * <li>passes the initialized JerseyTest to the consumer.
	 * <li>calls {@link JerseyTest#tearDown()}
	 * </ol>
	 *
	 * @param jerseyTest
	 * @param test
	 * @throws Exception
	 */
	public void runJerseyTest(JerseyTest jerseyTest, ThrowingConsumer<JerseyTest> test) throws Exception {
		try {
			jerseyTest.setUp();
			test.accept(jerseyTest);
		} finally {
			jerseyTest.tearDown();
		}
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T> {
		void accept(T in) throws Exception;
	}
}
