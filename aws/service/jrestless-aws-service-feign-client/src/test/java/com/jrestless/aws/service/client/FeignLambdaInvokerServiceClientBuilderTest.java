package com.jrestless.aws.service.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.jrestless.aws.service.client.FeignLambdaServiceInvokerClient;

public class FeignLambdaInvokerServiceClientBuilderTest {

	private static final String FUNCTION_NAME = "someFunctionName";

	private FeignLambdaServiceInvokerClient.Builder builder;
	private AWSLambdaClient lambdaClient = mock(AWSLambdaClient.class);

	@Before
	public void setup() {
		builder = spy(new FeignLambdaServiceInvokerClient.Builder());
		doReturn(null).when(builder).create(any(), any(), any(), any());
	}

	@Test
	public void setFunctionName_FunctionNameGiven_ShouldUseFunctionName() {
		builder.setFunctionName("myFunctionName");
		builder.setAwsLambdaClient(lambdaClient);
		builder.build();
		verify(builder).create(lambdaClient, "myFunctionName", null, null);
	}

	@Test(expected = IllegalStateException.class)
	public void setFunctionName_NoFunctionNameGiven_ShouldNotBuild() {
		builder.setAwsLambdaClient(lambdaClient);
		builder.build();
	}

	@Test(expected = IllegalStateException.class)
	public void setFunctionName_NullFunctionNameGiven_ShouldNotBuild() {
		builder.setFunctionName(null);
		builder.setAwsLambdaClient(lambdaClient);
		builder.build();
	}

	@Test
	public void setFunctionName_FunctionAliasGiven_ShouldUseAlias() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setAwsLambdaClient(lambdaClient);
		builder.setFunctionAlias("myAlias");
		builder.build();
		verify(builder).create(lambdaClient, FUNCTION_NAME, "myAlias", null);
	}

	@Test
	public void setFunctionName_NullFunctionAliasGiven_ShouldUseNullAsAlias() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setAwsLambdaClient(lambdaClient);
		builder.setFunctionAlias(null);
		builder.build();
		verify(builder).create(lambdaClient, FUNCTION_NAME, null, null);
	}

	@Test
	public void setFunctionName_FunctionVersionGiven_ShouldUseFunctionVersion() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setAwsLambdaClient(lambdaClient);
		builder.setFunctionVersion("myFunctionVersion");
		builder.build();
		verify(builder).create(lambdaClient, FUNCTION_NAME, null, "myFunctionVersion");
	}

	@Test
	public void setFunctionName_NullFunctionVersionGiven_ShouldUseNullAsFunctionVersion() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setAwsLambdaClient(lambdaClient);
		builder.setFunctionVersion(null);
		builder.build();
		verify(builder).create(lambdaClient, FUNCTION_NAME, null, null);
	}

	@Test
	public void setFunctionName_LambdaClientGiven_ShouldUseLamdaClient() {
		AWSLambdaClient myLambdaClient = mock(AWSLambdaClient.class);
		builder.setFunctionName(FUNCTION_NAME);
		builder.setAwsLambdaClient(myLambdaClient);
		builder.build();
		verify(builder).create(myLambdaClient, FUNCTION_NAME, null, null);
	}

	@Test
	public void setFunctionName_RegionGiven_ShouldCreateLambdaClientUsingRegion() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setRegion(Regions.AP_NORTHEAST_1);
		builder.build();
		verify(builder).create(isNotNull(), eq(FUNCTION_NAME), isNull(), isNull());
	}

	@Test(expected = IllegalStateException.class)
	public void setFunctionName_NullRegionGiven_ShouldNotBuild() {
		builder.setFunctionName(FUNCTION_NAME);
		builder.setRegion(null);
		builder.build();
	}

	@Test
	public void setFunctionName_LambdaClientAndRegionGiven_ShouldUseLambdaClient() {
		AWSLambdaClient myLambdaClient = mock(AWSLambdaClient.class);
		builder.setFunctionName(FUNCTION_NAME);
		builder.setRegion(Regions.AP_NORTHEAST_1);
		builder.setAwsLambdaClient(myLambdaClient);
		builder.build();
		verify(builder).create(myLambdaClient, FUNCTION_NAME, null, null);
	}
}
