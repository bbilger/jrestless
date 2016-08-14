package com.jrestless.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.jrestless.aws.handler.GatewayRequestObjectHandler;
import com.jrestless.aws.io.GatewayRequest;

/**
 * The request handler as lambda function.
 *
 * @author Bjoern Bilger
 *
 */
public class RequestHandler extends GatewayRequestObjectHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler() {
		// configure the application with the resource
		init(new GatewayResourceConfig().register(SampleResource.class));
		start();
	}

	@Override
	protected void beforeHandleRequest(GatewayRequest request, Context context) {
		LOG.info("start to handle request: " + request);
	}

	@Override
	protected GatewayContainerResponse onRequestSuccess(GatewayContainerResponse response, GatewayRequest request,
			Context context) {
		LOG.info("request handled successfully: " + response);
		return response;
	}

	@Override
	protected GatewayContainerResponse onRequestFailure(Exception e, GatewayRequest request, Context context) {
		LOG.error("request handling failed: ", e);
		return null;
	}
}
