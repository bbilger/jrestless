package com.jrestless.aws.sns.handler;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.jrestless.aws.AwsFeature;
import com.jrestless.aws.sns.SnsFeature;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.io.JRestlessContainerRequest;

public class SnsRequestHandlerTest {
	private JRestlessHandlerContainer<JRestlessContainerRequest> container;
	private SnsRequestHandlerImpl snsHandler;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		container = mock(JRestlessHandlerContainer.class);
		snsHandler = spy(new SnsRequestHandlerImpl());
		snsHandler.doInit(container);
		snsHandler.doStart();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void delegateRequest_ValidRequestAndReferencesGiven_ShouldSetReferencesOnRequestInitialization() {

		Context context = mock(Context.class);
		SNS sns = new SNS();
		sns.setTopicArn(":t");
		SNSRecord snsRecord = new SNSRecord();
		snsRecord.setSns(sns);

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, snsRecord);

		Ref<SNSRecord> snsRef = mock(Ref.class);
		Ref<Context> contextRef = mock(Ref.class);

		ServiceLocator serviceLocator = mock(ServiceLocator.class);
		when(serviceLocator.getService(SnsFeature.SNS_RECORD_TYPE)).thenReturn(snsRef);
		when(serviceLocator.getService(AwsFeature.CONTEXT_TYPE)).thenReturn(contextRef);

		requestScopedInitializer.initialize(serviceLocator);

		verify(snsRef).set(snsRecord);
		verify(contextRef).set(context);
	}

	@Test
	public void delegateRequest_ValidRequestAndNoReferencesGiven_ShouldNotFailOnRequestInitialization() {

		Context context = mock(Context.class);
		SNS sns = new SNS();
		sns.setTopicArn(":t");
		SNSRecord snsRecord = new SNSRecord();
		snsRecord.setSns(sns);

		RequestScopedInitializer requestScopedInitializer = getSetRequestScopedInitializer(context, snsRecord);

		ServiceLocator serviceLocator = mock(ServiceLocator.class);
		requestScopedInitializer.initialize(serviceLocator);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RequestScopedInitializer getSetRequestScopedInitializer(Context context, SNSRecord snsRecord) {
		SnsRecordAndLambdaContext reqAndContext = new SnsRecordAndLambdaContext(snsRecord, context);
		ArgumentCaptor<Consumer> containerEnhancerCaptor = ArgumentCaptor.forClass(Consumer.class);
		snsHandler.doDelegateRequest(reqAndContext);
		verify(container).handleRequest(any(), any(), any(), containerEnhancerCaptor.capture());

		ContainerRequest containerRequest = mock(ContainerRequest.class);
		containerEnhancerCaptor.getValue().accept(containerRequest);

		ArgumentCaptor<RequestScopedInitializer> requestScopedInitializerCaptor = ArgumentCaptor.forClass(RequestScopedInitializer.class);

		verify(containerRequest).setRequestScopedInitializer(requestScopedInitializerCaptor.capture());

		return requestScopedInitializerCaptor.getValue();
	}

	@Test
	public void createEntityStream_MessageGiven_ShouldUseMessageToCreateStream() {
		SnsRecordAndLambdaContext reqAndContext = createMinimalRequest();
		final String requestBody = "some message body";
		reqAndContext.getSnsRecord().getSNS().setMessage(requestBody);
		ByteArrayInputStream bais = (ByteArrayInputStream) new SnsRequestHandlerImpl()
				.createEntityStream(reqAndContext);
		assertArrayEquals(requestBody.getBytes(StandardCharsets.UTF_8), toBytes(bais));
	}

	@Test
	public void createEntityStream_NoMessageGiven_ShouldCreateEmptyStream() {
		SnsRecordAndLambdaContext reqAndContext = createMinimalRequest();
		reqAndContext.getSnsRecord().getSNS().setMessage(null);
		ByteArrayInputStream bais = (ByteArrayInputStream) new SnsRequestHandlerImpl()
				.createEntityStream(reqAndContext);
		assertArrayEquals(new byte[0], toBytes(bais));
	}

	@Test
	public void createHeaders_ShouldAlwaysJsonContentTypeHeaders() {
		Map<String, List<String>> expectedHeaders = Collections.singletonMap(HttpHeaders.CONTENT_TYPE,
				Collections.singletonList(MediaType.APPLICATION_JSON));
		assertEquals(expectedHeaders, new SnsRequestHandlerImpl().createHeaders(null));
		assertEquals(expectedHeaders, new SnsRequestHandlerImpl().createHeaders(createMinimalRequest()));
	}

	@Test
	public void createHttpMethod_ShouldAlwaysCreateEmptyHeaders() {
		assertEquals("POST", new SnsRequestHandlerImpl().createHttpMethod(null));
		assertEquals("POST", new SnsRequestHandlerImpl().createHttpMethod(createMinimalRequest()));
	}

	@Test
	public void createRequestUri_TopicArnAndNoSubjectGiven_ShouldCreateUriUsingTopicName() {
		SnsRecordAndLambdaContext reqAndContext = createMinimalRequest();
		reqAndContext.getSnsRecord().getSNS().setTopicArn("a:b:c");
		assertEquals(URI.create("/c"), new SnsRequestHandlerImpl().createRequestUri(reqAndContext));
	}

	@Test
	public void createRequestUri_TopicArnAndBlankSubjectGiven_ShouldCreateUriUsingTopicName() {
		SnsRecordAndLambdaContext reqAndContext = createMinimalRequest();
		reqAndContext.getSnsRecord().getSNS().setTopicArn("a:b:c");
		reqAndContext.getSnsRecord().getSNS().setSubject("  ");
		assertEquals(URI.create("/c"), new SnsRequestHandlerImpl().createRequestUri(reqAndContext));
	}

	@Test
	public void createRequestUri_TopicArnAndSubjectGiven_ShouldCreateUriUsingTopicNameAndSubject() {
		SnsRecordAndLambdaContext reqAndContext = createMinimalRequest();
		reqAndContext.getSnsRecord().getSNS().setTopicArn("a:b:c");
		reqAndContext.getSnsRecord().getSNS().setSubject("someSubject");
		assertEquals(URI.create("/c/someSubject"), new SnsRequestHandlerImpl().createRequestUri(reqAndContext));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void createResponseWriter_writeResponse_Always_ShouldDelegateResponseToHandler() throws IOException {
		SnsRecordAndLambdaContext reqAndContext = mock(SnsRecordAndLambdaContext.class);
		SNS sns = new SNS();
		sns.setTopicArn(":t");
		SNSRecord snsRecord = new SNSRecord();
		snsRecord.setSns(sns);
		when(reqAndContext.getSnsRecord()).thenReturn(snsRecord);

		StatusType statusType = mock(StatusType.class);
		Map<String, List<String>> headers = mock(Map.class);
		ByteArrayOutputStream entityOutputStream = mock(ByteArrayOutputStream.class);
		snsHandler.createResponseWriter(reqAndContext).writeResponse(statusType, headers, entityOutputStream);
		verify(snsHandler).handleReponse(reqAndContext, statusType, headers, entityOutputStream);
	}

	@Test
	public void createContainerRequest_Always_ShouldUseHooksToCreateContainerRequest() {
		SnsRecordAndLambdaContext reqAndContext = mock(SnsRecordAndLambdaContext.class);

		InputStream entityInputStream = mock(InputStream.class);
		doReturn(entityInputStream).when(snsHandler).createEntityStream(reqAndContext);

		Map<String, List<String>> headers = Collections.singletonMap("a", Collections.singletonList("b"));
		doReturn(headers).when(snsHandler).createHeaders(reqAndContext);

		String httpMethod = "someHttpMethod";
		doReturn(httpMethod).when(snsHandler).createHttpMethod(reqAndContext);

		URI requestUri = URI.create("someRequestUri");
		doReturn(requestUri).when(snsHandler).createRequestUri(reqAndContext);

		ArgumentCaptor<JRestlessContainerRequest> containerRequestCaptor = ArgumentCaptor
				.forClass(JRestlessContainerRequest.class);
		snsHandler.doDelegateRequest(reqAndContext);

		verify(container).handleRequest(containerRequestCaptor.capture(), any(), any(), any());

		JRestlessContainerRequest containerRequest = containerRequestCaptor.getValue();

		assertSame(entityInputStream, containerRequest.getEntityStream());
		assertEquals(headers, containerRequest.getHeaders());
		assertNotSame(headers, containerRequest.getHeaders());
		assertEquals(httpMethod, containerRequest.getHttpMethod());
		assertEquals(requestUri, containerRequest.getRequestUri());
	}

	private SnsRecordAndLambdaContext createMinimalRequest() {
		SNS sns = new SNS();
		sns.setTopicArn(":t");
		SNSRecord snsRecord = new SNSRecord();
		snsRecord.setSns(sns);
		return new SnsRecordAndLambdaContext(snsRecord, null);
	}

	public static byte[] toBytes(ByteArrayInputStream bais) {
		byte[] array = new byte[bais.available()];
		try {
			bais.read(array);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return array;
	}

	private static class SnsRequestHandlerImpl extends SnsRequestHandler {
		void doStart() {
			start();
		}
		void doInit(JRestlessHandlerContainer<JRestlessContainerRequest> container) {
			init(container);
		}
		void doDelegateRequest(SnsRecordAndLambdaContext request) {
			delegateRequest(request);
		}
	}
}
