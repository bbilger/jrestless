package com.jrestless.core.filter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;

public class ApplicationPathFilterTest {

	private static final URI EXISTING_BASE_URI = URI.create("existing-base-uri/");
	private static final URI TEST_UPDATED_BASE_URI = URI.create("existing-base-uri/test/");
	private static final URI REQUEST_URI = URI.create("request-uri");

	@Test
	public void filter_NoAppPathGiven_ShouldAccessRequestContext() throws IOException {
		createApplicationPathFilter(new ApplicationWithoutPath()).filter(null);
	}

	@Test
	public void filter_AppPathGiven_ShouldAccessRequestContext() throws IOException {
		assertThrows(NullPointerException.class, () -> createApplicationPathFilter(new ApplicationPathTest()).filter(null));
	}

	@Test
	public void filter_EmptyAppPathGiven_ShouldNotUpdateBaseUri() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPathEmpty()).filter(reqContext);
		verify(reqContext, never()).setRequestUri(any(), any());
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_NoAppPathGiven_ShouldNotUpdateBaseUri() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationWithoutPath()).filter(reqContext);
		verify(reqContext, never()).setRequestUri(any(), any());
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_NoAppPathRootGiven_ShouldNotUpdateBaseUri() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPathRoot()).filter(reqContext);
		verify(reqContext, never()).setRequestUri(any(), any());
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_NullAppConfigGiven_ShouldNotUpdateBaseUri() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(null).filter(reqContext);
		verify(reqContext, never()).setRequestUri(any(), any());
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_AppPathGiven_ShouldUpdateBaseUri() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPathTest()).filter(reqContext);
		verify(reqContext, times(1)).setRequestUri(TEST_UPDATED_BASE_URI, REQUEST_URI);
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_AppPathWithSlashGiven_ShouldUpdateBaseUriWithoutSlashes() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPathTestWithLeadingAndTrailingSlashes()).filter(reqContext);
		verify(reqContext, times(1)).setRequestUri(TEST_UPDATED_BASE_URI, REQUEST_URI);
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_AppPathWithSlashAsteriskGiven_ShouldUpdateBaseUriWithoutSlashesAsterisk() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPathTestSlashAsterisk()).filter(reqContext);
		verify(reqContext, times(1)).setRequestUri(TEST_UPDATED_BASE_URI, REQUEST_URI);
		verify(reqContext, never()).setRequestUri(any());
	}

	@Test
	public void filter_AppPathWithMultiplePathsGiven_ShouldUpdateBaseUriWithAllPaths() throws IOException {
		ContainerRequestContext reqContext = mockRequestContext(EXISTING_BASE_URI, REQUEST_URI);
		createApplicationPathFilter(new ApplicationPath1AndPath2()).filter(reqContext);
		verify(reqContext, times(1)).setRequestUri(URI.create("existing-base-uri/path1/path2/"), REQUEST_URI);
		verify(reqContext, never()).setRequestUri(any());
	}

	private static ContainerRequestContext mockRequestContext(URI baseUri, URI requestUri) {
		UriInfo uriInfo = mock(UriInfo.class);
		when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri(baseUri));
		when(uriInfo.getRequestUri()).thenReturn(requestUri);

		ContainerRequestContext reqContext = mock(ContainerRequestContext.class);
		when(reqContext.getUriInfo()).thenReturn(uriInfo);

		return reqContext;
	}

	private static ContainerRequestFilter createApplicationPathFilter(Application app) {
		ApplicationPathFilter filter = new ApplicationPathFilter();
		filter.setApplication(app);
		return filter;
	}

	private static class ApplicationWithoutPath extends Application {
	}

	@ApplicationPath("/")
	private static class ApplicationPathRoot extends Application {
	}

	@ApplicationPath("test")
	private static class ApplicationPathTest extends Application {
	}

	@ApplicationPath("///test//")
	private static class ApplicationPathTestWithLeadingAndTrailingSlashes extends Application {
	}

	@ApplicationPath("test/*")
	private static class ApplicationPathTestSlashAsterisk extends Application {
	}

	@ApplicationPath("  ")
	private static class ApplicationPathEmpty extends Application {
	}

	@ApplicationPath("///path1/path2//")
	private static class ApplicationPath1AndPath2 extends Application {
	}
}
