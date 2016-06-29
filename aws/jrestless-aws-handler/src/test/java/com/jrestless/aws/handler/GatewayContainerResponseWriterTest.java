/*
 * Copyright 2016 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.aws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponse;
import com.jrestless.aws.handler.GatewayRequestHandler.GatewayContainerResponseWriter;

public class GatewayContainerResponseWriterTest {
	@Test
	public void writeResponse_ResponseParamsGiven_ShouldCreateNewGatewayContainerResponse() throws IOException {
		GatewayContainerResponseWriter writer = new GatewayContainerResponseWriter();
		assertNull(writer.getResponse());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write("testBody".getBytes());
		Map<String, List<String>> headers = ImmutableMap.of("k", ImmutableList.of("v1", "v2"));
		StatusType status = Status.EXPECTATION_FAILED;
		writer.writeResponse(status, headers, baos);
		GatewayContainerResponse actualResponse1 = writer.getResponse();
		writer.writeResponse(status, headers, baos);
		GatewayContainerResponse actualResponse2 = writer.getResponse();

		GatewayContainerResponse expectedResponse = new GatewayContainerResponse(status, "testBody", headers);
		assertEquals(expectedResponse.getHeaders(), actualResponse1.getHeaders());
		assertEquals(expectedResponse.getBody(), actualResponse1.getBody());
		assertEquals(expectedResponse.getStatusType(), actualResponse1.getStatusType());
		assertNotSame(actualResponse1, actualResponse2);
	}

	@Test
	public void writeResponse_NullHeadersGiven_ShouldBeTreatedAsEmpty() throws IOException {
		GatewayContainerResponseWriter writer = new GatewayContainerResponseWriter();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write("testBody".getBytes());
		writer.writeResponse(Status.OK, null, baos);
		assertEquals(ImmutableMap.of(), writer.getResponse().getHeaders());
	}

	@Test
	public void getEntityOutputStream_ShouldCreateNewByteArrayOutputStream() {
		GatewayContainerResponseWriter writer = new GatewayContainerResponseWriter();
		ByteArrayOutputStream baos1 = writer.getEntityOutputStream();
		ByteArrayOutputStream baos2 = writer.getEntityOutputStream();
		assertNotNull(baos1);
		assertNotNull(baos2);
		assertNotSame(baos1, baos2);
	}
}
