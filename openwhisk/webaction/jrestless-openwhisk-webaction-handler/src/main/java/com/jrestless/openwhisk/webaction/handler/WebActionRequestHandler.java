/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.openwhisk.webaction.handler;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.jrestless.core.container.dpi.AbstractReferencingBinder;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.DefaultJRestlessContainerRequest;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.RequestAndBaseUri;
import com.jrestless.core.util.HeaderUtils;
import com.jrestless.openwhisk.webaction.io.DefaultWebActionRequest;
import com.jrestless.openwhisk.webaction.io.WebActionRequest;

/**
 * Base OpenWhisk Web Action request handler.
 * <p>
 * Notes:
 * <ol>
 * <li>The request handler is suitable for "RAW HTTP handling", only.
 * <li>The request handler depends on
 * {@link com.jrestless.openwhisk.webaction.io.WebActionBase64ReadInterceptor
 * WebActionBase64ReadInterceptor} which must be registered on the
 * {@link org.glassfish.jersey.server.ResourceConfig ResourceConfig} - for
 * example using {@link com.jrestless.openwhisk.webaction.WebActionConfig
 * WebActionConfig}
 * <li>Subclasses need to implement how the response is written back
 * </ol>
 *
 * @author Bjoern Bilger
 *
 */
public abstract class WebActionRequestHandler extends SimpleRequestHandler<WebActionRequest, JsonObject> {

	private static final Logger LOG = LoggerFactory.getLogger(WebActionRequestHandler.class);
	private static final Gson GSON = new GsonBuilder().create();
	private static final Type WEB_ACTION_REQUEST_TYPE = (new GenericType<Ref<WebActionRequest>>() { }).getType();

	protected abstract JsonObject createJsonResponse(String body, Map<String, String> responseHeaders,
			StatusType statusType);

	public JsonObject delegateJsonRequest(@Nonnull JsonObject request) {
		try {
			requireNonNull(request, "request may not be null");
			return delegateRequest(GSON.fromJson(request, DefaultWebActionRequest.class));
		} catch (JsonSyntaxException  e) {
			LOG.error("request failed with", e);
			return createJsonResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected JRestlessContainerRequest createContainerRequest(WebActionRequest request) {
		requireNonNull(request);
		final String httpMethod = requireNonNull(request.getMethod(), "httpMethod must be given").toUpperCase();
		final String body = request.getBody();
		final Map<String, String> requestHeaders = request.getHeaders();
		final Map<String, List<String>> containerRequestHeaders;
		if (requestHeaders == null) {
			containerRequestHeaders = Collections.emptyMap();
		} else {
			containerRequestHeaders = HeaderUtils.expandHeaders(request.getHeaders());
		}
		InputStream entityStream;
		if (body != null) {
			entityStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
		} else {
			entityStream = new ByteArrayInputStream(new byte[0]);
		}
		final RequestAndBaseUri requestAndBaseUri = getRequestAndBaseUri(request);
		return new DefaultJRestlessContainerRequest(requestAndBaseUri, httpMethod, entityStream,
				containerRequestHeaders);
	}

	protected RequestAndBaseUri getRequestAndBaseUri(WebActionRequest request) {
		final String path = request.getPath();
		String generatedRequestUri;
		// prepend "/"
		if (path == null || path.isEmpty()) {
			generatedRequestUri = "/";
		} else if (!path.startsWith("/")) {
			generatedRequestUri = "/" + path;
		} else {
			generatedRequestUri = path;
		}
		// append query parameters
		if (request.getQuery() != null && !request.getQuery().isEmpty()) {
			generatedRequestUri += "?" + request.getQuery();
		}
		/*
		 * we have to use "/" as base URI since there is no proper way to get
		 * the base URI from the request
		 */
		return new RequestAndBaseUri(URI.create("/"), URI.create(generatedRequestUri));
	}

	@Override
	protected SimpleResponseWriter<JsonObject> createResponseWriter(
			WebActionRequest request) {
		return new ResponseWriter();
	}

	@Override
	protected JsonObject onRequestFailure(Exception e, WebActionRequest request,
			JRestlessContainerRequest containerRequest) {
		LOG.error("request failed", e);
		return createJsonResponse(null, Collections.emptyMap(), Status.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected void extendActualJerseyContainerRequest(ContainerRequest actualContainerRequest,
			JRestlessContainerRequest containerRequest, WebActionRequest request) {
		actualContainerRequest.setRequestScopedInitializer(locator -> {
			Ref<WebActionRequest> webActionRequestRef = locator
					.<Ref<WebActionRequest>>getInstance(WEB_ACTION_REQUEST_TYPE);
			if (webActionRequestRef != null) {
				webActionRequestRef.set(request);
			} else {
				LOG.error("WebActionBinder has not been registered. WebActionRequest injection won't work.");
			}
		});
	}

	@Override
	protected final Binder createBinder() {
		return new WebActionBinder();
	}

	private class ResponseWriter implements SimpleResponseWriter<JsonObject> {
		private JsonObject response;

		@Override
		public OutputStream getEntityOutputStream() {
			return new ByteArrayOutputStream();
		}

		@Override
		public void writeResponse(StatusType statusType, Map<String, List<String>> headers,
				OutputStream entityOutputStream) throws IOException {
			Map<String, String> flattenedHeaders = HeaderUtils.flattenHeaders(headers);
			String body = ((ByteArrayOutputStream) entityOutputStream).toString(StandardCharsets.UTF_8.name());
			response = createJsonResponse(body, flattenedHeaders, statusType);
		}

		@Override
		public JsonObject getResponse() {
			return response;
		}
	}

	private static class WebActionBinder extends AbstractReferencingBinder {
		@Override
		public void configure() {
			bindReferencingFactory(WebActionRequest.class, ReferencingWebActionRequestFactory.class,
					new GenericType<Ref<WebActionRequest>>() { });
		}
	}

	private static class ReferencingWebActionRequestFactory extends ReferencingFactory<WebActionRequest> {
		@Inject
		ReferencingWebActionRequestFactory(final Provider<Ref<WebActionRequest>> referenceFactory) {
			super(referenceFactory);
		}
	}
}
