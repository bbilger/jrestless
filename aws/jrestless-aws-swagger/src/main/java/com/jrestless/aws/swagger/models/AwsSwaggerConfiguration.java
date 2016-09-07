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

import java.util.Arrays;
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
	private int[] additionalResponseCodes;
	private String[] supportedNonDefaultHeadersInOrder;

	// CHECKSTYLE:OFF
	@JsonCreator
	AwsSwaggerConfiguration(@JsonProperty("lambdaCredential") String lambdaCredential,
			@JsonProperty("lambdaUri") String lambdaUri, @JsonProperty("defaultCorsEnabled") Boolean defaultCorsEnabled,
			@JsonProperty("defaultAccessControlAllowOrigin") String defaultAccessControlAllowOrigin,
			@JsonProperty("defaultAccessControlAllowHeaders") String defaultAccessControlAllowHeaders,
			@JsonProperty("defaultAuthType") AuthType defaultAuthType,
			@JsonProperty("defaultSecurity") List<Map<String, List<String>>> defaultSecurity,
			@JsonProperty("additionalResponseCodes") int[] additionalResponseCodes,
			@JsonProperty("supportedNonDefaultHeadersInOrder") String[] supportedNonDefaultHeadersInOrder) {
		super();
		this.lambdaCredential = lambdaCredential;
		this.lambdaUri = lambdaUri;
		this.defaultCorsEnabled = defaultCorsEnabled;
		this.defaultAccessControlAllowOrigin = defaultAccessControlAllowOrigin;
		this.defaultAccessControlAllowHeaders = defaultAccessControlAllowHeaders;
		this.defaultAuthType = defaultAuthType;
		this.defaultSecurity = defaultSecurity;
		this.additionalResponseCodes = additionalResponseCodes;
		this.supportedNonDefaultHeadersInOrder = supportedNonDefaultHeadersInOrder;
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

	/**
	 * A copy of the additional response code (or null if none).
	 *
	 * @return
	 */
	public int[] getAdditionalResponseCodes() {
		if (additionalResponseCodes == null) {
			return null;
		} else {
			return Arrays.copyOf(additionalResponseCodes, additionalResponseCodes.length);
		}
	}

	public boolean isSetAdditionalResponseCodes() {
		return additionalResponseCodes != null;
	}

	/**
	 * A copy of the supported non-default headers in order.
	 * <p>
	 * Non-default means we throw an exception from lambda in
	 * order to pass 400s, for example. The outermost exception will contain
	 * nested exceptions, the so called header exceptions, that contain the
	 * actual header values in the errorMessage field.
	 *
	 * @return
	 */
	public String[] getSupportedNonDefaultHeadersInOrder() {
		if (supportedNonDefaultHeadersInOrder == null) {
			return null;
		} else {
			return Arrays.copyOf(supportedNonDefaultHeadersInOrder, supportedNonDefaultHeadersInOrder.length);
		}
	}

	public boolean isSetSupportedNonDefaultHeadersInOrder() {
		return supportedNonDefaultHeadersInOrder != null;
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
		AwsSwaggerConfiguration castOther = AwsSwaggerConfiguration.class.cast(other);
		return Objects.equals(lambdaCredential, castOther.lambdaCredential)
				&& Objects.equals(lambdaUri, castOther.lambdaUri)
				&& Objects.equals(defaultCorsEnabled, castOther.defaultCorsEnabled)
				&& Objects.equals(defaultAccessControlAllowOrigin, castOther.defaultAccessControlAllowOrigin)
				&& Objects.equals(defaultAccessControlAllowHeaders, castOther.defaultAccessControlAllowHeaders)
				&& Objects.equals(defaultAuthType, castOther.defaultAuthType)
				&& Objects.equals(defaultSecurity, castOther.defaultSecurity)
				&& Arrays.equals(additionalResponseCodes, castOther.additionalResponseCodes)
				&& Arrays.equals(supportedNonDefaultHeadersInOrder, castOther.supportedNonDefaultHeadersInOrder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lambdaCredential, lambdaUri, defaultCorsEnabled, defaultAccessControlAllowOrigin,
				defaultAccessControlAllowHeaders, defaultAuthType, defaultSecurity, additionalResponseCodes,
				supportedNonDefaultHeadersInOrder);
	}

	@Override
	public String toString() {
		return "AwsSwaggerConfiguration [lambdaCredential=" + lambdaCredential + ", lambdaUri=" + lambdaUri
				+ ", defaultCorsEnabled=" + defaultCorsEnabled + ", defaultAccessControlAllowOrigin="
				+ defaultAccessControlAllowOrigin + ", defaultAccessControlAllowHeaders="
				+ defaultAccessControlAllowHeaders + ", defaultAuthType=" + defaultAuthType + ", defaultSecurity="
				+ defaultSecurity + ", additionalResponseCodes=" + Arrays.toString(additionalResponseCodes)
				+ ", supportedNonDefaultHeadersInOrder=" + Arrays.toString(supportedNonDefaultHeadersInOrder) + "]";
	}


}
