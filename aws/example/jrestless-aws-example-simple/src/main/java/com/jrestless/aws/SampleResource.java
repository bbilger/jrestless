package com.jrestless.aws;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 * A sample resource.
 *
 * @author Bjoern Bilger
 *
 */
@Path("/")
@Api
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON })
public class SampleResource {

	private static final int MOVED_STATUS_CODE = 301;
	private static final int BAD_REQUEST_STATUS_CODE = 400;

	@GET
	@Path("/info_json")
	@ApiOperation(value = "get application info as JSON", response = VO.class)
	public Response getInfoAsJson() {
		return getInfo();
	}

	@GET
	@Path("/info_xml")
	@Produces(MediaType.APPLICATION_XML)
	@ApiOperation(value = "get application info", response = VO.class)
	public Response getInfoAsXml() {
		return getInfo();
	}

	private Response getInfo() {
		return Response.ok(wrapValue("up\nand\nrunning")).build();
	}

	@GET
	@Path("/secured")
	@ApiOperation(value = "some secured endpoint", response = VO.class)
//	@RolesAllowed("user")
	public Response getSecured() {
		return Response.ok(wrapValue("secured")).build();
	}

	/*
	 * Dynamic headers can be written for default as well as non-default responses.
	 * They, however, must be mentioned on each response.
	 * So, "/staticheader?status=OK" and "/staticheader?status=BAD" will output the header, but
	 * "/staticheader?status=EMPTY" will not.
	 */
	@GET
	@Path("/staticheader")
	@ApiOperation(value = "get entity by id", response = VO.class, responseHeaders = {
		@ResponseHeader(name = "X-Powered-By", description = "'JRestless - 200'", response = String.class)
	})
	@ApiResponses(@ApiResponse(code = BAD_REQUEST_STATUS_CODE, message = "", responseHeaders = {
		@ResponseHeader(name = "X-Powered-By", description = "'JRestless - 400'", response = String.class)
	}))
	public Response getStaticHeader(@DefaultValue("OK") @QueryParam("status") ResponseStatus status) {
		return Response.status(status.getCode()).entity(wrapValue("something")).build();
	}

	/*
	 * Dynamic headers can't be written for non-default responses at the moment.
	 * So when you request "/dynamicheader?bad=false", you will see the response
	 * header "Set-Cookie" but you will NOT see it if you request
	 * "/dynamicheader?bad=true".
	 */
	@GET
	@Path("/dynamicheader")
	@ApiOperation(value = "response with some dynamic header", response = VO.class, responseHeaders = {
		@ResponseHeader(name = "Set-Cookie", response = String.class)
	})
	@ApiResponses(@ApiResponse(code = BAD_REQUEST_STATUS_CODE, message = "", responseHeaders = {
		@ResponseHeader(name = "Set-Cookie", response = String.class)
	}))
	public Response getDynamicHeader(@DefaultValue("false") @QueryParam("bad") boolean bad) {
		int statusCode = bad ? Status.BAD_REQUEST.getStatusCode() : Status.OK.getStatusCode();
		return Response.status(statusCode).entity(wrapValue("something")).cookie(new NewCookie("foo", "bar")).build();
	}

	@GET
	@Path("/moved")
	@ApiOperation(code = MOVED_STATUS_CODE, value = "moved response", response = VO.class, responseHeaders = {
		@ResponseHeader(name = "Location", response = VO.class)
	})
	public Response getNonDefaultHeader() {
		return Response.status(Status.MOVED_PERMANENTLY).header("Location", "/newlocation").build();
	}

	@GET
	@Path("/pathparam/{value}")
	@ApiOperation(value = "get path param", response = VO.class)
	public Response getSomePathParam(@PathParam("value") String value) {
		return Response.ok(wrapValue(value)).build();
	}

	@GET
	@Path("/queryparam")
	@ApiOperation(value = "get query param", response = VO.class)
	public Response getQueryParam(@QueryParam("value") String value) {
		return Response.ok(wrapValue(value)).build();
	}

	@GET
	@Path("/badrequest")
	@ApiOperation(value = "bad request", response = VO.class)
	public Response getBadRequest() {
		return Response.status(Status.BAD_REQUEST).entity(wrapValue("badrequest")).build();
	}

	@GET
	@Path("/awscontext")
	@ApiOperation(value = "reflect request context", response = GatewayRequestContext.class)
	public Response reflectGatewayContext(@Context GatewayRequestContext gatewayContext) {
		return Response.ok(gatewayContext).build();
	}

	private static <T> GenericEntity<VO<T>> wrapValue(T value) {
		return new GenericEntity<VO<T>>(new VO<>(value)) { };
	}

	public enum ResponseStatus {
		OK(200),
		EMPTY(204),
		BAD(400);
		private int code;
		ResponseStatus(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
	}
}
