package com.jrestless.aws.sns.handler;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.jrestless.aws.sns.SnsFeature;
import com.jrestless.core.container.dpi.InstanceBinder;

public class SnsRequestHandlerIntTest {
	private SnsRequestObjectHandlerImpl handler;
	private TestService testService;
	private Context context = mock(Context.class);

	@Before
	public void setup() {
		ResourceConfig config = new ResourceConfig();
		config.register(SnsFeature.class);
		testService = mock(TestService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
		config.register(binder);
		config.register(TestResource.class);
		handler = new SnsRequestObjectHandlerImpl();
		handler.init(config);
		handler.start();
	}

	@Test
	public void testLambdaContextInjection() {
		SNSEvent snsEvent = createSnsEvent("inject-lambda-context");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedLambdaContext(context);
	}

	@Test
	public void testLambdaContextMemberInjection() {
		when(context.getAwsRequestId()).thenReturn("0", "1");
		for (int i = 0; i <= 1; i++) {
			SNSEvent snsEvent = createSnsEvent("inject-lambda-context-member" + i);
			handler.handleRequest(snsEvent, context);
			verify(testService).injectedStringArg("" + i);
		}
	}

	@Test
	public void testSnsRecordInjection() {
		SNSEvent snsEvent = createSnsEvent("inject-sns-record");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedSns(same(snsEvent.getRecords().get(0).getSNS()));
	}

	/*
	 * this "test case" is here to remind me of the fact that since we inject
	 * a class and not an interface we end up having a proxy!
	 * If we had an interface we wouldn't get a proxy.
	 * If we didn't use proxy=true we wouldn't get a proxy.
	 */
	@Test(expected = IllegalStateException.class)
	public void testSnsRecordInjectionAsMock() {
		SNSEvent snsEvent = createSnsEvent("inject-sns-record-mock");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedSnsRecord(same(snsEvent.getRecords().get(0)));
	}

	@Test
	public void testSnsRecordInjectionAsMember() {
		SNSEvent snsEvent = createSnsEvent("inject-sns-record-member0");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedSns(snsEvent.getRecords().get(0).getSNS());
		snsEvent = createSnsEvent("inject-sns-record-member1");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedSns(snsEvent.getRecords().get(0).getSNS());
	}

	@Test
	public void testMultipleRecordsCreateMultipleRequest() {
		SNSEvent snsEvent = new SNSEvent();
		SNSRecord snsRecord0 = createSnsRecord("a:b:mytopic", "inject-sns-record-member0");
		SNSRecord snsRecord1 = createSnsRecord("a:b:mytopic", "inject-sns-record-member1");
		snsEvent.setRecords(ImmutableList.of(snsRecord0, snsRecord1));
		handler.handleRequest(snsEvent, context);
		InOrder inOrder = Mockito.inOrder(testService);
		inOrder.verify(testService).injectedSns(snsRecord0.getSNS());
		inOrder.verify(testService).injectedSns(snsRecord1.getSNS());
	}

	@Test
	public void testNoSubjectIsValid() {
		SNSEvent snsEvent = createSnsEvent(null);
		handler.handleRequest(snsEvent, context);
		verify(testService).hitRoot();
	}

	@Test
	public void testPostString() {
		SNSEvent snsEvent = createSnsEvent("plain-data");
		snsEvent.getRecords().get(0).getSNS().setMessage("123");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedStringArg("123");
	}

	@Test
	public void testPostJson() {
		SNSEvent snsEvent = createSnsEvent("entities");
		snsEvent.getRecords().get(0).getSNS().setMessage("{\"value\":\"some data\"}");
		handler.handleRequest(snsEvent, context);
		verify(testService).injectedStringArg("some data");
	}

	@Test
	public void testAlwaysReturnsNull() {
		assertNull(handler.handleRequest(createSnsEvent(":mytopic", "204"), context));
		assertNull(handler.handleRequest(createSnsEvent(":mytopic", "200"), context));
		assertNull(handler.handleRequest(createSnsEvent(":mytopic", "500"), context));
		// make sure the test case is still valid and we actually hit the endpoints
		verify(testService).hit200();
		verify(testService).hit204();
		verify(testService).hit500();
	}

	private SNSEvent createSnsEvent(String subject) {
		return createSnsEvent("a:b:mytopic", subject);
	}

	private SNSEvent createSnsEvent(String topicArn, String subject) {
		SNSEvent snsEvent = new SNSEvent();
		snsEvent.setRecords(Collections.singletonList(createSnsRecord(topicArn, subject)));
		return snsEvent;
	}

	private SNSRecord createSnsRecord(String topicArn, String subject) {
		SNS sns = new SNS();
		sns.setTopicArn(topicArn);
		sns.setSubject(subject);
		SNSRecord snsRecord = new SNSRecord();
		snsRecord.setSns(sns);
		return snsRecord;
	}

	@Singleton
	@Path("/mytopic")
	public static class TestResource {

		@javax.ws.rs.core.Context
		private Context lambdaContextMember;

		@javax.ws.rs.core.Context
		private SNSRecord snsRecordMember;

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
		}

		@POST
		public void root() {
			service.hitRoot();
		}

		@Path("/inject-lambda-context")
		@POST
		public void injectLambdaContext(@javax.ws.rs.core.Context Context context) {
			service.injectedLambdaContext(context);
		}

		@Path("/inject-lambda-context-member0")
		@POST
		public void injectLambdaContextAsMember0() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
		}

		@Path("/inject-lambda-context-member1")
		@POST
		public void injectLambdaContextAsMember1() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
		}

		@Path("/inject-sns-record")
		@POST
		public void injectSnsRecord(@javax.ws.rs.core.Context SNSRecord snsRecord) {
			service.injectedSns(snsRecord.getSNS());
		}

		@Path("/inject-sns-record-mock")
		@POST
		public void injectSnsRecordMock(@javax.ws.rs.core.Context SNSRecord snsRecord) {
			service.injectedSnsRecord(snsRecord);
		}

		@Path("/inject-sns-record-member0")
		@POST
		public void injectRecordAsMember0() {
			service.injectedSns(snsRecordMember.getSNS());
		}

		@Path("/inject-sns-record-member1")
		@POST
		public void injectSnsRecordAsMember1() {
			service.injectedSns(snsRecordMember.getSNS());
		}

		@Path("/plain-data")
		@POST
		public void putPlainData(String string) {
			service.injectedStringArg(string);
		}

		@Path("/entities")
		@POST
		public void putSomething(Entity entity) {
			service.injectedStringArg(entity.getValue());
		}

		@Path("/204")
		@POST
		public void getNothing() {
			service.hit204();
		}


		@Path("/200")
		@POST
		public String getSomething() {
			service.hit200();
			return "something";
		}


		@Path("/500")
		@POST
		public String fail() {
			service.hit500();
			throw new RuntimeException();
		}
	}

	public static interface TestService {
		void hit204();
		void hit200();
		void hit500();
		void hitRoot();
		void injectedLambdaContext(Context context);
		void injectedSns(SNS sns);
		void injectedSnsRecord(SNSRecord snsRecord);
		void injectedStringArg(String arg);
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

	private static class SnsRequestObjectHandlerImpl extends SnsRequestObjectHandler {
	}
}
