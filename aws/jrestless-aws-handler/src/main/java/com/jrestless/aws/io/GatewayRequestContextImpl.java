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

import java.util.Objects;

import com.jrestless.aws.GatewayIdentity;
import com.jrestless.aws.GatewayRequestContext;

/**
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayRequestContextImpl implements GatewayRequestContext {

	private String accountId;
	private String resourceId;
	private String stage;
	private String requestId;
	private GatewayIdentity identity;
	private String resourcePath;
	private String httpMethod;
	private String apiId;

	public GatewayRequestContextImpl() {
	}

	// for unit testing, only
	// CHECKSTYLE:OFF
	GatewayRequestContextImpl(String accountId, String resourceId, String stage, String requestId,
			GatewayIdentityImpl identity, String resourcePath, String httpMethod, String apiId) {
		super();
		setAccountId(accountId);
		setResourceId(resourceId);
		setStage(stage);
		setRequestId(requestId);
		setIdentity(identity);
		setResourcePath(resourcePath);
		setHttpMethod(httpMethod);
		setApiId(apiId);
	}
	// CHECKSTYLE:ON

	@Override
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public GatewayIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(GatewayIdentityImpl identity) {
		this.identity = identity;
	}

	@Override
	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	@Override
	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = apiId;
	}


	@Override
	public String toString() {
		return "GatewayRequestContextImpl [accountId=" + accountId + ", resourceId=" + resourceId + ", stage=" + stage
				+ ", requestId=" + requestId + ", identity=" + identity + ", resourcePath=" + resourcePath
				+ ", httpMethod=" + httpMethod + ", apiId=" + apiId + "]";
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		GatewayRequestContextImpl castOther = (GatewayRequestContextImpl) other;
		return Objects.equals(accountId, castOther.accountId) && Objects.equals(resourceId, castOther.resourceId)
				&& Objects.equals(stage, castOther.stage) && Objects.equals(requestId, castOther.requestId)
				&& Objects.equals(identity, castOther.identity) && Objects.equals(resourcePath, castOther.resourcePath)
				&& Objects.equals(httpMethod, castOther.httpMethod) && Objects.equals(apiId, castOther.apiId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, resourceId, stage, requestId, identity, resourcePath, httpMethod, apiId);
	}
}
