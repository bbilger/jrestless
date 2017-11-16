package com.jrestless.fnproject;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fnproject.fn.api.InputEvent;
import com.fnproject.fn.api.RuntimeContext;
import com.google.common.collect.ImmutableMap;
import com.jrestless.core.container.dpi.InstanceBinder;
import com.jrestless.core.filter.ApplicationPathFilter;

public class FnRequestHandlerIntTest {
    private String DOMAIN_WITH_SCHEME = "http://www.example.com";
    private FnRequestHandler handler;
    private TestService testService;
    private RuntimeContext runtimeContext = mock(RuntimeContext.class);

    @Before
    public void setUp() {
        testService = mock(TestService.class);
        handler = createAndStartHandler(new ResourceConfig(), testService);
    }

    private FnRequestHandler createAndStartHandler(ResourceConfig config, TestService testService) {
        Binder binder = new InstanceBinder.Builder().addInstance(testService, TestService.class).build();
        config.register(binder);
        config.register(TestResource.class);
        config.register(ApplicationPathFilter.class);
        FnRequestHandler handler = new FnRequestHandler(){};
        handler.init(config);
        handler.start();
        handler.setRuntimeContext(runtimeContext);
        return handler;
    }

    @Test
    public void testRuntimeContextInjection() {
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/", "/")
                .setMethod("DELETE")
                .getInputEvent();

        handler.handleRequest(inputEvent);
        verify(testService).injectRuntimeContext(runtimeContext);
    }

    @Test
    public void testInputEventInjection() {
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/inject-input-event", "/inject-input-event")
                .setMethod("PUT")
                .getInputEvent();

        handler.handleRequest(inputEvent);
        verify(testService).injectInputEvent(same(inputEvent));
    }

    @Test
    public void testBaseUriWithoutHost() {
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/uris", "/uris")
                .setMethod("GET")
                .getInputEvent();

        handler.handleRequest(inputEvent);
        verify(testService).baseUri(URI.create(DOMAIN_WITH_SCHEME + "/"));
        verify(testService).requestUri(URI.create(DOMAIN_WITH_SCHEME + "/uris"));
    }

    @Test
    public void testBaseUriWithHost() {
        Map<String, String> inputHeaders = ImmutableMap.of(HttpHeaders.HOST, "www.example.com");
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/uris", "/uris")
                .setMethod("GET")
                .setHeaders(inputHeaders)
                .getInputEvent();

        handler.handleRequest(inputEvent);
        verify(testService).baseUri(URI.create(DOMAIN_WITH_SCHEME + "/"));
        verify(testService).requestUri(URI.create(DOMAIN_WITH_SCHEME + "/uris"));
    }

    @Test
    public void testAppPathWithoutHost() {
        FnRequestHandler handlerWithAppPath = createAndStartHandler(new ApiResourceConfig(), testService);
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/api/uris", "/api/uris")
                .setMethod("GET")
                .getInputEvent();

        handlerWithAppPath.handleRequest(inputEvent);
        verify(testService).baseUri(URI.create(DOMAIN_WITH_SCHEME + "/api/"));
        verify(testService).requestUri(URI.create(DOMAIN_WITH_SCHEME + "/api/uris"));
    }

    @Test
    public void testAppPathWithHost() {
        Map<String, String> inputHeaders = ImmutableMap.of(HttpHeaders.HOST, "www.example.com");
        FnRequestHandler handlerWithAppPath = createAndStartHandler(new ApiResourceConfig(), testService);
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/api/uris", "/api/uris")
                .setMethod("GET")
                .setHeaders(inputHeaders)
                .getInputEvent();

        handlerWithAppPath.handleRequest(inputEvent);
        verify(testService).baseUri(URI.create(DOMAIN_WITH_SCHEME + "/api/"));
        verify(testService).requestUri(URI.create(DOMAIN_WITH_SCHEME + "/api/uris"));
    }

    private interface TestService{
        void injectRuntimeContext(RuntimeContext context);
        void injectInputEvent(InputEvent request);
        void baseUri(URI baseUri);
        void requestUri(URI baseUri);
    }

    @Path("/")
    @Singleton // singleton in order to test proxies
    public static class TestResource {

        private final TestService service;
        private final UriInfo uriInfo;

        @Inject
        public TestResource(TestService service, UriInfo uriInfo) {
            this.service = service;
            this.uriInfo = uriInfo;
        }

        @DELETE
        public Response injectRuntimeContext(@javax.ws.rs.core.Context RuntimeContext runtimeContext) {
            service.injectRuntimeContext(runtimeContext);
            return Response.ok().build();
        }

        @Path("/inject-input-event")
        @PUT
        public Response injectInputEvent(@javax.ws.rs.core.Context InputEvent inputEvent) {
            service.injectInputEvent(inputEvent);
            return Response.ok().build();
        }

        @Path("/round-trip")
        @POST
        public Response putSomething(AnObject entity) {
            return Response.ok(entity).build();
        }

        @Path("uris")
        @GET
        public void getBaseUri() {
            service.baseUri(uriInfo.getBaseUri());
            service.requestUri(uriInfo.getRequestUri());
        }
    }

    static class AnObject {
        private String value;

        @JsonCreator
        AnObject(@JsonProperty("value") String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @ApplicationPath("api")
    public static class ApiResourceConfig extends ResourceConfig {
    }


}



