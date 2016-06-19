package com.jrestless.core.container;

import static com.jrestless.test.MockitoExt.eqBaos;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;

import com.jrestless.core.container.JRestlessHandlerContainer.JRestlessContainerResponse;
import com.jrestless.core.container.io.JRestlessResponseWriter;

public class JRestlessContainerResponseTest {

	private JRestlessResponseWriter responseWriter;

	private JRestlessContainerResponse containerResponse;

	@Before
	public void setup() {
		responseWriter = mock(JRestlessResponseWriter.class);
		when(responseWriter.getEntityOutputStream()).thenReturn(new ByteArrayOutputStream());
		containerResponse = new JRestlessContainerResponse(responseWriter);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void close_ResponseNotYetClosed_ShouldWriteResponse() throws IOException {
		containerResponse.close();
		verify(responseWriter, times(1)).writeResponse(isNotNull(Status.class), isNotNull(MultivaluedMap.class),
				isNotNull(ByteArrayOutputStream.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void close_StatusTypeSet_ShouldWriteResponseUsingSetStatus() throws IOException {
		containerResponse.setStatusType(Status.CONFLICT);
		containerResponse.close();
		verify(responseWriter, times(1)).writeResponse(eq(Status.CONFLICT), isNotNull(MultivaluedMap.class),
				isNotNull(ByteArrayOutputStream.class));
	}

	@Test
	public void close_HeadersSet_ShouldWriteResponseUsingSetHeaders() throws IOException {
		Map<String, List<String>> headers = new MultivaluedHashMap<>();
		headers.put("header0", asList("value0_0", "value0_1"));
		headers.put("header1", asList("value1_0"));
		containerResponse.getHeaders().putAll(headers);
		containerResponse.close();
		verify(responseWriter, times(1)).writeResponse(isNotNull(Status.class), eq(headers),
				isNotNull(ByteArrayOutputStream.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void close_OutputStreamUpdated_ShouldWriteOutputStream() throws IOException {
		containerResponse.getEntityOutputStream().write("response".getBytes());
		containerResponse.close();
		verify(responseWriter, times(1)).writeResponse(isNotNull(Status.class), isNotNull(MultivaluedMap.class),
				eqBaos("response"));
	}

	@Test
	public void close_ResponseAlreadyClosed_ShouldNotWriteResponse() {
		containerResponse.close();
		reset(responseWriter);
		containerResponse.close();
		verifyZeroInteractions(responseWriter);
	}

	@SafeVarargs
	private static <T> List<T> asList(T... values) {
		List<T> list = new ArrayList<>();
		for (T value : values) {
			list.add(value);
		}
		return list;
	}
}
