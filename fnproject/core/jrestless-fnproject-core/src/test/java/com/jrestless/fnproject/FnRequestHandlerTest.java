package com.jrestless.fnproject;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fnproject.fn.api.Headers;
import com.fnproject.fn.api.InputEvent;
import com.google.common.collect.ImmutableMap;
import com.jrestless.core.container.JRestlessHandlerContainer;
import com.jrestless.core.container.handler.SimpleRequestHandler;
import com.jrestless.core.container.io.JRestlessContainerRequest;
import com.jrestless.core.container.io.RequestAndBaseUri;

public class FnRequestHandlerTest {
    private String DOMAIN_WITH_SCHEME = "http://www.example.com";
    private JRestlessHandlerContainer<JRestlessContainerRequest> container;
    private FnRequestHandler requestHandler;
    private ByteArrayInputStream defaultBody;


    @SuppressWarnings("unchecked")
	@Before
    public void setUp() {
        container = mock(JRestlessHandlerContainer.class);
        requestHandler = new DefaultFnRequestHandler(container);
        defaultBody = new ByteArrayInputStream(new byte[]{});
    }

    @Test
    public void createContainerRequest_testHttpMethodAndHeaders() {
        Headers headers = Headers.fromMap(ImmutableMap.of("key_one", "value_one", "key_two", "value_two"));
        InputEvent inputEvent = new DefaultInputEvent()
                .setHeaders(headers)
                .getInputEvent();

        FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(inputEvent, defaultBody);
        JRestlessContainerRequest containerRequest = requestHandler.createContainerRequest(wrappedInput);
        assertEquals("GET", containerRequest.getHttpMethod());
        assertEquals(ImmutableMap.of("key-one", singletonList("value_one"), "key-two", singletonList("value_two")), containerRequest.getHeaders());
    }

    @Test
    public void createContainerRequest_NullHeaderValueGiven_ShouldFilterHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("key-one", null);
        headers.put("key-two", "value_two");
        InputEvent inputEvent = new DefaultInputEvent()
                .setHeaders(headers)
                .getInputEvent();

        FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(inputEvent, defaultBody);
        JRestlessContainerRequest containerRequest = requestHandler.createContainerRequest(wrappedInput);
        assertEquals(ImmutableMap.of("key-two", singletonList("value_two")), containerRequest.getHeaders());
    }

    @Test
    public void createContainerRequest_BodyGiven_ShouldUseBody() {
        String content = "42";
        ByteArrayInputStream body = new ByteArrayInputStream(content.getBytes());
        InputEvent inputEvent = new DefaultInputEvent()
                .setBody(body)
                .getInputEvent();

        FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(inputEvent, body);
        JRestlessContainerRequest containerRequest = requestHandler.createContainerRequest(wrappedInput);
        assertEquals(ByteArrayInputStream.class, (containerRequest.getEntityStream()).getClass());
        assertEquals(content, toString((ByteArrayInputStream) (containerRequest.getEntityStream())));
    }

    @Test
    public void createContainerRequest_NoBodyGiven_ShouldUseEmptyBais() {
        InputEvent inputEvent = new DefaultInputEvent()
                .getInputEvent();

        FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(inputEvent, defaultBody);
        JRestlessContainerRequest containerRequest = requestHandler.createContainerRequest(wrappedInput);
        assertEquals(ByteArrayInputStream.class, (containerRequest.getEntityStream()).getClass());
        assertEquals("", toString((ByteArrayInputStream) (containerRequest.getEntityStream())));
    }

    @Test (expected = IllegalStateException.class)
    public void getRequestAndBaseUri_RouteDiffersInURL_ShouldThrowException() {
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME + "/r/route", "/invalidRoute")
                .getInputEvent();

        requestHandler.getRequestAndBaseUri(inputEvent);
    }

    @Test
    public void getRequestAndBaseUri_ShouldGetCorrectBaseAndReqUri() {
        Map<String, List<String>> params = ImmutableMap.of("query", Collections.singletonList("params"));
        InputEvent inputEvent = new DefaultInputEvent()
                .setReqUrlAndRoute(DOMAIN_WITH_SCHEME +"/r/route?query=params","/route")
                .setQueryParameters(params)
                .getInputEvent();

        RequestAndBaseUri requestAndBaseUri = requestHandler.getRequestAndBaseUri(inputEvent);
        assertEquals(URI.create(DOMAIN_WITH_SCHEME +"/r/route?query=params"), requestAndBaseUri.getRequestUri());
        assertEquals(URI.create(DOMAIN_WITH_SCHEME + "/r/"), requestAndBaseUri.getBaseUri());
    }

    @Test
    public void testResponseWriter_WithoutContentHeader_ShouldDefaultToApplicationJson() throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        SimpleRequestHandler.SimpleResponseWriter<FnRequestHandler.WrappedOutput> responseWriter = requestHandler.createResponseWriter(null);
        responseWriter.writeResponse(Response.Status.OK, headers, new ByteArrayOutputStream());
        Assert.assertTrue(responseWriter.getResponse().getOutputEvent().getContentType().get().equals(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testResponseWriter_WithContentHeader_ShouldUseContentHeaderGiven() throws IOException {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList(MediaType.TEXT_HTML));
        SimpleRequestHandler.SimpleResponseWriter<FnRequestHandler.WrappedOutput> responseWriter = requestHandler.createResponseWriter(null);
        responseWriter.writeResponse(Response.Status.OK, headers, new ByteArrayOutputStream());
        Assert.assertTrue(responseWriter.getResponse().getOutputEvent().getContentType().get().equals(MediaType.TEXT_HTML));
    }

    @Test
    public void testOnRequestFailure_ShouldReturnEmptyOutputEvent() {
        FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(
                new DefaultInputEvent().getInputEvent(),
                new ByteArrayInputStream(new byte[]{}));
        RuntimeException exception = new RuntimeException("Testing that onRequestFailure works");

        FnRequestHandler.WrappedOutput output = requestHandler.onRequestFailure(exception, wrappedInput, null);
        assertFalse(output.getOutputEvent().isSuccess());
    }

    private static class DefaultFnRequestHandler extends FnRequestHandler {
        DefaultFnRequestHandler(JRestlessHandlerContainer<JRestlessContainerRequest> container){
            init(container);
            start();
        }
    }

    private static String toString(ByteArrayInputStream bais) {
        int size = bais.available();
        char[] chars = new char[size];
        byte[] bytes = new byte[size];

        bais.read(bytes, 0, size);
        for (int i = 0; i < size;)
            chars[i] = (char) (bytes[i++] & 0xff);

        return new String(chars);
    }
}
