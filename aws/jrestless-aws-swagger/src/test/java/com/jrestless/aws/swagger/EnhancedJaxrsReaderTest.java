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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.function.Function;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.collect.ImmutableSet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class EnhancedJaxrsReaderTest {

	private EnhancedJaxrsReader reader;
	private SwaggerEnhancer enhancer;
	private Swagger swagger;

	@Before
	public void setup() {
		enhancer = mock(SwaggerEnhancer.class);
		swagger = new Swagger();
		reader = new ReaderImpl(swagger, mock(LogAdapter.class), enhancer);
	}

	@Test
	public void read_ResourceClassesGiven_ShouldInvokeOnOperationCreationFinishedForEachEndpoint()
			throws NoSuchMethodException, SecurityException {

		Swagger swagger = reader.read(ImmutableSet.of(Resource0.class, Resource1.class));

		Operation getOperation = swagger.getPath("/get").getGet();
		Operation postOperation = swagger.getPath("/post").getPost();
		Operation putOperation = swagger.getPath("/put").getPut();
		Operation deleteOperation = swagger.getPath("/delete").getDelete();

		verify(enhancer, times(4)).onOperationCreationFinished(any());
		verify(enhancer, times(1)).onOperationCreationFinished(
				new OperationContext(getOperation, Resource0.class.getMethod("endpoint00"), swagger));
		verify(enhancer, times(1)).onOperationCreationFinished(
				new OperationContext(postOperation, Resource0.class.getMethod("endpoint01"), swagger));
		verify(enhancer, times(1)).onOperationCreationFinished(
				new OperationContext(putOperation, Resource1.class.getMethod("endpoint10"), swagger));
		verify(enhancer, times(1)).onOperationCreationFinished(
				new OperationContext(deleteOperation, Resource1.class.getMethod("endpoint11"), swagger));
	}

	@Test
	public void read_ResourceClassesGiven_ShouldInvokeOnSwaggerCreationFinished() {
		Swagger swagger = reader.read(ImmutableSet.of(Resource0.class, Resource1.class));
		verify(enhancer, times(1)).onSwaggerCreationFinished(eq(swagger), any());
	}

	@Test
	public void read_ResourceClassesGiven_ShouldInvokeOnSwaggerCreationFinishedAfterOnOperationCreationFinished() {
		reader.read(ImmutableSet.of(Resource0.class, Resource1.class));
		InOrder order = inOrder(enhancer);
		order.verify(enhancer, times(4)).onOperationCreationFinished(any());
		order.verify(enhancer, times(1)).onSwaggerCreationFinished(any(), any());
	}

	@Test
	public void read_ResourceClassesGiven_ShouldExposeEndpointMethodForAllResourcesInSet() throws NoSuchMethodException, SecurityException {
		EnhancedJaxrsReader reader = new ReaderImpl(swagger, mock(LogAdapter.class), new SwaggerEnhancer() {
			@Override
			public void onSwaggerCreationFinished(Swagger swagger, Function<Operation, Method> operationMethodMapper) {

				Operation getOperation = swagger.getPath("/get").getGet();
				Operation postOperation = swagger.getPath("/post").getPost();
				Operation putOperation = swagger.getPath("/put").getPut();
				Operation deleteOperation = swagger.getPath("/delete").getDelete();

				try {
					assertEquals(Resource0.class.getMethod("endpoint00"), operationMethodMapper.apply(getOperation));
					assertEquals(Resource0.class.getMethod("endpoint01"), operationMethodMapper.apply(postOperation));
					assertEquals(Resource1.class.getMethod("endpoint10"), operationMethodMapper.apply(putOperation));
					assertEquals(Resource1.class.getMethod("endpoint11"), operationMethodMapper.apply(deleteOperation));
				} catch (NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void onOperationCreationFinished(OperationContext operationContext) {

			}
		});
		reader.read(ImmutableSet.of(Resource0.class, Resource1.class));
	}

	@Test
	public void read_ResourceClassesGiven_ShouldExposeEndpointMethodForResourcesInSetOnly() throws NoSuchMethodException, SecurityException {
		Class<?> resourceClass[] = new Class<?>[1];
		EnhancedJaxrsReader reader = new ReaderImpl(swagger, mock(LogAdapter.class), new SwaggerEnhancer() {
			@Override
			public void onSwaggerCreationFinished(Swagger swagger, Function<Operation, Method> operationMethodMapper) {
				Operation getOperation = swagger.getPath("/get").getGet();
				Operation postOperation = swagger.getPath("/post").getPost();

				if (Resource0.class.equals(resourceClass[0])) {
					try {
						assertEquals(Resource0.class.getMethod("endpoint00"), operationMethodMapper.apply(getOperation));
						assertEquals(Resource0.class.getMethod("endpoint01"), operationMethodMapper.apply(postOperation));
					} catch (NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				} else {
					Operation putOperation = swagger.getPath("/put").getPut();
					Operation deleteOperation = swagger.getPath("/delete").getDelete();
					try {
						assertEquals(null, operationMethodMapper.apply(getOperation));
						assertEquals(null, operationMethodMapper.apply(postOperation));
						assertEquals(Resource1.class.getMethod("endpoint10"), operationMethodMapper.apply(putOperation));
						assertEquals(Resource1.class.getMethod("endpoint11"), operationMethodMapper.apply(deleteOperation));
					} catch (NoSuchMethodException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void onOperationCreationFinished(OperationContext operationContext) {
			}
		});
		resourceClass[0] = Resource0.class;
		reader.read(ImmutableSet.of(Resource0.class));
		resourceClass[0] = Resource1.class;
		reader.read(ImmutableSet.of(Resource1.class));
	}

	@Path("/")
	@Api
	public static class Resource0 {
		@GET
		@Path("/get")
		@ApiOperation(value = "get")
		public void endpoint00() {

		}
		@POST
		@Path("/post")
		@ApiOperation(value = "post")
		public void endpoint01() {

		}
	}

	@Path("/")
	@Api
	public static class Resource1 {
		@PUT
		@Path("/put")
		@ApiOperation(value = "put")
		public void endpoint10() {

		}
		@DELETE
		@Path("/delete")
		@ApiOperation(value = "delete")
		public void endpoint11() {

		}

		public void noEndpoint() {

		}
	}

	private static class ReaderImpl extends EnhancedJaxrsReader {
		public ReaderImpl(Swagger swagger, LogAdapter log, SwaggerEnhancer swaggerEnhancer) {
			super(swagger, log, swaggerEnhancer);
		}
	}
}
