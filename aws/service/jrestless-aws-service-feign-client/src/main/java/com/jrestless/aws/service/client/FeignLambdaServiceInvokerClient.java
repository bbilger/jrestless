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
package com.jrestless.aws.service.client;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.invoke.LambdaInvokerFactory;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceResponse;

/**
 * Feign client that redirects "http" requests to lambda functions implementing
 * {@code com.amazonaws.services.lambda.runtime.RequestHandler<ServiceRequest, ServiceResponse>}
 * using {@link LambdaInvokerFactory}.
 *
 * @author Bjoern Bilger
 *
 */
public class FeignLambdaServiceInvokerClient extends AbstractFeignLambdaServiceClient {

	private final LambdaInvokerService service;

	public FeignLambdaServiceInvokerClient(@Nonnull AWSLambdaClient awsLambdaClient, @Nonnull String functionName,
			@Nullable String functionAlias, @Nullable String functionVersion) {
		this(LambdaInvokerFactory.builder(), awsLambdaClient, functionName, functionAlias, functionVersion);
	}

	FeignLambdaServiceInvokerClient(LambdaInvokerFactory.Builder builder, AWSLambdaClient awsLambdaClient,
			String functionName, String functionAlias, String functionVersion) {
		requireNonNull(awsLambdaClient);
		requireNonNull(functionName);
		service = builder
				.lambdaFunctionNameResolver((method, annotation, config) -> functionName)
				.functionAlias(functionAlias)
				.functionVersion(functionVersion)
				.lambdaClient(awsLambdaClient)
				.build(LambdaInvokerService.class);
	}

	LambdaInvokerService getInvokerService() {
		return service;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	protected ServiceResponse execute(ServiceRequest serviceRequest, feign.Request.Options feignOptions) {
		return service.execute(serviceRequest);
	}

	public static class Builder {
		private String functionName;
		private String functionAlias;
		private String functionVersion;
		private AWSLambdaClient awsLambdaClient;
		private Regions region;

		public Builder setFunctionName(String functionName) {
			this.functionName = functionName;
			return this;
		}

		public Builder setFunctionAlias(String functionAlias) {
			this.functionAlias = functionAlias;
			return this;
		}

		public Builder setFunctionVersion(String functionVersion) {
			this.functionVersion = functionVersion;
			return this;
		}

		public Builder setAwsLambdaClient(AWSLambdaClient awsLambdaClient) {
			this.awsLambdaClient = awsLambdaClient;
			return this;
		}

		public Builder setRegion(Regions region) {
			this.region = region;
			return this;
		}

		protected AWSLambdaClient resolveAwsLambdaClient() {
			AWSLambdaClient resolvedClient = awsLambdaClient;
			if (resolvedClient == null && region != null) {
				resolvedClient = new AWSLambdaClient();
				resolvedClient.configureRegion(region);
			}
			return requireToBuild(resolvedClient, "an awsLambdaClient or a region is required");
		}

		private String resolveFunctionName() {
			return requireToBuild(functionName, "a functionName is required");
		}

		private static <T> T requireToBuild(T o, String msg) {
			if (o == null) {
				throw new IllegalStateException(msg);
			}
			return o;
		}

		// for JUnit
		FeignLambdaServiceInvokerClient create(AWSLambdaClient awsLambdaClient, String functionName,
				String functionAlias, String functionVersion) {
			return new FeignLambdaServiceInvokerClient(awsLambdaClient, functionName, functionAlias, functionVersion);
		}

		public FeignLambdaServiceInvokerClient build() {
			return create(resolveAwsLambdaClient(), resolveFunctionName(), functionAlias, functionVersion);
		}
	}

}
