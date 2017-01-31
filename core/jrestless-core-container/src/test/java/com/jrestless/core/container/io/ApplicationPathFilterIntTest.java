package com.jrestless.core.container.io;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.jrestless.core.filter.ApplicationPathFilter;

public class ApplicationPathFilterIntTest extends JerseyTest {

	@Override
	protected Application configure() {
		ResourceConfig application = new ApiResourceConfig();
		application.register(TestResource.class);
		application.register(ApplicationPathFilter.class);
		return application;
	}

	@Test
	public void testUsesConfiguredApplicationPath() {
		assertEquals(200, target("api").path("users").request().get().getStatus());
	}

	@Path("")
	public static class TestResource {
		@Path("users")
		@GET
		public Response injectLambdaContext() {
			return Response.ok().build();
		}
	}

	@ApplicationPath("api")
	private static class ApiResourceConfig extends ResourceConfig {

	}
}
