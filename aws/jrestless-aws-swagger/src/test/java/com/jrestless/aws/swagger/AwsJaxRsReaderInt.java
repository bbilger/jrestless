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
package com.jrestless.aws.swagger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.json.JSONException;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.google.common.io.Files;
import com.jrestless.aws.annotation.Cors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.util.Json;

public class AwsJaxRsReaderInt {

	private Swagger swagger = new Swagger();
	private LogAdapter logAdapter = Mockito.mock(LogAdapter.class);

	@Test(expected = RuntimeException.class)
	public void init_NoConfigGiven_ShouldThrowException() {
		createReader(null);
	}

	@Test(expected = RuntimeException.class)
	public void init_ConfigNotExistsGiven_ShouldThrowException() {
		createReader("minimal1.json");
	}

	@Test(expected = RuntimeException.class)
	public void init_ConfigIsNoFileGiven_ShouldThrowException() {
		createReader("");
	}

	@Test(expected = RuntimeException.class)
	public void init_InvalidConfigGiven_ShouldThrowException() {
		createReader("invalid.json");
	}

	@Test
	public void testMinimalResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/minimalResourceWithDefaultConfig.json", reader.read(Collections.singleton(MinimalResource.class)));
	}

	@Test
	public void testMinimalResourceWithCorsTurnedOff() {
		AwsJaxRsReader reader = createReader("corsOff.json");
		assertSwagger("/swagger/jaxrs/minimalResourceWithCorsTurnedOff.json", reader.read(Collections.singleton(MinimalResource.class)));
	}

	@Test
	public void testMinimalResourceWithCustomCorsHeaders() {
		AwsJaxRsReader reader = createReader("customCorsHeaders.json");
		assertSwagger("/swagger/jaxrs/minimalResourceWithCustomCorsHeaders.json",
				reader.read(Collections.singleton(MinimalResource.class)));
	}

	@Test
	public void testSimpleCrudResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/simpleCrudResourceWithDefaultConfig.json", reader.read(Collections.singleton(SimpleCrudResource.class)));
	}

	@Test
	public void testCustomCorsResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/customCorsResource.json", reader.read(Collections.singleton(CustomCorsResource.class)));
	}

	@Test
	public void testCustomCorsResourceWithCorsTurnedOff() {
		/*
		 * turning CORS off in the configuration may not have
		 * any impact if it is controlled completely via annotations
		 */
		AwsJaxRsReader reader = createReader("corsOff.json");
		assertSwagger("/swagger/jaxrs/customCorsResource.json", reader.read(Collections.singleton(CustomCorsResource.class)));
	}

	@Test
	public void testCustomDefaultStatusCodeResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/customDefaultStatusCodeResourceWithDefaultConfig.json",
				reader.read(Collections.singleton(CustomDefaultStatusCodeResource.class)));
	}

	@Test
	public void testCustomAdditionalStatusCodesResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/customAdditionalStatusCodesResourceWithDefaultConfig.json",
				reader.read(Collections.singleton(CustomAdditionalStatusCodesResource.class)));
	}

	@Test
	public void testCustomStatusCodesResourceWithDefaultConfig() {
		AwsJaxRsReader reader = createReader("minimal.json");
		assertSwagger("/swagger/jaxrs/customStatusCodesResourceWithDefaultConfig.json",
				reader.read(Collections.singleton(CustomStatusCodesResource.class)));
	}

	@Test
	public void testSecuredResourceWithIamAuth() {
		AwsJaxRsReader reader = createReader("authTypeIam.json");
		assertSwagger("/swagger/jaxrs/securedResourceWithIamAuth.json",
				reader.read(Collections.singleton(SecuredResource.class)));
	}

	@Test
	public void testSecuredResourceWithAuthorizerAuth() {
		AwsJaxRsReader reader = createReader("authTypeAuthorizer.json");
		assertSwagger("/swagger/jaxrs/securedResourceWithAuthorizerAuth.json",
				reader.read(Collections.singleton(SecuredResource.class)));
	}

	@Test
	public void testSecuredResourceWithNoneAuth() {
		AwsJaxRsReader reader = createReader("authTypeNone.json");
		assertSwagger("/swagger/jaxrs/securedResourceWithNoneAuth.json",
				reader.read(Collections.singleton(SecuredResource.class)));
	}

	private AwsJaxRsReader createReader(String configName) {
		String configAbs = null;
		if (configName != null) {
			configAbs = getAbsResourcePath("/config/minimal.json").replaceAll("minimal.json$", configName);
			System.setProperty("aws-swagger-configuration", configAbs);
		} else {
			System.clearProperty("aws-swagger-configuration");
		}
		return new AwsJaxRsReader(swagger, logAdapter);
	}

	private void assertSwagger(String expectedSwaggerPath, Swagger actual) {
		try {
			String actualJson = Json.mapper().writeValueAsString(actual);
			try(  PrintWriter out = new PrintWriter( "/home/REMOVED/tmp/swagger.json" )  ){
			    out.println( actualJson );
			}
			String expectedJson = Files.toString(getResourceFile(expectedSwaggerPath), StandardCharsets.UTF_8);
			JSONAssert.assertEquals(expectedJson, actualJson, true);
		} catch (IOException | JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private File getResourceFile(String resource) {
		URL url = AwsJaxRsReaderInt.class.getResource(resource);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private String getAbsResourcePath(String resource) {
		return getResourceFile(resource).getAbsolutePath();
	}

	@Api
	@Path("/")
	public static class MinimalResource {
		@GET
		@Path("/blub")
		@ApiOperation(value = "blub", code = 301, response = String.class)
		public Response corsoff() {
			return null;
		}
	}

	@Api
	@Path("/")
	@Cors(enabled = false)
	public static class CustomCorsResource {
		@GET
		@Path("/corsoff")
		@ApiOperation(value = "corsoff", response = String.class)
		public Response corsoff() {
			return null;
		}
		@GET
		@Path("/corson")
		@Cors(enabled = true)
		@ApiOperation(value = "corson", response = String.class)
		public Response corson() {
			return null;
		}
	}

	@Api
	@Path("/")
	@Cors(enabled = false)
	public static class CustomDefaultStatusCodeResource {
		@GET
		@Path("/302")
		@ApiOperation(value = "default status code 302", code = 302, response = String.class)
		public Response default302() {
			return null;
		}

		@GET
		@Path("/204")
		@ApiOperation(value = "default status code 204", code = 204, response = String.class)
		public Response default204() {
			return null;
		}
	}

	@Api
	@Path("/")
	@Cors(enabled = false)
	public static class CustomAdditionalStatusCodesResource {
		@GET
		@Path("/500")
		@ApiOperation(value = "additional status code 500 (default 200)", response = String.class)
		@ApiResponses(@ApiResponse(message = "", code = 500))
		public Response additional500() {
			return null;
		}

		@GET
		@Path("/204")
		@ApiOperation(value = "additional status code 400, 404 (default 200)", response = String.class)
		@ApiResponses({
			@ApiResponse(message = "", code = 400),
			@ApiResponse(message = "", code = 404)
		})
		public Response additional400404() {
			return null;
		}
	}

	@Api
	@Path("/")
	@Cors(enabled = false)
	public static class CustomStatusCodesResource {
		@GET
		@Path("/204-500")
		@ApiOperation(value = "default 204, additional = {500})", code = 204, response = String.class)
		@ApiResponses(@ApiResponse(message = "", code = 500))
		public Response default204additional500() {
			return null;
		}

		@GET
		@Path("/200-400-404")
		@ApiOperation(value = "default 200, additional = {400, 404})", response = String.class)
		@ApiResponses({
			@ApiResponse(message = "", code = 400),
			@ApiResponse(message = "", code = 404)
		})
		public Response default200additional400404() {
			return null;
		}
	}

	@Api
	@Path("/")
	@DenyAll
	@Cors(enabled = false)
	public static class SecuredResource {
		@GET
		@Path("/permitAll")
		@PermitAll
		@ApiOperation(value = "permit all", code = 200, response = String.class)
		@ApiResponses({ @ApiResponse(message = "123", code = 500) })
		public Response permitAll() {
			return null;
		}

		@GET
		@Path("/rolesAllowed")
		@RolesAllowed({ "user, admin" })
		@ApiOperation(value = "roles allowed", code = 200, response = String.class)
		@ApiResponses(@ApiResponse(message = "", code = 500))
		public Response rolesAllowed() {
			return null;
		}

		@GET
		@Path("/swaggerRolesAllowed")
		@RolesAllowed({ "user, admin" })
		@ApiOperation(value = "swagger roles allowed", code = 200, response = String.class, authorizations = {
			@Authorization(value = "swagger-authorizer")
		})
		@ApiResponses(@ApiResponse(message = "", code = 500))
		public Response swaggerRolesAllowed() {
			return null;
		}
	}

	@Api
	@Path("/base")
	public static class SimpleCrudResource {
		@GET
		@Path("/entity0")
		@ApiOperation(value = "get entity", response = Entity0.class)
		public Response getEntity() {
			return null;
		}
		@DELETE
		@Path("/entity0")
		@ApiOperation(value = "delete entity")
		public Response deleteEntity() {
			return null;
		}
		@POST
		@Path("/entity0")
		@ApiOperation(value = "create entity", response = Entity0.class)
		public Response createEntity() {
			return null;
		}
		@PUT
		@Path("/entity0")
		@ApiOperation(value = "update entity", response = Entity0.class)
		public Response updateEntity() {
			return null;
		}
	}

	public static class Entity0 {
		private String prop;

		public Entity0(String prop) {
			super();
			this.prop = prop;
		}

		public String getProp() {
			return prop;
		}
	}
}
