package com.jrestless.aws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

	@GET
	@Path("/staticheader")
	@ApiOperation(value = "get entity by id", response = VO.class, responseHeaders = {
		@ResponseHeader(name = "X-Powered-By", description = "'JRestless'", response = String.class)
	})
	public Response getStaticHeader() {
		return Response.ok(wrapValue("something")).build();
	}


	@GET
	@Path("/dynamicheader")
	@ApiOperation(value = "response with some dynamic header", response = VO.class, responseHeaders = {
		@ResponseHeader(name = "Set-Cookie", response = String.class)
	})
	public Response getDynamicHeader() {
		return Response.ok(wrapValue("something")).cookie(new NewCookie("foo", "bar")).build();
	}

//	@GET
//	@Path("/moved")
//	@ApiOperation(value = "moved response", responseHeaders = {
//		@ResponseHeader(name = "Location", response = String.class)
//	})
//	@StatusCodes(defaultCode = MOVED_STATUS_CODE)
//	public Response getNonDefaultHeader() {
//		return Response.status(Status.MOVED_PERMANENTLY).header("Location", "/newlocation").build();
//	}

	@GET
	@Path("/pathparam")
	@ApiOperation(value = "get path param", response = VO.class)
	public Response getSomePathParam(@PathParam("pathParam") String pathParam) {
		return Response.ok(wrapValue(pathParam)).build();
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
}
