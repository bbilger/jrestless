package com.jrestless.aws.service.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.service.ServiceResourceConfig;
import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceRequestImpl;
import com.jrestless.aws.service.io.ServiceResponse;
import com.jrestless.aws.service.io.ServiceResponseImpl;
import com.jrestless.core.container.dpi.InstanceBinder;

public class ServiceRequestObjectHandlerIntTest {

	private ServiceRequestObjectHandler handler;
	private TestService testService;
	private Context context = mock(Context.class);

	@Before
	public void setup() {
		ResourceConfig config = new ServiceResourceConfig();
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		handler = spy(new ServiceRequestObjectHandlerImpl());
		handler.init(config);
		handler.start();
	}

	@Test
	public void testLambdaContextInjection() {
		ServiceRequestImpl request = new ServiceRequestImpl(null, new HashMap<>(), URI.create("/"), "DELETE");
		handler.handleRequest(request, context);
		verify(testService).injectLambdaContext(context);
	}

	@Test
	public void testServiceRequestInjection() {
		ServiceRequestImpl request = new ServiceRequestImpl(null, new HashMap<>(), URI.create("/inject-service-request"), "PUT");
		handler.handleRequest(request, context);
		verify(testService).injectServiceRequest(same(request));
	}

	@Test
	public void testContainerFailureCreates500() {
		ServiceRequestImpl request = new ServiceRequestImpl(null, new HashMap<>(), URI.create("/"), "DELETE");
		doThrow(new RuntimeException()).when(handler).createContainerRequest(any());
		ServiceResponse response = handler.handleRequest(request, context);
		assertEquals(new ServiceResponseImpl(null, new HashMap<>(), 500, "Internal Server Error"), response);
	}

	@Test
	public void testRoundTrip() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		List<String> jsonMediaType = Collections.singletonList(MediaType.APPLICATION_JSON);
		Map<String, List<String>> requestHeaders = ImmutableMap.of(HttpHeaders.ACCEPT, jsonMediaType,
				HttpHeaders.CONTENT_TYPE, jsonMediaType);
		String requestBody = mapper.writeValueAsString(new Entity("123"));
		ServiceRequestImpl request = new ServiceRequestImpl(requestBody, requestHeaders, URI.create("/simple"), "POST");
		ServiceResponse response = handler.handleRequest(request, context);
		Map<String, List<String>> responseHeaders = ImmutableMap.of(HttpHeaders.CONTENT_TYPE, jsonMediaType);
		String responseBody = mapper.writeValueAsString(new Entity("123"));
		assertEquals(new ServiceResponseImpl(responseBody, responseHeaders, 200, "OK"), response);
	}

	@Path("/")
	public static class TestResource {

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
		}

		@DELETE
		public Response injectLambdaContext(@javax.ws.rs.core.Context Context context) {
			service.injectLambdaContext(context);
			return Response.ok().build();
		}

		@Path("/inject-service-request")
		@PUT
		public Response injectServiceRequest(@javax.ws.rs.core.Context ServiceRequest request) {
			service.injectServiceRequest(request);
			return Response.ok().build();
		}

		@Path("/simple")
		@POST
		public Response putSomething(Entity entity) {
			return Response.ok(entity).build();
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectServiceRequest(ServiceRequest request);
	}

	public static class Entity {
		private String value;

		@JsonCreator
		public Entity(@JsonProperty("value") String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
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
			Entity castOther = (Entity) other;
			return Objects.equals(value, castOther.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class ServiceRequestObjectHandlerImpl extends ServiceRequestObjectHandler {
	}
}
