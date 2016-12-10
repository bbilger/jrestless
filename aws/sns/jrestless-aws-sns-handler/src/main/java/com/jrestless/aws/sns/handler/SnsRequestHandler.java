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
package com.jrestless.aws.sns.handler;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.jrestless.aws.AwsFeature;
import com.jrestless.aws.sns.SnsFeature;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.DefaultJRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequest;

/**
 * Base AWS SNS request handler.
 * <p>
 * Note: we don't implement
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler RequestHandler}
 * in case we need
 * {@link com.amazonaws.services.lambda.runtime.RequestStreamHandler
 * RequestStreamHandler} at some point.
 * <p>
 *
 *
 * @author Bjoern Bilger
 *
 */
public abstract class SnsRequestHandler extends SimpleRequestHandler<SnsRecordAndLambdaContext, Void> {

	private static final Logger LOG = LoggerFactory.getLogger(SnsRequestHandler.class);

	private final URI baseUri;

	public SnsRequestHandler() {
		this(URI.create("/"));
	}

	public SnsRequestHandler(URI baseUri) {
		this.baseUri = baseUri;
	}

	@Override
	public final JRestlessContainerRequest createContainerRequest(
			@Nonnull SnsRecordAndLambdaContext snsRecordAndContext) {
		requireNonNull(snsRecordAndContext);
		InputStream entityStream = requireNonNull(createEntityStream(snsRecordAndContext));
		Map<String, List<String>> headers = requireNonNull(createHeaders(snsRecordAndContext));
		String httpMethod = requireNonNull(createHttpMethod(snsRecordAndContext));
		URI requestUri = requireNonNull(createRequestUri(snsRecordAndContext));
		return new DefaultJRestlessContainerRequest(baseUri, requestUri, httpMethod, entityStream, headers);
	}

	/**
	 * Creates the entity input stream (the request body) for the actual request
	 * made to the Jersey container.
	 * <p>
	 * The default implementation uses the SNS message as body.
	 *
	 * @param snsRecordAndContext
	 * @return the entity input stream
	 */
	@Nonnull
	public InputStream createEntityStream(@Nonnull SnsRecordAndLambdaContext snsRecordAndContext) {
		String message = snsRecordAndContext.getSnsRecord().getSNS().getMessage();
		if (message == null) {
			return new ByteArrayInputStream(new byte[0]);
		} else {
			return new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Creates the request headers for the actual request made to the Jersey
	 * cotainer.
	 * <p>
	 * The default implementation returns a map with the Content-Type header set
	 * to "application/json".
	 *
	 * @param snsRecordAndContext
	 * @return the request headers
	 */
	@Nonnull
	public Map<String, List<String>> createHeaders(@Nonnull SnsRecordAndLambdaContext snsRecordAndContext) {
		return Collections.singletonMap(HttpHeaders.CONTENT_TYPE,
				Collections.singletonList(MediaType.APPLICATION_JSON));
	}

	/**
	 * Creates the http method for the actual request made to the Jersey
	 * container.
	 * <p>
	 * The default implementation uses "POST", always.
	 *
	 * @param snsRecordAndContext
	 * @return the request's http method
	 */
	@Nonnull
	public String createHttpMethod(@Nonnull SnsRecordAndLambdaContext snsRecordAndContext) {
		return HttpMethod.POST;
	}

	/**
	 * Creates the request URI for the actual request made to the Jersey
	 * container.
	 * <p>
	 * The default implementation uses the topic name (the last part of the
	 * topic arn) and concatenates the subject if one is set:
	 * {@code topicName + "/" + subject}.
	 *
	 * @param snsRecordAndContext
	 * @return the request URI
	 */
	@Nonnull
	public URI createRequestUri(@Nonnull SnsRecordAndLambdaContext snsRecordAndContext) {
		SNS sns = snsRecordAndContext.getSnsRecord().getSNS();
		String subject = sns.getSubject();
		String topicArn = sns.getTopicArn();
		int lastColonIndex = topicArn.lastIndexOf(':');
		String topicName = topicArn.substring(lastColonIndex + 1);
		if (subject != null && !subject.trim().isEmpty()) {
			return URI.create("/" + topicName + "/" + subject);
		} else {
			return URI.create("/" + topicName);
		}
	}

	@Override
	public final SimpleResponseWriter<Void> createResponseWriter(SnsRecordAndLambdaContext snsRecordAndContext) {
		return new ResponseWriter(snsRecordAndContext);
	}

	@Override
	public void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, SnsRecordAndLambdaContext snsRecordAndContext) {
		SNSRecord snsRecord = snsRecordAndContext.getSnsRecord();
		Context lambdaContext = snsRecordAndContext.getLambdaContext();
		actualContainerRequest.setRequestScopedInitializer(locator -> {
			Ref<SNSRecord> snsRecordRef = locator.<Ref<SNSRecord>>getService(SnsFeature.SNS_RECORD_TYPE);
			if (snsRecordRef != null) {
				snsRecordRef.set(snsRecord);
			} else {
				LOG.error("SnsFeature has not been registered. SNSRecord injection won't work.");
			}
			Ref<Context> contextRef = locator.<Ref<Context>>getService(AwsFeature.CONTEXT_TYPE);
			if (contextRef != null) {
				contextRef.set(lambdaContext);
			} else {
				LOG.error("AwsFeature has not been registered. Context injection won't work.");
			}
		});
	}

	/**
	 * Hook method to deal with responses.
	 * <p>
	 * SNS cannot react to responses this is mainly here to log issues.
	 * <p>
	 * The default implementation logs all non 2xx responses to error and non
	 * 204 responses to warning.
	 *
	 * @param snsRecordAndContext
	 *            the request
	 * @param statusType
	 *            the response status type
	 * @param headers
	 *            the response headers
	 * @param entityOutputStream
	 *            the response body
	 */
	public void handleReponse(SnsRecordAndLambdaContext snsRecordAndContext, StatusType statusType,
			Map<String, List<String>> headers, ByteArrayOutputStream entityOutputStream) {
		Supplier<String> logMsgSupplier = () -> "endpoints consuming sns events should respond with 204 but got '"
				+ statusType.getStatusCode() + "' for topic '"
				+ snsRecordAndContext.getSnsRecord().getSNS().getTopicArn() + "'";
		if (!Status.Family.SUCCESSFUL.equals(statusType.getFamily())) {
			LOG.error(logMsgSupplier.get());
		} else if (!Status.NO_CONTENT.equals(statusType)) {
			LOG.warn(logMsgSupplier.get());
		}
	}

	@Override
	public Void onRequestFailure(Exception e, SnsRecordAndLambdaContext request,
			JRestlessContainerRequest containerRequest) {
		LOG.error("request failed", e);
		return null;
	}

	private class ResponseWriter implements SimpleResponseWriter<Void> {
		private final SnsRecordAndLambdaContext snsRecordAndContext;

		ResponseWriter(SnsRecordAndLambdaContext snsRecordAndContext) {
			this.snsRecordAndContext = snsRecordAndContext;
		}

		@Override
		public OutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			SnsRequestHandler.this.handleReponse(snsRecordAndContext, statusType, headers,
					(ByteArrayOutputStream) entityOutputStream);
		}

		@Override
		public Void getResponse() {
			return null;
		}
	}
}
