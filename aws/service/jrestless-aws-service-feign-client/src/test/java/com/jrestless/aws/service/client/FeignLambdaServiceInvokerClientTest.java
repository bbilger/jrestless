package com.jrestless.aws.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.invoke.LambdaFunctionNameResolver;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.jrestless.aws.service.io.DefaultServiceResponse;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceResponse;

public class FeignLambdaServiceInvokerClientTest {

	private static final String FUNCTION_NAME = "someFunctionName";

	private AWSLambda lambdaClient = mock(AWSLambda.class);

	private LambdaInvokerFactory.Builder lambdaInvokerFactoryBuilder;
	private LambdaInvokerService service = mock(LambdaInvokerService.class);

	@BeforeEach
	public void setup() {
		lambdaInvokerFactoryBuilder = mock(LambdaInvokerFactory.Builder.class);
		when(lambdaInvokerFactoryBuilder.lambdaFunctionNameResolver(any())).thenReturn(lambdaInvokerFactoryBuilder);
		when(lambdaInvokerFactoryBuilder.functionAlias(any())).thenReturn(lambdaInvokerFactoryBuilder);
		when(lambdaInvokerFactoryBuilder.functionVersion(any())).thenReturn(lambdaInvokerFactoryBuilder);
		when(lambdaInvokerFactoryBuilder.lambdaClient(any())).thenReturn(lambdaInvokerFactoryBuilder);
		when(lambdaInvokerFactoryBuilder.build(LambdaInvokerService.class)).thenReturn(service);
	}

	@Test
	public void init_NullLambdaClientGiven_ShouldFail() {
		assertThrows(NullPointerException.class, () -> new FeignLambdaServiceInvokerClient(null, FUNCTION_NAME, null, null));
	}

	@Test
	public void init_NullFunctionNameGiven_ShouldFail() {
		assertThrows(NullPointerException.class, () -> new FeignLambdaServiceInvokerClient(lambdaClient, null, null, null));
	}

	@Test
	public void init_LambdaClientGiven_ShouldUseOnInvocationBuilder() {
		AWSLambda myLambdaClient = mock(AWSLambda.class);
		FeignLambdaServiceInvokerClient invokerClient = init(myLambdaClient, FUNCTION_NAME, null, null);
		verify(lambdaInvokerFactoryBuilder).lambdaClient(myLambdaClient);
		verify(lambdaInvokerFactoryBuilder).lambdaFunctionNameResolver(eqFn(FUNCTION_NAME));
		verify(lambdaInvokerFactoryBuilder).functionAlias(isNull());
		verify(lambdaInvokerFactoryBuilder).functionVersion(isNull());
		verify(lambdaInvokerFactoryBuilder).build(LambdaInvokerService.class);
		assertEquals(service, invokerClient.getInvokerService());
	}

	@Test
	public void init_FunctionNameGiven_ShouldUseOnInvocationBuilder() {
		FeignLambdaServiceInvokerClient invokerClient = init(lambdaClient, "myFunctionName", null, null);
		verify(lambdaInvokerFactoryBuilder).lambdaClient(lambdaClient);
		verify(lambdaInvokerFactoryBuilder).lambdaFunctionNameResolver(eqFn("myFunctionName"));
		verify(lambdaInvokerFactoryBuilder).functionAlias(isNull());
		verify(lambdaInvokerFactoryBuilder).functionVersion(isNull());
		verify(lambdaInvokerFactoryBuilder).build(LambdaInvokerService.class);
		assertEquals(service, invokerClient.getInvokerService());
	}

	@Test
	public void init_FunctionAliasGiven_ShouldUseOnInvocationBuilder() {
		FeignLambdaServiceInvokerClient invokerClient = init(lambdaClient, FUNCTION_NAME, "myFunctionAlias", null);
		verify(lambdaInvokerFactoryBuilder).lambdaClient(lambdaClient);
		verify(lambdaInvokerFactoryBuilder).lambdaFunctionNameResolver(eqFn(FUNCTION_NAME));
		verify(lambdaInvokerFactoryBuilder).functionAlias("myFunctionAlias");
		verify(lambdaInvokerFactoryBuilder).functionVersion(isNull());
		verify(lambdaInvokerFactoryBuilder).build(LambdaInvokerService.class);
		assertEquals(service, invokerClient.getInvokerService());
	}

	@Test
	public void init_FunctionVersionGiven_ShouldUseOnInvocationBuilder() {
		FeignLambdaServiceInvokerClient invokerClient = init(lambdaClient, FUNCTION_NAME, null, "myFunctionVersion");
		verify(lambdaInvokerFactoryBuilder).lambdaClient(lambdaClient);
		verify(lambdaInvokerFactoryBuilder).lambdaFunctionNameResolver(eqFn(FUNCTION_NAME));
		verify(lambdaInvokerFactoryBuilder).functionAlias(isNull());
		verify(lambdaInvokerFactoryBuilder).functionVersion("myFunctionVersion");
		verify(lambdaInvokerFactoryBuilder).build(LambdaInvokerService.class);
		assertEquals(service, invokerClient.getInvokerService());
	}

	@Test
	public void execute_RequestGiven_ShouldInvokeServiceReturnResponseAndNothingElse() {
		FeignLambdaServiceInvokerClient invokerClient = init(lambdaClient, FUNCTION_NAME, null, null);
		ServiceRequest request = mock(ServiceRequest.class);
		DefaultServiceResponse expectedResponse = mock(DefaultServiceResponse.class);
		feign.Request.Options requestOptions = mock(feign.Request.Options.class);
		when(service.execute(request)).thenReturn(expectedResponse);
		ServiceResponse actualResponse = invokerClient.execute(request, requestOptions);
		verifyZeroInteractions(expectedResponse);
		verifyZeroInteractions(requestOptions);
		verifyZeroInteractions(request);
		assertEquals(expectedResponse, actualResponse);
	}

	@Test
	public void testInitCreatesService() {
		assertNotNull(new FeignLambdaServiceInvokerClient(lambdaClient, FUNCTION_NAME, null, null).getInvokerService());
	}

	@Test
	public void testInitViaBuilderCreatesService() {
		assertNotNull(FeignLambdaServiceInvokerClient.builder()
				.setAwsLambdaClient(lambdaClient)
				.setFunctionName(FUNCTION_NAME)
				.build()
			.getInvokerService());
	}

	FeignLambdaServiceInvokerClient init(AWSLambda awsLambdaClient, String functionName, String functionAlias,
			String functionVersion) {
		FeignLambdaServiceInvokerClient lambdaClient = new FeignLambdaServiceInvokerClient(lambdaInvokerFactoryBuilder,
				awsLambdaClient, functionName, functionAlias, functionVersion);
		return lambdaClient;
	}

	private static LambdaFunctionNameResolver eqFn(String functionName) {
		return argThat(new ArgumentMatcher<LambdaFunctionNameResolver>() {
			@Override
			public boolean matches(LambdaFunctionNameResolver argument) {
				return functionName.equals(argument.getFunctionName(null, null, null));
			}
		});
	}
}
