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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + ((apiId == null) ? 0 : apiId.hashCode());
		result = prime * result + ((apiKey == null) ? 0 : apiKey.hashCode());
		result = prime * result + ((caller == null) ? 0 : caller.hashCode());
		result = prime * result
				+ ((cognitoAuthenticationProvider == null) ? 0 : cognitoAuthenticationProvider.hashCode());
		result = prime * result + ((cognitoAuthenticationType == null) ? 0 : cognitoAuthenticationType.hashCode());
		result = prime * result + ((cognitoIdentityId == null) ? 0 : cognitoIdentityId.hashCode());
		result = prime * result + ((cognitoIdentityPoolId == null) ? 0 : cognitoIdentityPoolId.hashCode());
		result = prime * result + ((httpMethod == null) ? 0 : httpMethod.hashCode());
		result = prime * result + ((principalId == null) ? 0 : principalId.hashCode());
		result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
		result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
		result = prime * result + ((resourcePath == null) ? 0 : resourcePath.hashCode());
		result = prime * result + ((sourceIp == null) ? 0 : sourceIp.hashCode());
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + ((stageVariables == null) ? 0 : stageVariables.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
		result = prime * result + ((userArn == null) ? 0 : userArn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GatewayRequestContextImpl other = (GatewayRequestContextImpl) obj;
		if (accountId == null) {
			if (other.accountId != null) {
				return false;
			}
		} else if (!accountId.equals(other.accountId)) {
			return false;
		}
		if (apiId == null) {
			if (other.apiId != null) {
				return false;
			}
		} else if (!apiId.equals(other.apiId)) {
			return false;
		}
		if (apiKey == null) {
			if (other.apiKey != null) {
				return false;
			}
		} else if (!apiKey.equals(other.apiKey)) {
			return false;
		}
		if (caller == null) {
			if (other.caller != null) {
				return false;
			}
		} else if (!caller.equals(other.caller)) {
			return false;
		}
		if (cognitoAuthenticationProvider == null) {
			if (other.cognitoAuthenticationProvider != null) {
				return false;
			}
		} else if (!cognitoAuthenticationProvider.equals(other.cognitoAuthenticationProvider)) {
			return false;
		}
		if (cognitoAuthenticationType == null) {
			if (other.cognitoAuthenticationType != null) {
				return false;
			}
		} else if (!cognitoAuthenticationType.equals(other.cognitoAuthenticationType)) {
			return false;
		}
		if (cognitoIdentityId == null) {
			if (other.cognitoIdentityId != null) {
				return false;
			}
		} else if (!cognitoIdentityId.equals(other.cognitoIdentityId)) {
			return false;
		}
		if (cognitoIdentityPoolId == null) {
			if (other.cognitoIdentityPoolId != null) {
				return false;
			}
		} else if (!cognitoIdentityPoolId.equals(other.cognitoIdentityPoolId)) {
			return false;
		}
		if (httpMethod == null) {
			if (other.httpMethod != null) {
				return false;
			}
		} else if (!httpMethod.equals(other.httpMethod)) {
			return false;
		}
		if (principalId == null) {
			if (other.principalId != null) {
				return false;
			}
		} else if (!principalId.equals(other.principalId)) {
			return false;
		}
		if (requestId == null) {
			if (other.requestId != null) {
				return false;
			}
		} else if (!requestId.equals(other.requestId)) {
			return false;
		}
		if (resourceId == null) {
			if (other.resourceId != null) {
				return false;
			}
		} else if (!resourceId.equals(other.resourceId)) {
			return false;
		}
		if (resourcePath == null) {
			if (other.resourcePath != null) {
				return false;
			}
		} else if (!resourcePath.equals(other.resourcePath)) {
			return false;
		}
		if (sourceIp == null) {
			if (other.sourceIp != null) {
				return false;
			}
		} else if (!sourceIp.equals(other.sourceIp)) {
			return false;
		}
		if (stage == null) {
			if (other.stage != null) {
				return false;
			}
		} else if (!stage.equals(other.stage)) {
			return false;
		}
		if (stageVariables == null) {
			if (other.stageVariables != null) {
				return false;
			}
		} else if (!stageVariables.equals(other.stageVariables)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (userAgent == null) {
			if (other.userAgent != null) {
				return false;
			}
		} else if (!userAgent.equals(other.userAgent)) {
			return false;
		}
		if (userArn == null) {
			if (other.userArn != null) {
				return false;
			}
		} else if (!userArn.equals(other.userArn)) {
			return false;
		}
		return true;
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
