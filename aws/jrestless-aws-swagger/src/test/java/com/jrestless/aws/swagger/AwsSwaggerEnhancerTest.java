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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.security.RolesAllowed;

import org.junit.Before;
import org.junit.Test;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jrestless.annotation.Cors;
import com.jrestless.aws.swagger.models.ApiGatewayAuth;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationExtension;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration.AuthType;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class AwsSwaggerEnhancerTest {

	private AwsSwaggerEnhancer enhancer;
	private AwsSwaggerConfiguration configuration;
	private ApiGatewayIntegrationExtensionFactory xFactory;
	private LogAdapter log;

	@Before
	public void setup() {
		configuration = mock(AwsSwaggerConfiguration.class);
		xFactory = mock(ApiGatewayIntegrationExtensionFactory.class);
		log = mock(LogAdapter.class);
		enhancer = spy(new AwsSwaggerEnhancer(xFactory, configuration, log));
	}

	@Test
	public void getResponseContentType_NoContentTypeGiven_ShouldAssumeJson() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = mock(Operation.class);
		when(context.getOperation()).thenReturn(operation);
		when(operation.getProduces()).thenReturn(null);
		String contentType = enhancer.getContentType(context);
		assertEquals("application/json", contentType);
	}

	@Test
	public void getResponseContentType_OneContentTypeGiven_ShouldReturnTheContentType() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = mock(Operation.class);
		when(context.getOperation()).thenReturn(operation);
		when(operation.getProduces()).thenReturn(Collections.singletonList("someContentType"));
		String contentType = enhancer.getContentType(context);
		assertEquals("someContentType", contentType);
	}

	@Test(expected = RuntimeException.class)
	public void getResponseContentType_MultipleContentTypesGiven_ShouldThrowException() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = mock(Operation.class);
		when(context.getOperation()).thenReturn(operation);
		when(operation.getProduces()).thenReturn(ImmutableList.of("someContentType1", "someContentType2"));
		enhancer.getContentType(context);
	}

	@Test
	public void addContentTypeHeaders_ContentTypeHeaderGiven_ShouldNotOverwrite() {
		Response response = new Response();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("'some cc'");
		response.addHeader("Content-Type", headerProp);
		OperationContext context = mock(OperationContext.class);
		doReturn("some other cc").when(enhancer).getContentType(context);
		enhancer.addContentTypeHeader(response, context);
		assertEquals("'some cc'", response.getHeaders().get("Content-Type").getDescription());
	}

	@Test
	public void addContentTypeHeaders_NullHeadersGiven_ShouldSetContentTypeHeader() {
		Response response = new Response();
		OperationContext context = mock(OperationContext.class);
		doReturn("some other cc").when(enhancer).getContentType(context);
		enhancer.addContentTypeHeader(response, context);
		assertEquals("'some other cc'", response.getHeaders().get("Content-Type").getDescription());
	}

	@Test
	public void addContentTypeHeaders_NoContentTypeHeaderGiven_ShouldSetContentTypeHeader() {
		Response response = new Response();
		response.setHeaders(new HashMap<>());
		OperationContext context = mock(OperationContext.class);
		doReturn("some other cc").when(enhancer).getContentType(context);
		enhancer.addContentTypeHeader(response, context);
		assertEquals("'some other cc'", response.getHeaders().get("Content-Type").getDescription());
	}

	@Test
	public void addAdditionalResponses_NoResponsesAndNoDefaultConfiguredGiven_ShouldAddDefault() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		op.setResponses(new HashMap<>());
		Method meth = SampleResource.class.getMethod("method");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of("400", "401", "500", "403", "404", "415", "405", "406"), op.getResponses().keySet());
	}

	@Test
	public void addAdditionalResponses_NoAdditionalResponsesAndNoDefaultConfiguredGiven_ShouldAddDefault() throws NoSuchMethodException, SecurityException {
		addAdditionalResponses_NoResponsesAndNoDefaultConfiguredGiven_ShouldAddDefault();
	}

	@Test
	public void addAdditionalResponses_AdditionalResponsesAndNoDefaultConfiguredGiven_ShouldNotAddDefault() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		op.setResponses(new HashMap<>());
		Method meth = SampleResource.class.getMethod("method1");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of(), op.getResponses().keySet());
	}

	@Test
	public void addAdditionalResponses_DefaultResponseAndNoAdditionalResponsesAndNoDefaultConfiguredGiven_ShouldNotOverwriteDefaultResponse() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		Map<String, Response> responses = new HashMap<>();
		Response defaultResponse = new Response();
		responses.put("400", defaultResponse);
		op.setResponses(responses);
		Method meth = SampleResource.class.getMethod("method");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of("400", "401", "500", "403", "404", "415", "405", "406"), op.getResponses().keySet());
		assertSame(defaultResponse, responses.get("400"));
	}

	@Test
	public void addAdditionalResponses_NoResponsesAndDefaultConfiguredGiven_ShouldAddConfigured() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		op.setResponses(new HashMap<>());
		when(configuration.isSetAdditionalResponseCodes()).thenReturn(true);
		when(configuration.getAdditionalResponseCodes()).thenReturn(new int[] { 123 });
		Method meth = SampleResource.class.getMethod("method");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of("123"), op.getResponses().keySet());
	}

	@Test
	public void addAdditionalResponses_NoAdditionalResponsesAndDefaultConfiguredGiven_ShouldAddConfigured() throws NoSuchMethodException, SecurityException {
		addAdditionalResponses_NoResponsesAndDefaultConfiguredGiven_ShouldAddConfigured();
	}

	@Test
	public void addAdditionalResponses_AdditionalResponsesAndDefaultConfiguredGiven_ShouldAddConfigured() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		op.setResponses(new HashMap<>());
		when(configuration.isSetAdditionalResponseCodes()).thenReturn(true);
		when(configuration.getAdditionalResponseCodes()).thenReturn(new int[] { 123 });
		Method meth = SampleResource.class.getMethod("method1");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of(), op.getResponses().keySet());
	}

	@Test
	public void addAdditionalResponses_DefaultResponseAndNoAdditionalResponsesAndDefaultConfiguredGiven_ShouldNotOverwriteDefaultResponse() throws NoSuchMethodException, SecurityException {
		Operation op = new Operation();
		Map<String, Response> responses = new HashMap<>();
		Response defaultResponse = new Response();
		responses.put("123", defaultResponse);
		op.setResponses(responses);
		when(configuration.isSetAdditionalResponseCodes()).thenReturn(true);
		when(configuration.getAdditionalResponseCodes()).thenReturn(new int[] { 123 });
		Method meth = SampleResource.class.getMethod("method");
		OperationContext oc = new OperationContext(op, meth, new Swagger());
		enhancer.addAdditionalResponses(oc);
		assertEquals(ImmutableSet.of("123"), op.getResponses().keySet());
		assertSame(defaultResponse, responses.get("123"));
	}

	@Test
	public void setApiGatewayExtension_ContextGiven_ShouldSetExtensionFromFactory() {
		OperationContext context = mock(OperationContext.class);
		Operation op = new Operation();
		when(context.getOperation()).thenReturn(op);
		ApiGatewayIntegrationExtension integrationExtension = mock(ApiGatewayIntegrationExtension.class);
		when(xFactory.createApiGatewayExtension(context, configuration)).thenReturn(integrationExtension);
		enhancer.setApiGatewayExtension(context);
		assertEquals(Collections.singleton("x-amazon-apigateway-integration"), op.getVendorExtensions().keySet());
		assertSame(integrationExtension, op.getVendorExtensions().get("x-amazon-apigateway-integration"));
	}

	@Test
	public void getCorsAllowOrigin_NotConfiguredAndNoLogging_ShouldReturnDefaultAndNotLog() {
		when(configuration.isSetDefaultAccessControlAllowOrigin()).thenReturn(false);
		assertEquals("*", enhancer.getCorsAllowOrigin(false));
		verifyZeroInteractions(log);
	}

	@Test
	public void getCorsAllowOrigin_NotConfiguredAndLogging_ShouldReturnDefaultAndNotLog() {
		when(configuration.isSetDefaultAccessControlAllowOrigin()).thenReturn(false);
		assertEquals("*", enhancer.getCorsAllowOrigin(true));
		verify(log).warn(any());
	}

	@Test
	public void getCorsAllowOrigin_Configured_ShouldReturnConfiguredValue() {
		when(configuration.isSetDefaultAccessControlAllowOrigin()).thenReturn(true);
		when(configuration.getDefaultAccessControlAllowOrigin()).thenReturn("whatever");
		assertEquals("whatever", enhancer.getCorsAllowOrigin(false));
	}

	@Test
	public void getCorsAllowHeaders_NotConfiguredAndNoLogging_ShouldReturnDefaultAndNotLog() {
		when(configuration.isSetDefaultAccessControlAllowHeaders()).thenReturn(false);
		assertEquals("Content-Type,X-Amz-Date,Authorization,X-Api-Key", enhancer.getCorsAllowHeaders(false));
		verifyZeroInteractions(log);
	}

	@Test
	public void getCorsAllowHeaders_NotConfiguredAndLogging_ShouldReturnDefaultAndNotLog() {
		when(configuration.isSetDefaultAccessControlAllowHeaders()).thenReturn(false);
		assertEquals("Content-Type,X-Amz-Date,Authorization,X-Api-Key", enhancer.getCorsAllowHeaders(true));
		verify(log).info(any());
	}

	@Test
	public void getCorsAllowHeaders_Configured_ShouldReturnConfiguredValue() {
		when(configuration.isSetDefaultAccessControlAllowHeaders()).thenReturn(true);
		when(configuration.getDefaultAccessControlAllowHeaders()).thenReturn("whatever");
		assertEquals("whatever", enhancer.getCorsAllowHeaders(false));
	}

	@Test
	public void getMethodsAllowingCors_NoMethodAllowsCors_ShouldReturnEmptySet() {
		Path path = new Path();
		Operation getOperation = new Operation();
		Operation postOperation = new Operation();
		Function<Operation, Method> operationMethodMapper = (o) -> null;
		path.setGet(getOperation);
		path.setPost(postOperation);
		doReturn(false).when(enhancer).isCorsEnabled(any());
		assertTrue(enhancer.getMethodsAllowingCors(path, operationMethodMapper).isEmpty());
	}

	@Test
	public void getMethodsAllowingCors_OneMethodAllowsCors_ShouldReturnEnabledMethod() throws NoSuchMethodException, SecurityException {
		Path path = new Path();
		Operation getOperation = new Operation();
		Method getMethod = SampleResource.class.getMethod("method");
		Operation postOperation = new Operation();
		Method postMethod = SampleResource.class.getMethod("method1");
		Function<Operation, Method> operationMethodMapper = (o) -> {
			// identity check
			if (o == getOperation) {
				return getMethod;
			} else {
				return postMethod;
			}
		};
		path.setGet(getOperation);
		path.setPost(postOperation);
		doReturn(true).when(enhancer).isCorsEnabled(getMethod);
		doReturn(false).when(enhancer).isCorsEnabled(postMethod);
		assertEquals(Collections.singleton("GET"), enhancer.getMethodsAllowingCors(path, operationMethodMapper));
	}

	@Test
	public void getMethodsAllowingCors_AllMethodsAllowCors_ShouldReturnAllMethods() throws NoSuchMethodException, SecurityException {
		Path path = new Path();
		Operation getOperation = new Operation();
		Method getMethod = SampleResource.class.getMethod("method");
		Operation postOperation = new Operation();
		Method postMethod = SampleResource.class.getMethod("method1");
		Function<Operation, Method> operationMethodMapper = (o) -> {
			// identity check
			if (o == getOperation) {
				return getMethod;
			} else {
				return postMethod;
			}
		};
		path.setGet(getOperation);
		path.setPost(postOperation);
		doReturn(true).when(enhancer).isCorsEnabled(getMethod);
		doReturn(true).when(enhancer).isCorsEnabled(postMethod);
		assertEquals(ImmutableSet.of("GET", "POST"), enhancer.getMethodsAllowingCors(path, operationMethodMapper));
	}

	@Test
	public void onOperationCreationFinished_ContextGiven_ShouldInvokeMethods() {
		OperationContext context = mock(OperationContext.class);
		Operation op = new Operation();
		Map<String, Response> resps = new HashMap<>();
		// mock for equality check
		Response r1 = mock(Response.class);
		Response r2 = mock(Response.class);
		resps.put("1", r1);
		resps.put("2", r2);
		op.setResponses(resps);
		when(context.getOperation()).thenReturn(op);
		doNothing().when(enhancer).addAdditionalResponses(context);
		doNothing().when(enhancer).addContentTypeHeader(r1, context);
		doNothing().when(enhancer).addContentTypeHeader(r2, context);
		doNothing().when(enhancer).addCorsAllowOriginHeader(r1, context);
		doNothing().when(enhancer).addCorsAllowOriginHeader(r2, context);
		doNothing().when(enhancer).setApiGatewayExtension(context);
		doNothing().when(enhancer).setSecurityExtension(context);
		enhancer.onOperationCreationFinished(context);

		verify(enhancer).onOperationCreationFinished(context); // to be able to verifyNoMoreInteractions
		verify(enhancer).addAdditionalResponses(context);
		verify(enhancer).addContentTypeHeader(r1, context);
		verify(enhancer).addContentTypeHeader(r2, context);
		verify(enhancer).addCorsAllowOriginHeader(r1, context);
		verify(enhancer).addCorsAllowOriginHeader(r2, context);
		verify(enhancer).setApiGatewayExtension(context);
		verify(enhancer).setSecurityExtension(context);
		verifyNoMoreInteractions(enhancer);
	}

	@Test
	public void onSwaggerCreationFinished_ContextAndMapperGiven_ShouldAddCorsOperations() {
		Swagger swagger = mock(Swagger.class);
		Path p1 = mock(Path.class);
		Path p2 = mock(Path.class);
		Map<String, Path> paths = ImmutableMap.of("1", p1, "2", p2);
		when(swagger.getPaths()).thenReturn(paths);
		Function<Operation, Method> mapper = (o) -> null;

		doNothing().when(enhancer).addCorsOperations(paths.values(), mapper);

		enhancer.onSwaggerCreationFinished(swagger, mapper);

		verify(enhancer).onSwaggerCreationFinished(swagger, mapper);
		verify(enhancer).addCorsOperations(paths.values(), mapper);

		verifyNoMoreInteractions(enhancer); // to be able to verifyNoMoreInteractions

		verifyZeroInteractions(p1);
		verifyZeroInteractions(p2);
	}

	@Test
	public void getContentType_NullProduces_ShouldReturnJson() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = new Operation();
		operation.setProduces(null);
		when(context.getOperation()).thenReturn(operation);
		assertEquals("application/json", enhancer.getContentType(context));
	}

	@Test
	public void getContentType_NoContentTypeGiven_ShouldReturnJson() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = new Operation();
		operation.setProduces(ImmutableList.of());
		when(context.getOperation()).thenReturn(operation);
		assertEquals("application/json", enhancer.getContentType(context));
	}

	@Test
	public void getContentType_ContentTypeGiven_ShouldReturnSetContentType() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = new Operation();
		operation.setProduces(ImmutableList.of("whatever"));
		when(context.getOperation()).thenReturn(operation);
		assertEquals("whatever", enhancer.getContentType(context));
	}

	@Test(expected = RuntimeException.class)
	public void getContentType_MultipleContentTypesGiven_ShouldReturnSetContentType() {
		OperationContext context = mock(OperationContext.class);
		Operation operation = new Operation();
		operation.setProduces(ImmutableList.of("a", "b"));
		when(context.getOperation()).thenReturn(operation);
		enhancer.getContentType(context);
	}

	@Test
	public void addCorsAllowOriginHeader_CorsDisabledAndSetManuallyViaHeader_ShouldKeepCorsHeader() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Method endpoint = SampleResource.class.getMethod("method");
		when(context.getEndpointMethod()).thenReturn(endpoint);
		Response resp = new Response();
		StringProperty headerProp = mock(StringProperty.class);
		resp.setHeaders(ImmutableMap.of("Access-Control-Allow-Origin", headerProp));
		doReturn(false).when(enhancer).isCorsEnabled(endpoint);
		enhancer.addCorsAllowOriginHeader(resp, context);
		assertTrue(resp.getHeaders().size() == 1);
		assertSame(headerProp, resp.getHeaders().get("Access-Control-Allow-Origin"));
		verifyZeroInteractions(headerProp);
	}

	@Test
	public void addCorsAllowOriginHeader_CorsEnabledAndSetManuallyViaHeader_ShouldKeepCorsHeader() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Method endpoint = SampleResource.class.getMethod("method");
		when(context.getEndpointMethod()).thenReturn(endpoint);
		Response resp = new Response();
		StringProperty headerProp = mock(StringProperty.class);
		resp.setHeaders(ImmutableMap.of("Access-Control-Allow-Origin", headerProp));
		doReturn(true).when(enhancer).isCorsEnabled(endpoint);
		enhancer.addCorsAllowOriginHeader(resp, context);
		assertTrue(resp.getHeaders().size() == 1);
		assertSame(headerProp, resp.getHeaders().get("Access-Control-Allow-Origin"));
		verifyZeroInteractions(headerProp);
	}

	@Test
	public void addCorsAllowOriginHeader_CorsEnabledAndNoHeaderSet_ShouldAddCorsHeader() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Method endpoint = SampleResource.class.getMethod("method");
		when(context.getEndpointMethod()).thenReturn(endpoint);
		Response resp = new Response();
		resp.setHeaders(new HashMap<>());
		doReturn(true).when(enhancer).isCorsEnabled(endpoint);
		doReturn("whatever").when(enhancer).getCorsAllowOrigin(false);
		enhancer.addCorsAllowOriginHeader(resp, context);
		assertTrue(resp.getHeaders().size() == 1);
		assertEquals("'whatever'", resp.getHeaders().get("Access-Control-Allow-Origin").getDescription());
	}

	@Test
	public void addCorsAllowOriginHeader_CorsEnabledAndNullHeader_ShouldAddCorsHeader() throws NoSuchMethodException, SecurityException {
		OperationContext context = mock(OperationContext.class);
		Method endpoint = SampleResource.class.getMethod("method");
		when(context.getEndpointMethod()).thenReturn(endpoint);
		Response resp = new Response();
		resp.setHeaders(null);
		doReturn(true).when(enhancer).isCorsEnabled(endpoint);
		doReturn("whatever").when(enhancer).getCorsAllowOrigin(false);
		enhancer.addCorsAllowOriginHeader(resp, context);
		assertTrue(resp.getHeaders().size() == 1);
		assertEquals("'whatever'", resp.getHeaders().get("Access-Control-Allow-Origin").getDescription());
	}

	@Test
	public void isCorsEnabled_NoCorsGiven_ShouldReturnAnnotationsDefaultValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(false);
		assertTrue(enhancer.isCorsEnabled(getMethod(NoCors.class)));
	}

	@Test
	public void isCorsEnabled_CorsOnClassGiven_ShouldReturnAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(false);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnClass.class)));
	}

	@Test
	public void isCorsEnabled_CorsOnMethodGiven_ShouldReturnAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(false);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnMethod.class)));
	}

	@Test
	public void isCorsEnabled_CorsOnClassAndMethodGiven_ShouldReturnMethodAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(false);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnClassAndMethod.class)));
	}

	@Test
	public void isCorsEnabledParam_NoCorsGiven_ShouldReturnPassedDefaultValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(true);
		when(configuration.isDefaultCorsEnabled()).thenReturn(false);
		assertFalse(enhancer.isCorsEnabled(getMethod(NoCors.class)));
	}

	@Test
	public void isCorsEnabledParam_CorsOnClassGiven_ShouldReturnAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(true);
		when(configuration.isDefaultCorsEnabled()).thenReturn(true);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnClass.class)));
	}

	@Test
	public void isCorsEnabledParam_CorsOnMethodGiven_ShouldReturnAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(true);
		when(configuration.isDefaultCorsEnabled()).thenReturn(true);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnMethod.class)));
	}

	@Test
	public void isCorsEnabledParam_CorsOnClassAndMethodGiven_ShouldReturnMethodAnnotationValue() {
		when(configuration.isSetDefaultCorsEnabled()).thenReturn(true);
		when(configuration.isDefaultCorsEnabled()).thenReturn(true);
		assertFalse(enhancer.isCorsEnabled(getMethod(CorsOnClassAndMethod.class)));
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointSecuredAndIamSecurity_ShouldSetAuthExtension() {
		Operation op = mock(Operation.class);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.IAM);
		enhancer.setSecurityExtension(context);
		verify(op).setVendorExtension("x-amazon-apigateway-auth", new ApiGatewayAuth());
		verify(op).getSecurity();
		verify(op, times(0)).setSecurity(any());
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointSecuredAndAuthorizerSecurityButNoDefaultSecurity_ShouldComplainMissingDefaultSecurity() {
		Operation op = mock(Operation.class);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.AUTHORIZER);
		enhancer.setSecurityExtension(context);
		verify(log).warn(any());
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointSecuredAndAuthorizerSecurityAndDefaultSecurity_ShouldSetSecurity() {
		Operation op = mock(Operation.class);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		List<Map<String, List<String>>> defaultSecurity = ImmutableList.of(ImmutableMap.of("a", ImmutableList.of("b")));
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.AUTHORIZER);
		when(configuration.isSetDefaultSecurity()).thenReturn(true);
		when(configuration.getDefaultSecurity()).thenReturn(defaultSecurity);
		enhancer.setSecurityExtension(context);
		verify(op).setSecurity(defaultSecurity);
		verify(op).getSecurity();
		verify(op, times(0)).setVendorExtension(any(), any());
	}

	@Test
	public void setSecurityExtension_SecuritySet_ShouldNotSetSecurity() {
		Operation op = mock(Operation.class);
		List<Map<String, List<String>>> opSecurity = ImmutableList.of(ImmutableMap.of("a", ImmutableList.of("b")));
		when(op.getSecurity()).thenReturn(opSecurity);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		List<Map<String, List<String>>> defaultSecurity = ImmutableList.of(ImmutableMap.of("c", ImmutableList.of("d")));
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.AUTHORIZER);
		when(configuration.isSetDefaultSecurity()).thenReturn(true);
		when(configuration.getDefaultSecurity()).thenReturn(defaultSecurity);
		enhancer.setSecurityExtension(context);
		verify(op).getSecurity();
		verify(op, times(0)).setSecurity(any());
		verify(op, times(0)).setVendorExtension(any(), any());
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointSecuredAndNoneSecurity_ShouldNotSetAuthExtension() {
		Operation op = mock(Operation.class);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.NONE);
		enhancer.setSecurityExtension(context);
		verify(op).getSecurity();
		verify(op, times(0)).setSecurity(any());
		verify(op, times(0)).setVendorExtension(any(), any());
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointSecuredAndNoDefaultSecurity_ShouldNotSetAuthExtension() {
		Operation op = mock(Operation.class);
		OperationContext context = new OperationContext(op, getMethod(Secured.class), new Swagger());
		when(configuration.isSetDefaultAuthType()).thenReturn(false);
		enhancer.setSecurityExtension(context);
		verify(op).getSecurity();
		verify(op, times(0)).setSecurity(any());
		verify(op, times(0)).setVendorExtension(any(), any());
	}

	@Test
	public void setSecurityExtension_NoSecSetYetAndEndpointNotSecuredAndIamSecurity_ShouldNotSetAuthExtension() {
		Operation op = mock(Operation.class);
		when(op.getSecurity()).thenReturn(null);
		OperationContext context = new OperationContext(op, getMethod(Secured.class, "notSecure"), new Swagger());
		when(configuration.isSetDefaultAuthType()).thenReturn(true);
		when(configuration.getDefaultAuthType()).thenReturn(AuthType.IAM);
		enhancer.setSecurityExtension(context);
		verify(op).getSecurity();
		verify(op, times(0)).setSecurity(any());
		verify(op, times(0)).setVendorExtension(any(), any());
	}

	@Test
	public void addCorsOperations_OneCorsEnabledOperationAndDefaultConfigGiven_ShouldAddDefaultCorsOperation() {
		Collection<Path> paths = new LinkedList<>();
		Path p = new Path();
		paths.add(p);
		Operation op = new Operation();
		p.setGet(op);
		Function<Operation, Method> operationMethodMapper = (o) -> {
			if (o == op) {
				return getMethod(NoCors.class);
			} else {
				return null;
			}
		};
		enhancer.addCorsOperations(paths, operationMethodMapper);
		Operation corsOp = p.getOptions();
		assertCorsOperation(corsOp, "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'", "'*'", "'GET'");
	}

	@Test
	public void addCorsOperations_MultipleCorsEnabledOperationAndDefaultConfigGiven_ShouldAddDefaultCorsOperation() {
		Collection<Path> paths = new LinkedList<>();
		Path p = new Path();
		paths.add(p);
		Operation getOp = new Operation();
		Operation postOp = new Operation();
		p.setGet(getOp);
		p.setPost(postOp);
		Function<Operation, Method> operationMethodMapper = (o) -> {
			if (o == getOp || o == postOp) {
				return getMethod(NoCors.class);
			}  else {
				return null;
			}
		};
		enhancer.addCorsOperations(paths, operationMethodMapper);
		Operation corsOp = p.getOptions();
		assertCorsOperation(corsOp, "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'", "'*'", "'POST,GET'");
	}

	@Test
	public void addCorsOperations_OneCorsEnabledOperationAndExplicitConfigGiven_ShouldAddDefaultCorsOperation() {
		when(configuration.isSetDefaultAccessControlAllowHeaders()).thenReturn(true);
		when(configuration.isSetDefaultAccessControlAllowOrigin()).thenReturn(true);
		when(configuration.getDefaultAccessControlAllowHeaders()).thenReturn("Some-Allow-Header");
		when(configuration.getDefaultAccessControlAllowOrigin()).thenReturn("some.allow.origin");
		Collection<Path> paths = new LinkedList<>();
		Path p = new Path();
		paths.add(p);
		Operation getOp = new Operation();
		p.setGet(getOp);
		Function<Operation, Method> operationMethodMapper = (o) -> {
			if (o == getOp) {
				return getMethod(NoCors.class);
			}  else {
				return null;
			}
		};
		enhancer.addCorsOperations(paths, operationMethodMapper);
		Operation corsOp = p.getOptions();
		assertCorsOperation(corsOp, "'Some-Allow-Header'", "'some.allow.origin'", "'GET'");
	}

	@Test
	public void addCorsOperations_OptionsOperationGiven_ShouldNotChangeOptionsOperation() {
		Collection<Path> paths = new LinkedList<>();
		Path p = new Path();
		paths.add(p);
		Operation getOp = new Operation();
		Operation postOp = new Operation();
		Operation corsOp = mock(Operation.class);
		p.setGet(getOp);
		p.setPost(postOp);
		p.setOptions(corsOp);
		Function<Operation, Method> operationMethodMapper = (o) -> {
			if (o == getOp || o == postOp || o == corsOp) {
				return getMethod(NoCors.class);
			}  else {
				return null;
			}
		};
		enhancer.addCorsOperations(paths, operationMethodMapper);
		verifyZeroInteractions(corsOp);
		assertSame(corsOp, p.getOptions());
	}

	@Test
	public void addCorsOperations_CorsDisabled_ShouldSetCorsOperation() {
		Collection<Path> paths = new LinkedList<>();
		Path p = new Path();
		paths.add(p);
		Operation getOp = new Operation();
		p.setGet(getOp);
		Function<Operation, Method> operationMethodMapper = (o) -> {
			if (o == getOp) {
				return getMethod(CorsOnMethod.class);
			}  else {
				return null;
			}
		};
		enhancer.addCorsOperations(paths, operationMethodMapper);
		assertNull(p.getOptions());
	}

	private void assertCorsOperation(Operation corsOp, String headers, String origin, String methods) {
		assertNotNull(corsOp);
		Map<String, Property> corsHeaders = corsOp.getResponses().get("200").getHeaders();
		assertNotNull(corsHeaders.get("Access-Control-Allow-Headers"));
		assertNotNull(corsHeaders.get("Access-Control-Allow-Methods"));
		assertNotNull(corsHeaders.get("Access-Control-Allow-Origin"));
		@SuppressWarnings("unchecked")
		Map<String, Object> responseParams = (Map<String, Object>) get("responseParameters",
				get("default", get("responses", get("x-amazon-apigateway-integration", corsOp.getVendorExtensions()))));
		assertEquals(headers, responseParams.get("method.response.header.Access-Control-Allow-Headers"));
		assertEquals(origin, responseParams.get("method.response.header.Access-Control-Allow-Origin"));
		assertEquals(methods, responseParams.get("method.response.header.Access-Control-Allow-Methods"));
	}

	@SuppressWarnings("unchecked")
	private static Object get(String key, Object o) {
		return ((Map<String, Object>) o).get(key);
	}

	private static Method getMethod(Class<?> clazz, String methodName) {
		try {
			return clazz.getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Method getMethod(Class<?> clazz) {
		return getMethod(clazz, "method");
	}


	static class SampleResource {
		public void method() {
		}

		@ApiOperation(value = "", code = 3)
		@ApiResponses({
			@ApiResponse(message = "", code = 4),
			@ApiResponse(message = "", code = 5)
		})
		public void method1() {
		}

		@ApiOperation(value = "", code = 3)
		public void method2() {
		}
	}

	static class NoCors {
		public void method() {
		}
	}

	@Cors(enabled = false)
	static class CorsOnClass {
		public void method() {
		}
	}

	static class CorsOnMethod {
		@Cors(enabled = false)
		public void method() {
		}
	}

	@Cors(enabled = true)
	static class CorsOnClassAndMethod {
		@Cors(enabled = false)
		public void method() {
		}
	}

	static class Secured {
		@RolesAllowed("")
		public void method() {
		}
		public void notSecure() {
		}
	}
}
