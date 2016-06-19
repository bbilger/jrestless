package com.jrestless.core.container;

import static com.jrestless.test.MockitoExt.emptyBaos;
import static com.jrestless.test.MockitoExt.eqBaos;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.junit.Before;
import org.junit.Test;

import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.dpi.InstanceBinder;
import com.jrestless.core.container.io.JRestlessRequestContext;
import com.jrestless.core.container.io.JRestlessResponseWriter;
import com.jrestless.test.AbstractTestRequest;

public class JRestlessHandlerContainerInt {

	private ArticleService testService;
	private JRestlessHandlerContainer<JRestlessRequestContext> container;

	@Before
	public void setup() {
		testService = mock(ArticleService.class);
		Binder binder = new InstanceBinder.Builder().addInstance(testService, ArticleService.class).build();

		container = new JRestlessHandlerContainer<JRestlessRequestContext>(
				new ResourceConfig().register(TestResource.class).register(binder).register(RolesAllowedDynamicFeature.class));
		container.onStartup();
	}

	@Test
	public void getArticle_NonExistingIdGiven_ShouldReturnNotFound() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		container.handleRequest(new TestRequest("/articles/1", "GET"), responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.NOT_FOUND), any(), emptyBaos());
	}

	@Test
	public void getArticle_ExistingIdGiven_ShouldReturnArticle() throws IOException {
		when(testService.getArticle(1)).thenReturn("some article");
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		container.handleRequest(new TestRequest("/articles/1", "GET"), responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("some article"));
	}

	@Test
	public void getArticle_ServiceExceptionGiven_ShouldReturnInternalServerError() throws IOException {
		when(testService.getArticle(1)).thenThrow(new RuntimeException());
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		container.handleRequest(new TestRequest("/articles/1", "GET"), responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.INTERNAL_SERVER_ERROR), any(), emptyBaos());
	}

	@Test
	public void getArticle_NonSupportedMethodGiven_ShouldReturnInternalServerError() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		container.handleRequest(new TestRequest("/articles/1", "PATCH"), responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.METHOD_NOT_ALLOWED), any(), emptyBaos());
	}

	@Test
	public void getArticle_NonSupportedAccept_ShouldReturnNotAcceptable() throws IOException {
		when(testService.getArticle(1)).thenReturn("some article");
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/articles/1", "GET");
		req.getHeadersAsMultimap().add("Accept", MediaType.APPLICATION_XML);
		container.handleRequest(req, responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.NOT_ACCEPTABLE), any(), emptyBaos());
	}

	@Test
	public void getArticle_OneSupportedAcceptGiven_ShouldReturnArticle() throws IOException {
		when(testService.getArticle(1)).thenReturn("some article");
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/articles/1", "GET");
		req.getHeadersAsMultimap().add("Accept", MediaType.APPLICATION_XML + "," + MediaType.TEXT_PLAIN);
		container.handleRequest(req, responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("some article"));
	}

	@Test
	public void updateArticle_ArticleBodyGiven_ShouldReturnUpdatedArticle() throws IOException {
		when(testService.updateArticle(1, "some updated article text")).thenReturn("some updated persisted article text");
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		container.handleRequest(new TestRequest("/articles/1", "PUT", new ByteArrayInputStream("some updated article text".getBytes())), responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("some updated persisted article text"));
	}

	@Test
	public void updateArticle_ArticleBodyWithNonSupportedContentTypeGiven_ShouldReturnUpdatedArticle() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/articles/1", "PUT", new ByteArrayInputStream("{\"text\": \"some updated article text\"}".getBytes()));
		req.getHeadersAsMultimap().add("Content-Type",  MediaType.APPLICATION_JSON);
		container.handleRequest(req, responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.UNSUPPORTED_MEDIA_TYPE), any(), emptyBaos());
	}

	@Test
	public void getArticles_ContainsGiven_ShouldReturnArticles() throws IOException {
		when(testService.getArticles("test")).thenReturn(Arrays.asList("test article 1", "test article 2"));
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/articles?contains=test", "GET");
		container.handleRequest(req, responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("test article 1, test article 2"));
	}

	@Test
	public void getPrincipalName_PrincipalNameGiven_ShouldReturnPrincipalName() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/info", "GET");
		SecurityContext sc = mock(SecurityContext.class);
		Principal p = mock(Principal.class);
		when(p.getName()).thenReturn("someName");
		when(sc.getUserPrincipal()).thenReturn(p);
		container.handleRequest(req, responseWriter, sc);
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("someName"));
	}

	@Test
	public void getHeaderParam_HeaderParamNameGiven_ShouldReturnHeaderParam() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		TestRequest req = new TestRequest("/header", "GET");
		req.getHeadersAsMultimap().add("key", "value");
		container.handleRequest(req, responseWriter, mock(SecurityContext.class));
		verify(responseWriter, times(1)).writeResponse(eq(Status.OK), any(), eqBaos("value"));
	}

	@Test
	public void deleteApp_NoAdminUserGiven_ShouldForbidAccess() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		SecurityContext sc = mock(SecurityContext.class);
		Principal p = mock(Principal.class);
		when(sc.getUserPrincipal()).thenReturn(p);
		when(sc.isUserInRole("admin")).thenReturn(false);
		container.handleRequest(new TestRequest("/app", "DELETE"), responseWriter, sc);
		verify(responseWriter, times(1)).writeResponse(eq(Status.FORBIDDEN), any(), emptyBaos());
	}

	@Test
	public void deleteApp_AdminUserGiven_ShouldAllowAccess() throws IOException {
		JRestlessResponseWriter responseWriter = createResponseWriterMock();
		SecurityContext sc = mock(SecurityContext.class);
		Principal p = mock(Principal.class);
		when(sc.getUserPrincipal()).thenReturn(p);
		when(sc.isUserInRole("admin")).thenReturn(true);
		container.handleRequest(new TestRequest("/app", "DELETE"), responseWriter, sc);
		verify(responseWriter, times(1)).writeResponse(eq(Status.NO_CONTENT), any(), emptyBaos());
	}

	protected JRestlessResponseWriter createResponseWriterMock() {
		JRestlessResponseWriter responseWriter = mock(JRestlessResponseWriter.class);
		when(responseWriter.getEntityOutputStream()).thenReturn(new ByteArrayOutputStream());
		return responseWriter;
	}

	@Path("/")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll
	public static class TestResource {

		private ArticleService testService;

		@Inject
		public TestResource(ArticleService testService) {
			this.testService = testService;
		}

		@GET
		@Path("/articles")
		public Response getArticles(@QueryParam("contains") String contains) {
			List<String> articles = testService.getArticles(contains);
			if (articles.size() > 0) {
				return Response.ok(String.join(", ", articles)).build();
			} else {
				return Response.status(Status.NOT_FOUND).build();
			}
		}

		@GET
		@Path("/articles/{articleId}")
		public Response getArticle(@PathParam("articleId") int articleId) {
			String article = testService.getArticle(articleId);
			if (article == null) {
				return Response.status(Status.NOT_FOUND).build();
			} else {
				return Response.ok(article).build();
			}
		}

		@PUT
		@Path("/articles/{articleId}")
		public Response updateArticle(@PathParam("articleId") int articleId, String article) {
			String updatedArticle = testService.updateArticle(articleId, article);
			if (article == null) {
				return Response.status(Status.NOT_FOUND).build();
			} else {
				return Response.ok(updatedArticle).build();
			}
		}

		@DELETE
		@Path("/app")
		@RolesAllowed({ "admin" })
		public Response deleteApp() {
			return Response.noContent().build();
		}

		@GET
		@Path("/info")
		public Response getPrincipalName(@Context SecurityContext securityContext) {
			return Response.ok(securityContext.getUserPrincipal().getName()).build();
		}

		@GET
		@Path("/header")
		public Response getHeaderParam(@HeaderParam("key") String param) {
			return Response.ok(param).build();
		}
	}

	private static interface ArticleService {
		List<String> getArticles(String contains);
		String getArticle(int articleId);
		String updateArticle(int articleId, String article);
	}

	public static class TestRequest extends AbstractTestRequest {
		private final InputStream entityStream;

		public TestRequest(String requestUri, String httpMethod) {
			this(requestUri, httpMethod, new ByteArrayInputStream(new byte[0]));
		}

		public TestRequest(String requestUri, String httpMethod, InputStream entityStream) {
			this("/", requestUri, httpMethod, entityStream, new MultivaluedHashMap<>());

		}

		public TestRequest(String baseUri, String requestUri, String httpMethod, InputStream entityStream, MultivaluedMap<String, String> headers) {
			this(URI.create(baseUri), URI.create(requestUri), httpMethod, entityStream, headers);

		}

		public TestRequest(URI baseUri, URI requestUri, String httpMethod, InputStream entityStream, MultivaluedMap<String, String>  headers) {
			super(baseUri, requestUri, httpMethod, headers);
			this.entityStream = entityStream;
		}

		@Override
		public InputStream getEntityStream() {
			return entityStream;
		}

		public MultivaluedMap<String, String> getHeadersAsMultimap() {
			return (MultivaluedMap<String, String>) getHeaders();
		}
	}

}
