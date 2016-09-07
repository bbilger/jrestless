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

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jrestless.aws.GatewayRequestContext;


/**
 * AWS API Gateway request context object.
 * <p>
 * The implementation highly depends on the AWS API Gateway request template and
 * is designed to get de-serialized from it.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayRequestContextImpl implements GatewayRequestContext {

	private String apiId;
	private String principalId;
	private String httpMethod;
	private String accountId;
	private String apiKey;
	private String caller;
	private String cognitoAuthenticationProvider;
	private String cognitoAuthenticationType;
	private String cognitoIdentityId;
	private String cognitoIdentityPoolId;
	private String sourceIp;
	private String user;
	private String userAgent;
	private String userArn;
	private String requestId;
	private String resourceId;
	private String resourcePath;
	private String stage;
	private Map<String, String> stageVariables = new HashMap<>();

	public GatewayRequestContextImpl() {
	}
	// for unit test
	// CHECKSTYLE:OFF
	GatewayRequestContextImpl(String apiId, String principalId, String httpMethod, String accountId, String apiKey,
			String caller, String cognitoAuthenticationProvider, String cognitoAuthenticationType,
			String cognitoIdentityId, String cognitoIdentityPoolId, String sourceIp, String user, String userAgent,
			String userArn, String requestId, String resourceId, String resourcePath, String stage,
			Map<String, String> stageVariables) {
		setApiId(apiId);
		setPrincipalId(principalId);
		setHttpMethod(httpMethod);
		setAccountId(accountId);
		setApiKey(apiKey);
		setCaller(caller);
		setCognitoAuthenticationProvider(cognitoAuthenticationProvider);
		setCognitoAuthenticationType(cognitoAuthenticationType);
		setCognitoIdentityId(cognitoIdentityId);
		setCognitoIdentityPoolId(cognitoIdentityPoolId);
		setSourceIp(sourceIp);
		setUser(user);
		setUserAgent(userAgent);
		setUserArn(userArn);
		setRequestId(requestId);
		setResourceId(resourceId);
		setResourcePath(resourcePath);
		setStage(stage);
		setStageVariables(stageVariables);
	}
	// CHECKSTYLE:ON

	@Override
	public String getApiId() {
		return apiId;
	}

	public void setApiId(String apiId) {
		this.apiId = trimToNull(apiId);
	}

	@Override
	public String getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(String principalId) {
		this.principalId = trimToNull(principalId);
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = trimToNull(httpMethod);
	}

	@Override
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = trimToNull(accountId);
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = trimToNull(apiKey);
	}

	@Override
	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = trimToNull(caller);
	}

	@Override
	public String getCognitoAuthenticationProvider() {
		return cognitoAuthenticationProvider;
	}

	public void setCognitoAuthenticationProvider(String cognitoAuthenticationProvider) {
		this.cognitoAuthenticationProvider = trimToNull(cognitoAuthenticationProvider);
	}

	@Override
	public String getCognitoAuthenticationType() {
		return cognitoAuthenticationType;
	}

	public void setCognitoAuthenticationType(String cognitoAuthenticationType) {
		this.cognitoAuthenticationType = trimToNull(cognitoAuthenticationType);
	}

	@Override
	public String getCognitoIdentityId() {
		return cognitoIdentityId;
	}

	public void setCognitoIdentityId(String cognitoIdentityId) {
		this.cognitoIdentityId = trimToNull(cognitoIdentityId);
	}

	@Override
	public String getCognitoIdentityPoolId() {
		return cognitoIdentityPoolId;
	}

	public void setCognitoIdentityPoolId(String cognitoIdentityPoolId) {
		this.cognitoIdentityPoolId = trimToNull(cognitoIdentityPoolId);
	}

	@Override
	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = trimToNull(sourceIp);
	}

	@Override
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = trimToNull(user);
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = trimToNull(userAgent);
	}

	@Override
	public String getUserArn() {
		return userArn;
	}

	public void setUserArn(String userArn) {
		this.userArn = trimToNull(userArn);
	}

	@Override
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = trimToNull(requestId);
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = trimToNull(resourceId);
	}

	@Override
	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = trimToNull(resourcePath);
	}

	@Override
	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = trimToNull(stage);
	}

	@Override
	public Map<String, String> getStageVariables() {
		return Collections.unmodifiableMap(stageVariables);
	}

	public void setStageVariables(Map<String, String> stageVariables) {
		this.stageVariables.clear();
		if (stageVariables != null) {
			this.stageVariables.putAll(stageVariables);
		}
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
		return Objects.equals(apiId, castOther.apiId) && Objects.equals(principalId, castOther.principalId)
				&& Objects.equals(httpMethod, castOther.httpMethod) && Objects.equals(accountId, castOther.accountId)
				&& Objects.equals(apiKey, castOther.apiKey) && Objects.equals(caller, castOther.caller)
				&& Objects.equals(cognitoAuthenticationProvider, castOther.cognitoAuthenticationProvider)
				&& Objects.equals(cognitoAuthenticationType, castOther.cognitoAuthenticationType)
				&& Objects.equals(cognitoIdentityId, castOther.cognitoIdentityId)
				&& Objects.equals(cognitoIdentityPoolId, castOther.cognitoIdentityPoolId)
				&& Objects.equals(sourceIp, castOther.sourceIp) && Objects.equals(user, castOther.user)
				&& Objects.equals(userAgent, castOther.userAgent) && Objects.equals(userArn, castOther.userArn)
				&& Objects.equals(requestId, castOther.requestId) && Objects.equals(resourceId, castOther.resourceId)
				&& Objects.equals(resourcePath, castOther.resourcePath) && Objects.equals(stage, castOther.stage)
				&& Objects.equals(stageVariables, castOther.stageVariables);
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiId, principalId, httpMethod, accountId, apiKey, caller, cognitoAuthenticationProvider,
				cognitoAuthenticationType, cognitoIdentityId, cognitoIdentityPoolId, sourceIp, user, userAgent, userArn,
				requestId, resourceId, resourcePath, stage, stageVariables);
	}

	@Override
	public String toString() {
		return "GatewayRequestContextImpl [apiId=" + apiId + ", principalId=" + principalId + ", httpMethod="
				+ httpMethod + ", accountId=" + accountId + ", apiKey=" + apiKey + ", caller=" + caller
				+ ", cognitoAuthenticationProvider=" + cognitoAuthenticationProvider + ", cognitoAuthenticationType="
				+ cognitoAuthenticationType + ", cognitoIdentityId=" + cognitoIdentityId + ", cognitoIdentityPoolId="
				+ cognitoIdentityPoolId + ", sourceIp=" + sourceIp + ", user=" + user + ", userAgent=" + userAgent
				+ ", userArn=" + userArn + ", requestId=" + requestId + ", resourceId=" + resourceId + ", resourcePath="
				+ resourcePath + ", stage=" + stage + ", stageVariables=" + stageVariables + "]";
	}
}
