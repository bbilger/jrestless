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
package com.jrestless.aws.swagger.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration file for generating the AWS API Gateway extensions for Swagger.
 * <p>
 * Before accessing any of the getters, it must be checked via the corresponding
 * isSet* method if the property is available, at all. Only if the property
 * isSet, the getter may be invoked; else the behavior is undefined.
 *
 * @author Bjoern Bilger
 *
 */
public class AwsSwaggerConfiguration {

	private String lambdaCredential;
	private String lambdaUri;
	private Boolean defaultCorsEnabled;
	private String defaultAccessControlAllowOrigin;
	private String defaultAccessControlAllowHeaders;
	private AuthType defaultAuthType;
	private List<Map<String, List<String>>> defaultSecurity;

	// CHECKSTYLE:OFF
	@JsonCreator
	AwsSwaggerConfiguration(@JsonProperty("lambdaCredential") String lambdaCredential,
			@JsonProperty("lambdaUri") String lambdaUri, @JsonProperty("defaultCorsEnabled") Boolean defaultCorsEnabled,
			@JsonProperty("defaultAccessControlAllowOrigin") String defaultAccessControlAllowOrigin,
			@JsonProperty("defaultAccessControlAllowHeaders") String defaultAccessControlAllowHeaders,
			@JsonProperty("defaultAuthType") AuthType defaultAuthType,
			@JsonProperty("defaultSecurity") List<Map<String, List<String>>> defaultSecurity) {
		super();
		this.lambdaCredential = lambdaCredential;
		this.lambdaUri = lambdaUri;
		this.defaultCorsEnabled = defaultCorsEnabled;
		this.defaultAccessControlAllowOrigin = defaultAccessControlAllowOrigin;
		this.defaultAccessControlAllowHeaders = defaultAccessControlAllowHeaders;
		this.defaultAuthType = defaultAuthType;
		this.defaultSecurity = defaultSecurity;
	}
	// CHECKSTYLE:ON

	public boolean isSetLambdaCredential() {
		return lambdaCredential != null;
	}

	/**
	 * The lambda credentials.
	 *
	 * @return
	 */
	public String getLambdaCredential() {
		return lambdaCredential;
	}

	public boolean isSetLambdaUri() {
		return lambdaUri != null;
	}

	/**
	 * The lambda uri.
	 *
	 * @return
	 */
	public String getLambdaUri() {
		return lambdaUri;
	}

	public boolean isSetDefaultCorsEnabled() {
		return defaultCorsEnabled != null;
	}

	/**
	 * The default CORS state if none is given explicitly on resource (class) or
	 * endpoint (method) level via
	 * {@link com.jrestless.aws.annotation.Cors#enabled()}.
	 *
	 * @return
	 */
	public boolean isDefaultCorsEnabled() {
		return defaultCorsEnabled;
	}

	public boolean isSetDefaultAccessControlAllowOrigin() {
		return defaultAccessControlAllowOrigin != null;
	}

	/**
	 * The CORS header 'Access-Control-Allow-Origin' if none is given
	 * via Swagger annotations.
	 *
	 * @return
	 */
	public String getDefaultAccessControlAllowOrigin() {
		return defaultAccessControlAllowOrigin;
	}

	public boolean isSetDefaultAccessControlAllowHeaders() {
		return defaultAccessControlAllowHeaders != null;
	}


	/**
	 * The CORS header 'Access-Control-Allow-Headers' if none is given
	 * via Swagger annotations.
	 *
	 * @return
	 */
	public String getDefaultAccessControlAllowHeaders() {
		return defaultAccessControlAllowHeaders;
	}

	public boolean isSetDefaultAuthType() {
		return defaultAuthType != null;
	}

	/**
	 * The default authentication type for secured endpoints if no security is
	 * set via Swagger annotations.
	 *
	 * @return
	 */
	public AuthType getDefaultAuthType() {
		return defaultAuthType;
	}

	public boolean isSetDefaultSecurity() {
		return defaultSecurity != null;
	}

	/**
	 * The default security for secured endpoints if the authentication type is
	 * {@link AuthType#AUTHORIZER authorizer} and no security is set via Swagger
	 * annotations.
	 *
	 * @return
	 */
	public List<Map<String, List<String>>> getDefaultSecurity() {
		return defaultSecurity;
	}

	public enum AuthType {
		@JsonProperty("none")
		NONE,
		@JsonProperty("iam")
		IAM,
		@JsonProperty("authorizer")
		AUTHORIZER;

		@JsonCreator
		public static AuthType forValue(String value) {
			Objects.requireNonNull(value);
			return AuthType.valueOf(value.toUpperCase());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((defaultAccessControlAllowHeaders == null) ? 0 : defaultAccessControlAllowHeaders.hashCode());
		result = prime * result
				+ ((defaultAccessControlAllowOrigin == null) ? 0 : defaultAccessControlAllowOrigin.hashCode());
		result = prime * result + ((defaultAuthType == null) ? 0 : defaultAuthType.hashCode());
		result = prime * result + ((defaultCorsEnabled == null) ? 0 : defaultCorsEnabled.hashCode());
		result = prime * result + ((defaultSecurity == null) ? 0 : defaultSecurity.hashCode());
		result = prime * result + ((lambdaCredential == null) ? 0 : lambdaCredential.hashCode());
		result = prime * result + ((lambdaUri == null) ? 0 : lambdaUri.hashCode());
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
		AwsSwaggerConfiguration other = (AwsSwaggerConfiguration) obj;
		if (defaultAccessControlAllowHeaders == null) {
			if (other.defaultAccessControlAllowHeaders != null) {
				return false;
			}
		} else if (!defaultAccessControlAllowHeaders.equals(other.defaultAccessControlAllowHeaders)) {
			return false;
		}
		if (defaultAccessControlAllowOrigin == null) {
			if (other.defaultAccessControlAllowOrigin != null) {
				return false;
			}
		} else if (!defaultAccessControlAllowOrigin.equals(other.defaultAccessControlAllowOrigin)) {
			return false;
		}
		if (defaultAuthType != other.defaultAuthType) {
			return false;
		}
		if (defaultCorsEnabled == null) {
			if (other.defaultCorsEnabled != null) {
				return false;
			}
		} else if (!defaultCorsEnabled.equals(other.defaultCorsEnabled)) {
			return false;
		}
		if (defaultSecurity == null) {
			if (other.defaultSecurity != null) {
				return false;
			}
		} else if (!defaultSecurity.equals(other.defaultSecurity)) {
			return false;
		}
		if (lambdaCredential == null) {
			if (other.lambdaCredential != null) {
				return false;
			}
		} else if (!lambdaCredential.equals(other.lambdaCredential)) {
			return false;
		}
		if (lambdaUri == null) {
			if (other.lambdaUri != null) {
				return false;
			}
		} else if (!lambdaUri.equals(other.lambdaUri)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AwsSwaggerConfiguration [lambdaCredential=" + lambdaCredential + ", lambdaUri=" + lambdaUri
				+ ", defaultCorsEnabled=" + defaultCorsEnabled + ", defaultAccessControlAllowOrigin="
				+ defaultAccessControlAllowOrigin + ", defaultAccessControlAllowHeaders="
				+ defaultAccessControlAllowHeaders + ", defaultAuthType=" + defaultAuthType + ", defaultSecurity="
				+ defaultSecurity + "]";
	}


}
