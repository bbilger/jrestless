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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.swagger.ApiGatewayIntegrationExtensionFactoryImpl.AwsOperationContext;
import com.jrestless.aws.swagger.ApiGatewayIntegrationExtensionFactoryImpl.ResponseContext;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationExtension;
import com.jrestless.aws.swagger.models.ApiGatewayIntegrationResponse;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class ApiGatewayIntegrationExtensionFactoryImplTest {

	private ApiGatewayIntegrationExtensionFactoryImpl factory;

	@Before
	public void setup() {
		LogAdapter log = mock(LogAdapter.class);
		factory = spy(new ApiGatewayIntegrationExtensionFactoryImpl(log, "awsCredentials", "awsLambdaUri"));
	}

	@Test(expected = NullPointerException.class)
	public void instantiate_NullLog_ShouldThrowNpe() {
		new ApiGatewayIntegrationExtensionFactoryImpl(null, "s.th.", "s.th.");
	}

	@Test(expected = NullPointerException.class)
	public void instantiate_NullCredentials_ShouldThrowNpe() {
		LogAdapter log = mock(LogAdapter.class);
		new ApiGatewayIntegrationExtensionFactoryImpl(log, null, "s.th.");
	}

	@Test(expected = NullPointerException.class)
	public void instantiate_NullUri_ShouldThrowNpe() {
		LogAdapter log = mock(LogAdapter.class);
		new ApiGatewayIntegrationExtensionFactoryImpl(log, "s.th.", null);
	}

	@Test
	public void getDefaultStatusCode_NoDefaultCodeGiven_ShouldReturn200() throws NoSuchMethodException, SecurityException {
		AwsOperationContext context = mock(AwsOperationContext.class);
		Method method = StatusCodeTestResource1.class.getMethod("getSomeResource1");
		when(context.getEndpointMethod()).thenReturn(method);
		assertEquals(200, factory.getDefaultStatusCode(context));
	}

	@Test
	public void getDefaultStatusCode_EndpointCodeGiven_ShouldReturnEndpointCode() throws NoSuchMethodException, SecurityException {
		AwsOperationContext context = mock(AwsOperationContext.class);
		Method method = StatusCodeTestResource1.class.getMethod("getSomeResource2");
		when(context.getEndpointMethod()).thenReturn(method);
		assertEquals(400, factory.getDefaultStatusCode(context));
	}

	@Test
	public void getResponseTemplate_DefaultResponseGiven_ShouldReturnDefaultResponseTemplate() {
		ResponseContext context = mock(ResponseContext.class);
		doReturn(true).when(factory).isDefaultResponse(context);
		String responseTemplate = factory.getResponseTemplate(context);
		assertEquals(ApiGatewayIntegrationExtensionFactoryImpl.INTEGRATION_DEFAULT_RESPONSE_TEMPLATE, responseTemplate);
	}

	@Test
	public void getResponseTemplate_NonDefaultResponseGiven_ShouldReturnErrorResponseTemplate() {
		ResponseContext context = mock(ResponseContext.class);
		doReturn(false).when(factory).isDefaultResponse(context);
		String responseTemplate = factory.getResponseTemplate(context);
		assertEquals(ApiGatewayIntegrationExtensionFactoryImpl.INTEGRATION_ERROR_RESPONSE_TEMPLATE, responseTemplate);
	}

	@Test
	public void getIntegrationStatusCodePattern_DefaultResponseGiven_ShouldReturnDefault() {
		ResponseContext context = mock(ResponseContext.class);
		doReturn(true).when(factory).isDefaultResponse(context);
		String statusCodePattern = factory.getIntegrationStatusCodePattern(context);
		assertEquals("default", statusCodePattern);
	}

	@Test
	public void getIntegrationStatusCodePattern_NonDefaultResponseGiven_ShouldPatternToMatchStatusCode() {
		ResponseContext context = mock(ResponseContext.class);
		doReturn(false).when(factory).isDefaultResponse(context);
		when(context.getStatusCode()).thenReturn("500");
		String statusCodePattern = factory.getIntegrationStatusCodePattern(context);
		assertEquals("(.|\\n)*\\\"statusCode\\\"\\:\\s*\\n*\\s*\\\"500\\\"(.|\\n)*", statusCodePattern);
	}

	@Test
	public void isDefaultResponse_MatchingNonExplicitDefaultResponseGiven_ShouldAssumeTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "200");
		when(aContext.getEndpointMethod()).thenReturn(StatusCodeTestResource1.class.getMethod("getSomeResource1"));
		assertTrue(factory.isDefaultResponse(rContext));
	}

	@Test
	public void isDefaultResponse_NotMatchingNonExplicitDefaultResponseGiven_ShouldAssumeTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "300");
		when(aContext.getEndpointMethod()).thenReturn(StatusCodeTestResource1.class.getMethod("getSomeResource1"));
		assertFalse(factory.isDefaultResponse(rContext));
	}

	@Test
	public void isDefaultResponse_MatchingExplicitDefaultResponseGiven_ShouldAssumeTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "400");
		when(aContext.getEndpointMethod()).thenReturn(StatusCodeTestResource1.class.getMethod("getSomeResource2"));
		assertTrue(factory.isDefaultResponse(rContext));
	}

	@Test
	public void isDefaultResponse_NotMatchingExplicitDefaultResponseGiven_ShouldAssumeTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "300");
		when(aContext.getEndpointMethod()).thenReturn(StatusCodeTestResource1.class.getMethod("getSomeResource2"));
		assertFalse(factory.isDefaultResponse(rContext));
	}

	@Test
	public void isDefaultResponse_StatusCodeDefaultGiven_ShouldReturnTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "default");
		assertTrue(factory.isDefaultResponse(rContext));
	}

	@Test
	public void isDefaultResponse_StatusCodeNotDefaultGiven_ShouldReturnTrue() throws NoSuchMethodException, SecurityException {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		ResponseContext rContext = new ResponseContext(aContext, mock(io.swagger.models.Response.class), "123");
		when(aContext.getEndpointMethod()).thenReturn(StatusCodeTestResource1.class.getMethod("getSomeResource2"));
		assertFalse(factory.isDefaultResponse(rContext));
	}

	@Test
	public void createIntegrationResponseParameters_NullHeadersGiven_ShouldReturnEmptyParametersMap() {
		ResponseContext rc = mock(ResponseContext.class);
		Response r = new Response();
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(0, rParams.size());
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithMissingName_ShouldSkipAndReturnEmptyParametersMap() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		headers.put("", new StringProperty());
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(0, rParams.size());
		verify(factory).warnSkipHeader(any(), any());
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithNonStaticAndNonDynValue_ShouldSkipAndReturnEmptyParametersMap() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("test");
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(0, rParams.size());
		verify(factory).warnSkipHeader(any(), any());
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithDynValueOnNonDefaultResponse_ShouldAddHeaderMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("201");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("integration.response.body.test");
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("integration.response.body.test", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithDynValueOnDefaultResponse_ShouldAddHeaderMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("default");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("integration.response.body.test");
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("integration.response.body.test", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithStaticValueOnDefaultResponse_ShouldAddHeaderMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("200");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("'test'");
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("'test'", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithStaticValueOnNonDefaultResponse_ShouldAddHeaderMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("201");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headerProp.setDescription("'test'");
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("'test'", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponseParameters_HeaderWithoutValueOnDefaultResponse_ShouldAddHeaderNameMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("default");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("integration.response.body.headers.Some-Header", rParams.get("method.response.header.Some-Header"));
	}

	@Test(expected = RuntimeException.class)
	public void createIntegrationResponseParameters_NonSupportedNonDefaultHeaderGiven_ShouldFail() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getAwsOperationContext()).thenReturn(createMockAwsOperationContext());
		when(rc.getStatusCode()).thenReturn("201");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		factory.createIntegrationResponseParameters(rc);
	}

	@Test
	public void createIntegrationResponseParameters_SupportedNonDefaultHeaderAtFirstPositionGiven_ShouldAddHeaderNameMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		AwsOperationContext context = createMockAwsOperationContext();
		when(context.getConfiguration().getSupportedNonDefaultHeadersInOrder()).thenReturn(new String[] { "Some-Header" });
		when(context.getConfiguration().isSetSupportedNonDefaultHeadersInOrder()).thenReturn(true);
		when(rc.getAwsOperationContext()).thenReturn(context);
		when(rc.getStatusCode()).thenReturn("201");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		factory.createIntegrationResponseParameters(rc);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("integration.response.body.cause.errorMessage", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponseParameters_SupportedNonDefaultHeaderAtSecondPositionGiven_ShouldAddHeaderNameMapping() {
		ResponseContext rc = mock(ResponseContext.class);
		AwsOperationContext context = createMockAwsOperationContext();
		when(context.getConfiguration().getSupportedNonDefaultHeadersInOrder()).thenReturn(new String[] { "Some-Other-Header", "Some-Header" });
		when(context.getConfiguration().isSetSupportedNonDefaultHeadersInOrder()).thenReturn(true);
		when(rc.getAwsOperationContext()).thenReturn(context);
		when(rc.getStatusCode()).thenReturn("201");
		Response r = new Response();
		Map<String, Property> headers = new HashMap<>();
		StringProperty headerProp = new StringProperty();
		headers.put("Some-Header", headerProp);
		r.setHeaders(headers);
		when(rc.getResponse()).thenReturn(r);
		factory.createIntegrationResponseParameters(rc);
		Map<String, String> rParams = factory.createIntegrationResponseParameters(rc);
		assertEquals(1, rParams.size());
		assertEquals("integration.response.body.cause.cause.errorMessage", rParams.get("method.response.header.Some-Header"));
	}

	@Test
	public void createIntegrationResponses_ResponsesGiven_ShouldCreateIntegrationResponses() {
		AwsOperationContext aContext = mock(AwsOperationContext.class);
		Operation op = new Operation();
		Map<String, Response> resps = new HashMap<>();
		op.setResponses(resps);
		Response r1 = new Response();
		Response r2 = new Response();
		resps.put("200", r1);
		resps.put("400", r2);
		when(aContext.getOperation()).thenReturn(op);

		ApiGatewayIntegrationResponse iR1 = mock(ApiGatewayIntegrationResponse.class);
		ApiGatewayIntegrationResponse iR2 = mock(ApiGatewayIntegrationResponse.class);
		doReturn(new SimpleEntry<>("c1", iR1)).when(factory).createIntegrationResponse(sameResponseContextByResponse(r1));
		doReturn(new SimpleEntry<>("c2", iR2)).when(factory).createIntegrationResponse(sameResponseContextByResponse(r2));

		Map<String, ApiGatewayIntegrationResponse> intResponses = factory.createIntegrationResponses(aContext);

		assertTrue(intResponses.size() == 2);
		assertEquals(iR1, intResponses.get("c1"));
		assertEquals(iR2, intResponses.get("c2"));
		verifyZeroInteractions(iR1);
		verifyZeroInteractions(iR2);
	}

	@Test
	public void createIntegrationResponse_ResponseGiven_ShouldCreateIntegrationResponse() {
		ResponseContext rc = mock(ResponseContext.class);
		when(rc.getStatusCode()).thenReturn("123");

		Map<String, String> integrationResponseParameters = ImmutableMap.of("testKey", "testValue");
		doReturn(integrationResponseParameters).when(factory).createIntegrationResponseParameters(rc);
		doReturn("integrationStatusCodePattern").when(factory).getIntegrationStatusCodePattern(rc);
		doReturn("responseTemplate").when(factory).getResponseTemplate(rc);
		doReturn(123).when(factory).getDefaultStatusCode(any());

		Entry<String, ApiGatewayIntegrationResponse> intRespEntry = factory.createIntegrationResponse(rc);
		assertEquals("integrationStatusCodePattern", intRespEntry.getKey());
		ApiGatewayIntegrationResponse intResp = intRespEntry.getValue();
		assertEquals("123", intResp.getStatusCode());
		assertEquals(integrationResponseParameters, intResp.getResponseParameters());
		assertEquals(ImmutableMap.of(MediaType.APPLICATION_JSON, "responseTemplate"), intResp.getResponseTemplates());
	}

	@Test
	public void testCreateRequestTemplates() throws JsonParseException, JsonMappingException, IOException {
		Map<String, String> tmpls = factory.createRequestTemplates(null);
		String tmpl = tmpls.get("application/json");
		assertNotNull(tmpl);
	}

	@Test
	public void createApiGatewayExtension_ContextAndConfigurationGiven_ShouldCreateExtension() throws NoSuchMethodException, SecurityException {
		Operation operation = mock(Operation.class);
		Swagger swagger = mock(Swagger.class);
		Method endpointMethod = StatusCodeTestResource1.class.getMethod("getSomeResource1");
		OperationContext context = new OperationContext(operation, endpointMethod, swagger);
		AwsSwaggerConfiguration config = mock(AwsSwaggerConfiguration.class);

		Map<String, String> requestTmpls = ImmutableMap.of("k1", "v1", "k2", "v2");
		ApiGatewayIntegrationResponse iR1 = mock(ApiGatewayIntegrationResponse.class);
		ApiGatewayIntegrationResponse iR2 = mock(ApiGatewayIntegrationResponse.class);
		Map<String, ApiGatewayIntegrationResponse> intResponse = ImmutableMap.of("k1", iR1, "k2", iR2);

		doReturn(requestTmpls).when(factory).createRequestTemplates(any());
		doReturn(intResponse).when(factory).createIntegrationResponses(any());

		ApiGatewayIntegrationExtension actualExt = factory.createApiGatewayExtension(context, config);

		ApiGatewayIntegrationExtension expectedExt = new ApiGatewayIntegrationExtension("awsCredentials", "awsLambdaUri", requestTmpls, ImmutableMap.of(), intResponse);

		assertEquals(expectedExt, actualExt);
		verifyZeroInteractions(iR1);
		verifyZeroInteractions(iR2);
	}

	private static AwsOperationContext createMockAwsOperationContext() {
		Method endpoint;
		try {
			endpoint = StatusCodeTestResource1.class.getMethod("getSomeResource1");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		AwsOperationContext aContext = new AwsOperationContext(mock(Operation.class), endpoint, mock(Swagger.class), mock(AwsSwaggerConfiguration.class));
		return aContext;
	}

	private static class StatusCodeTestResource1 {
		@SuppressWarnings("unused")
		public Response getSomeResource1() {
			return null;
		}
		@ApiOperation(value = "", code = 400)
		public Response getSomeResource2() {
			return null;
		}
	}

	private ResponseContext sameResponseContextByResponse(Response response) {
		return Mockito.argThat(new ArgumentMatcher<ResponseContext>() {
			@Override
			public boolean matches(Object actual) {
				return (actual instanceof ResponseContext) && ((ResponseContext) actual).getResponse() == response;
			}
		});
	}
}
