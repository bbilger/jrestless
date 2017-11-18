package com.jrestless.aws.service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jrestless.aws.service.io.ServiceRequest;
import com.jrestless.aws.service.io.ServiceResponse;
import com.jrestless.test.IOUtils;

public class AbstractFeignLambdaServiceClientTest {

	private feign.Request feignRequest = mock(feign.Request.class);
	private ServiceResponse serviceResponse = mock(ServiceResponse.class);

	@BeforeEach
	public void setup() throws UnsupportedEncodingException {
		when(feignRequest.url()).thenReturn("/");
		when(feignRequest.method()).thenReturn("GET");

		when(serviceResponse.getStatusCode()).thenReturn(200);
	}

	@Test
	public void execute_NullRequestBodyGiven_ShouldRequestWithNullBody() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		client.execute(feignRequest, null);
		assertEquals(null, client.getServiceRequest().getBody());
	}

	@Test
	public void execute_RequestBodyGiven_ShouldRequestWithBody() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.body()).thenReturn("body".getBytes());
		client.execute(feignRequest, null);
		assertEquals("body", client.getServiceRequest().getBody());
	}

	@Test
	public void execute_NullRequestHeadersGiven_ShouldRequestWithEmptyHeaders() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.headers()).thenReturn(null);
		client.execute(feignRequest, null);
		assertEquals(Collections.emptyMap(), client.getServiceRequest().getHeaders());
	}

	@Test
	public void execute_RequestHeadersGiven_ShouldRequestWithHeaders() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.headers()).thenReturn(Collections.singletonMap("k", Collections.singleton("v")));
		client.execute(feignRequest, null);
		assertEquals(Collections.singletonMap("k", Collections.singletonList("v")), client.getServiceRequest().getHeaders());
	}

	@Test
	public void execute_NoRequestUrlGiven_ShouldFail() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.url()).thenReturn(null);
		assertThrows(NullPointerException.class, () -> client.execute(feignRequest, null));
	}

	@Test
	public void execute_RequestUrlGiven_ShouldRequestWithUrl() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.url()).thenReturn("/whatever?a=b&c=d");
		client.execute(feignRequest, null);
		assertEquals(URI.create("/whatever?a=b&c=d"), client.getServiceRequest().getRequestUri());
	}

	@Test
	public void execute_NoHttpMethodGiven_ShouldFail() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.method()).thenReturn(null);
		assertThrows(NullPointerException.class, () -> client.execute(feignRequest, null));
	}

	@Test
	public void execute_HttpMethodGiven_ShouldRequestWithHttpMethod() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(feignRequest.method()).thenReturn("some method");
		client.execute(feignRequest, null);
		assertEquals("some method", client.getServiceRequest().getHttpMethod());
	}

	@Test
	public void execute_NullResponseBodyGiven_ShouldRespondWithNullBody() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(null, response.body());
	}

	@Test
	public void execute_ResponseBodyGiven_ShouldRespondWithBody() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getBody()).thenReturn("some body");
		feign.Response response = client.execute(feignRequest, null);
		assertEquals("some body", IOUtils.toString(response.body().asInputStream()));
	}

	@Test
	public void execute_NullResponseHeadersGiven_ShouldRespondWithEmptyHeaders() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getHeaders()).thenReturn(null);
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(Collections.emptyMap(), response.headers());
	}

	@Test
	public void execute_ResponseHeadersGiven_ShouldRespondWithHeaders() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getHeaders()).thenReturn(Collections.singletonMap("k", Collections.singletonList("v")));
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(Collections.singletonMap("k", Collections.singletonList("v")), response.headers());
	}

	@Test
	public void execute_NullResponseReasonPhraseGiven_ShouldRespondWithNullReasonPhrase() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(null, response.reason());
	}

	@Test
	public void execute_ResponseReasonPhraseGiven_ShouldRespondWithReasonPhrase() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getReasonPhrase()).thenReturn("some reason");
		feign.Response response = client.execute(feignRequest, null);
		assertEquals("some reason", response.reason());
	}

	@Test
	public void execute_OkStatusGiven_ShouldRespondWithOkStatus() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getStatusCode()).thenReturn(200);
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(200, response.status());
	}

	@Test
	public void execute_BadStatusGiven_ShouldRespondWithBadStatus() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		when(serviceResponse.getStatusCode()).thenReturn(400);
		feign.Response response = client.execute(feignRequest, null);
		assertEquals(400, response.status());
	}

	@Test
	public void execute_NullOptionsGiven_ShouldPassNullOptionsToInternalExec() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		feign.Request.Options feignOptions = mock(feign.Request.Options.class);
		client.execute(feignRequest, feignOptions);
		assertEquals(feignOptions, client.getFeignRequestOptions());
	}

	@Test
	public void execute_OptionsGiven_ShouldPassOptionsToInternalExec() throws IOException {
		FeignLambdaClientImpl client = new FeignLambdaClientImpl(serviceResponse);
		client.execute(feignRequest, null);
		assertEquals(null, client.getFeignRequestOptions());
	}

	private static class FeignLambdaClientImpl extends AbstractFeignLambdaServiceClient {
		private ServiceResponse response;
		private ServiceRequest serviceRequest;
		private feign.Request.Options feignRequestOptions;

		private FeignLambdaClientImpl(ServiceResponse response) {
			this.response = response;
		}

		@Override
		protected ServiceResponse execute(ServiceRequest serviceRequest, feign.Request.Options feignOptions) {
			this.serviceRequest = serviceRequest;
			this.feignRequestOptions = feignOptions;
			return response;
		}

		ServiceRequest getServiceRequest() {
			return serviceRequest;
		}

		feign.Request.Options getFeignRequestOptions() {
			return feignRequestOptions;
		}

	}
}
