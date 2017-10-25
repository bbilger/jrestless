package com.jrestless.aws;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.core.container.dpi.InstanceBinder;

public class AbstractLambdaContextReferencingBinderIntTest extends JerseyTest {

	private TestService testService;
	private LambdaContextProvider lambdaContextProvider;

	@Override
	protected Application configure() {
		ResourceConfig application = new ResourceConfig();
		application.register(new AbstractLambdaContextReferencingBinder() {
			@Override
			protected void configure() {
				bindReferencingLambdaContextFactory();
			}
		});
		testService = mock(TestService.class);

		lambdaContextProvider = mock(LambdaContextProvider.class);

		Binder binder = new InstanceBinder.Builder()
				.addInstance(testService, TestService.class, Singleton.class)
				.addInstance(lambdaContextProvider, LambdaContextProvider.class, Singleton.class)
				.build();
		application.register(binder);

		application.register(TestResource.class);
		application.register(LambdaContextSetter.class);
		return application;
	}

	@Test
	public void testLambdaContextInjection() {
		Context lambdaContext = mock(Context.class);
		when(lambdaContextProvider.getLambdaContext()).thenReturn(lambdaContext);
		target().path("inject-lambda-context").request().get();
		verify(testService).injectLambdaContext(eq(lambdaContext));
	}

	@Test
	public void testLambdaContextInjectionAsMember() {
		Context lambdaContext0 = mock(Context.class);
		when(lambdaContext0.getAwsRequestId()).thenReturn("0");
		Context lambdaContext1 = mock(Context.class);
		when(lambdaContext1.getAwsRequestId()).thenReturn("1");
		when(lambdaContextProvider.getLambdaContext()).thenReturn(lambdaContext0, lambdaContext1);
		target().path("inject-lambda-context-member0").request().get();
		verify(testService).injectedStringArg("0");
		reset(testService);
		target().path("inject-lambda-context-member1").request().get();
		verify(testService).injectedStringArg("1");
	}

	@Path("/")
	@Singleton // singleton in order to test proxies
	public static class TestResource {

		@javax.ws.rs.core.Context
		private Context lambdaContextMember;

		private TestService service;

		@Inject
		public TestResource(TestService service) {
			this.service = service;
		}

		@Path("/inject-lambda-context")
		@GET
		public Response injectLambdaContext(@javax.ws.rs.core.Context Context context) {
			service.injectLambdaContext(context);
			return Response.ok().build();
		}

		@Path("/inject-lambda-context-member0")
		@GET
		public Response injectLambdaContextAsMember0() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
			return Response.ok().build();
		}

		@Path("/inject-lambda-context-member1")
		@GET
		public Response injectLambdaContextAsMember1() {
			service.injectedStringArg(lambdaContextMember.getAwsRequestId());
			return Response.ok().build();
		}
	}

	public static interface TestService {
		void injectLambdaContext(Context context);
		void injectedStringArg(String arg);
	}

	public static interface LambdaContextProvider {
		Context getLambdaContext();
	}

	@Provider
	public static class LambdaContextSetter implements ContainerRequestFilter {

		private final LambdaContextProvider lambdaContextProvider;
		private final InjectionManager injectionManager;

		@Inject
		public LambdaContextSetter(LambdaContextProvider lambdaContextProvider, InjectionManager injectionManager) {
			this.lambdaContextProvider = lambdaContextProvider;
			this.injectionManager = injectionManager;
		}

		@Override
		public void filter(ContainerRequestContext requestContext) throws IOException {
			injectionManager.<Ref<Context>>getInstance(AbstractLambdaContextReferencingBinder.LAMBDA_CONTEXT_TYPE)
					.set(lambdaContextProvider.getLambdaContext());
		}

	}
}