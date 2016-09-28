# JRestless

JRestless allows you to create serverless applications using JAX-RS.

![](jrestless_512_256.png)

[![Build Status](https://img.shields.io/travis/bbilger/jrestless.svg?maxAge=2592000&style=flat-square)](https://travis-ci.org/bbilger/jrestless)
[![codecov](https://img.shields.io/codecov/c/github/bbilger/jrestless.svg?maxAge=2592000&style=flat-square)](https://codecov.io/gh/bbilger/jrestless)
[![GitHub issues](https://img.shields.io/github/issues/bbilger/jrestless.svg?maxAge=2592000&style=flat-square)](https://github.com/bbilger/jrestless/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/bbilger/jrestless.svg?maxAge=2592000&style=flat-square)](https://github.com/bbilger/jrestless/issues?q=is%3Aissue+is%3Aclosed)
[![License](https://img.shields.io/github/license/bbilger/jrestless.svg?maxAge=2592000&style=flat-square)](https://github.com/bbilger/jrestless/blob/master/LICENSE)

JRestless is a framework that makes it possible to run JAX-RS applications in FasS environments like AWS Lambda. This is achieved by providing a generic Jersey container that handles requests in the form of POJOs. For each FaaS environment there is a separate module acting as an integration layer between the actual environment and the generic Jersey container.

Since this framework is just a wrapper around Jersey it is possible use the features provided by JAX-RS like filters but also Jersey's custom extensions like Spring integration - not Spring MVC, though since this functionality is provided by JAX-RS itself.

AWS Lambda is the only FaaS environment that supports Java at the moment and so the framework only supports that environment for now.

## Installation

### AWS

#### Gradle

```gradle
repositories {
  ...
  maven {
    url 'https://dl.bintray.com/bbilger/maven/'
  }
}

dependencies {
  compile(
    'com.jrestless.aws:jrestless-aws-gateway-handler:0.2.0',
    ...
  )
  ...
}
```

## Usage example

Create a JAX-RS resource:

```java
@Path("/sample")
public class SampleResource {
  @GET
  @Path("/health")
  public Response getInfo() {
    return Response.ok(new HealthStatusDto("up and running")).build();    
  }
  
  public static class HealthStatusDto {
    private String statusMessage;
    HealthStatusDto(String statusMessage) {
      this.statusMessage = statusMessage;
    }
    public String getStatusMessage() {
      return statusMessage;
    }
  }
}
```

### AWS

Extend the request handler provided by the framework, register your resource in Gateway's default resource config, pass the resource config and start the container:

```java
public class SampleRequestHandler extends GatewayRequestObjectHandler {
  public SampleRequestHandler() {
    init(new GatewayResourceConfig().register(SampleResource.class));
    start();
  }
}
```

Upload the function to a supported region (e.g. `us-west-2`) and give it a name (e.g. `JRestlessSampleFunction`).

Create a new API in API Gateway using `Lambda Function Proxy` and a method using catch-all path variable that invokes your lambda function, or simply create a new API by importing this swagger definition.

```json
{
  "swagger": "2.0",
  "info": {
    "title": "JRestless Example"
  },
  "paths": {
    "/{proxy+}": {
      "x-amazon-apigateway-any-method": {
        "produces": [
          "application/json"
        ],
        "parameters": [
          {
            "name": "proxy",
            "in": "path",
            "required": true,
            "type": "string"
          }
        ],
        "responses": {},
        "x-amazon-apigateway-integration": {
          "uri": "arn:aws:apigateway:YOUR_REGION:lambda:path/2015-03-31/functions/arn:aws:lambda:YOUR_REGION:YOUR_ID:function:MyFunction/YOUR_FUNCTION_NAME",
          "passthroughBehavior": "when_no_match",
          "httpMethod": "POST",
          "responses": {
            "default": {
              "statusCode": "500"
            }
          },
          "type": "aws_proxy"
        }
      }
    }
  }
}
```

Deploy your API.

Hit `YOUR_INVOKE_URL/sample/health`:

```sh
curl -H 'Accept: application/json' 'YOUR_INVOKE_URL/sample/health'
```

## Release History

* 0.2.0 
  * use the new `Lambda Function Proxy` and catch-all paths provided by API Gateway (this reduces the framework's complexity and limitations significantly especially since the generation of a swagger definition is not necessary anymore)
* 0.1.0 (-SNAPSHOT)
  * (never released since the new features `Lambda Function Proxy` and catch-all paths were released before)
  * add support for dynamic response Content-Type
  * add support for custom response headers
* 0.0.1
  * intial release
  
## Meta 

Distributed under Apache 2.0 license. See [License](https://github.com/bbilger/jrestless/blob/master/LICENSE) for more information.
