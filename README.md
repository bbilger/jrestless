# JRestless

JRestless allows you to create serverless or rather AWS Lambda applications using JAX-RS.

![](jrestless_512_256.png)

[![Build Status](https://img.shields.io/travis/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://travis-ci.org/bbilger/jrestless)
[![codecov](https://img.shields.io/codecov/c/github/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://codecov.io/gh/bbilger/jrestless)
[![GitHub issues](https://img.shields.io/github/issues/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/issues?q=is%3Aissue+is%3Aclosed)
[![License](https://img.shields.io/github/license/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/blob/master/LICENSE)

SonarQube:
[![SonarQube - Quality Gate](https://sonarqube.com/api/badges/gate?key=jrestless)](https://sonarqube.com/dashboard?id=jrestless)
[![SonarQube - Coverage](https://sonarqube.com/api/badges/measure?key=jrestless&metric=coverage&template=FLAT)](https://sonarqube.com/component_measures/domain/Coverage?id=jrestless)
[![SonarQube - Bugs](https://sonarqube.com/api/badges/measure?key=jrestless&metric=bugs&template=FLAT)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=BUG)
[![SonarQube - Vulnerabilities](https://sonarqube.com/api/badges/measure?key=jrestless&metric=vulnerabilities&template=FLAT)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=VULNERABILITY)
[![SonarQube - Tests](https://sonarqube.com/api/badges/measure?key=jrestless&metric=tests&template=FLAT)](https://sonarqube.com/component_measures/metric/tests/list?id=jrestless)
[![SonarQube - Duplicated Blocks](https://sonarqube.com/api/badges/measure?key=jrestless&metric=duplicated_lines_density&template=FLAT)](https://sonarqube.com/component_measures/metric/duplicated_blocks/list?id=jrestless)
[![SonarQube - Technical Debt](https://sonarqube.com/api/badges/measure?key=jrestless&metric=sqale_debt_ratio&template=FLAT)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|facetMode=effort|types=CODE_SMELL)
[![SonarQube - Code Smells](https://sonarqube.com/api/badges/measure?key=jrestless&metric=code_smells&template=FLAT)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=CODE_SMELL)

## Table of Contents

* [Description](#description)
* [Motivation](#motivation)
* [Features](#features)
* [Function Types](#function-types)
* [Usage Example](#usage-example)
  * [AWS Usage Example](#aws-usage-example)
* [Modules](#modules)
* [Release History](#release-history)
* [Alternative Projects](#alternative-projects)
* [Limitations](#limitations)
* [Meta](#meta)
  * [License](#license)

## Description

JRestless is a framework allowing you to build serverless JAX-RS applications or rather to run JAX-RS applications in FasS (Function as a Service) environments like AWS Lambda. This is achieved by providing a generic Jersey container that handles requests in the form of POJOs. For each FaaS environment there is a separate module acting as an integration layer between the actual environment and the generic Jersey container.

Since this framework is just a wrapper around or rather a container for Jersey, you can use almost all JAX-RS features plus Jersey's custom extensions like **Spring integration** - not Spring MVC, though since this functionality is provided by JAX-RS itself.

AWS Lambda is the only FaaS environment that supports Java at the moment and so it is the only supported environment for now.

## Motivation

The motivation for this project is to avoid a cloud vendor lock-in and to allow developers to run and test their code locally.

## Features

- Almost all JAX-RS features can be used (JSON/XML/text/... requests/responses, container request/response filters, etc.). Example: [aws-gateway-showcase](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-showcase)
- Jersey extensions can be used. For example:
  - **Spring**: [aws-gateway-spring](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-spring)
  - **CDI**: [aws-gateway-cdi](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-cdi)
  - **Guice**: [aws-gateway-guice](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-guice)
- _AWS Gateway Functions_ can also consume and produce **binary** data. Example: [aws-gateway-binary](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-binary)
- _AWS Gateway Functions_ use the data added to the request by authorizers (_Custom Authorizers_ or _Cognito User Pool Authorizers_) to create a Principal ([CustomAuthorizerPrincipal](https://github.com/bbilger/jrestless/blob/master/aws/core/jrestless-aws-core/src/main/java/com/jrestless/aws/security/CustomAuthorizerPrincipal.java) or [CognitoUserPoolAuthorizerPrincipal](https://github.com/bbilger/jrestless/blob/master/aws/core/jrestless-aws-core/src/main/java/com/jrestless/aws/security/CognitoUserPoolAuthorizerPrincipal.java)) within the SecurityContext containing all claims. Examples: [aws-gateway-security-cognito-authorizer](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-security-cognito-authorizer) and [aws-gateway-security-custom-authorizer](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-security-custom-authorizer)
- _AWS Gateway Functions_ can use a CORS filter. Example: [aws-gateway-cors](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-cors-frontend)
- Injection of provider and/or function type specific values via `@javax.ws.rs.core.Context` into resources and endpoints:
  - All AWS functions can inject `com.amazonaws.services.lambda.runtime.Context`.
  - _AWS Gateway Functions_ can also inject the raw request [GatewayRequest](https://github.com/bbilger/jrestless/blob/master/aws/gateway/jrestless-aws-gateway-core/src/main/java/com/jrestless/aws/gateway/io/GatewayRequest.java)
  - _AWS Service Functions_ can also inject the raw request [ServiceRequest](https://github.com/bbilger/jrestless/blob/master/aws/service/jrestless-aws-service-core/src/main/java/com/jrestless/aws/service/io/ServiceRequest.java)
  - _AWS SNS Functions_ can also inject the raw request [SNSRecord](https://github.com/aws/aws-lambda-java-libs/blob/master/aws-lambda-java-events/src/main/java/com/amazonaws/services/lambda/runtime/events/SNSEvent.java#L225)
- It's worth mentioning that _AWS Gateway Functions_ is designed to be used with API Gateway's _proxy integration type_ for _Lambda Functions_. So there are no limitations on the status code, the headers and the body you return.

## Function Types

### AWS

- _Gateway Functions_ are AWS Lambda functions that get invoked by AWS API Gateway. Usage example: [aws-gateway-usage-example](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-usage-example). [Read More...](aws/gateway/jrestless-aws-gateway-handler).
- _Service Functions_ are AWS Lambda functions that can either be invoked by other AWS Lambda functions or can be invoked directly through the AWS SDK. The point is that you don't use AWS API Gateway. You can abstract the fact that you invoke an AWS Lambda function away by using a special feign client ([jrestless-aws-service-feign-client](aws/service/jrestless-aws-service-feign-client)). Usage example: [aws-service-usage-example](https://github.com/bbilger/jrestless-examples/tree/master/aws/service/aws-service-usage-example). [Read More...](aws/service/jrestless-aws-service-handler).
- _SNS functions_ are AWS Lambda function that get invoked by SNS. This allow asynchronous calls to other Lambda functions. So when one Lambda function publishes a message to one SNS topic, SNS can then invoke all (1-N) subscribed Lambda functions. Usage example: [aws-sns-usage-example](https://github.com/bbilger/jrestless-examples/tree/master/aws/sns/aws-sns-usage-example). [Read More...](aws/sns/jrestless-aws-sns-handler).

Note: the framework is split up into multiple modules, so you choose which functionality you actually want to use. See [Modules](#modules)

## Usage Example

All examples, including the following one, can be found in a separate repository: https://github.com/bbilger/jrestless-examples

### AWS Usage Example

JRestless does not depend on the [serverless framework](https://github.com/serverless/serverless) but it simplifies the necessary AWS configuration tremendously and will be used for this example.

Install `serverless` (>= 1.0.2) as described in the docs https://serverless.com/framework/docs/guide/installing-serverless/

Setup your AWS account as described in the docs https://serverless.com/framework/docs/providers/aws/guide/credentials/

Create a new function using `serverless`

```bash
mkdir aws-gateway-usage-example
cd aws-gateway-usage-example
serverless create --template aws-java-gradle --name aws-gateway-usage-example
rm -rf src/main/java # remove the classes created by the template
mkdir -p src/main/java/com/jrestless/aws/examples # create the package structure
```
Replace `serverless.yml` with the following contents:

```yml
service: aws-gateway-usage-example-service

provider:
  name: aws
  runtime: java8
  stage: dev
  region: eu-central-1

package:
  artifact: build/distributions/aws-gateway-usage-example.zip

functions:
  sample:
    handler: com.jrestless.aws.examples.RequestHandler
    events:
      - http:
          path: sample/{proxy+}
          method: any

```

Replace `build.gradle` with the following contents:

```gradle
apply plugin: 'java'

repositories {
  jcenter()
  mavenCentral()
}
dependencies {
  compile(
    'com.jrestless.aws:jrestless-aws-gateway-handler:0.5.0',
    'org.glassfish.jersey.media:jersey-media-json-jackson:2.23'
  )
}
task buildZip(type: Zip) {
  archiveName = "${project.name}.zip"
  from compileJava
  from processResources
  into('lib') {
    from configurations.runtime
  }
}
build.dependsOn buildZip

```

Create a new JAX-RS resource and a response object (`src/main/java/com/jrestless/aws/examples/SampleResource.java`):

```java
package com.jrestless.aws.examples;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/sample")
public class SampleResource {
  @GET
  @Path("/health")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getHealthStatus() {
    return Response.ok(new HealthStatusResponse("up and running")).build();
  }
  static class HealthStatusResponse {
    private final String statusMessage;
    HealthStatusResponse(String statusMessage) {
      this.statusMessage = statusMessage;
    }
    public String getStatusMessage() {
      return statusMessage;
    }
  }
}
```

Create the request handler (`src/main/java/com/jrestless/aws/examples/RequestHandler.java`):
```java
package com.jrestless.aws.examples;

import org.glassfish.jersey.server.ResourceConfig;

import com.jrestless.aws.gateway.GatewayFeature;
import com.jrestless.aws.gateway.handler.GatewayRequestObjectHandler;

public class RequestHandler extends GatewayRequestObjectHandler {
  public RequestHandler() {
    // initialize the container with your resource configuration
    ResourceConfig config = new ResourceConfig()
      .register(GatewayFeature.class)
      .packages("com.jrestless.aws.examples");
    init(config);
    // start the container
    start();
  }
}
```

Build your function from within the directory `aws-gateway-usage-example`:
```bash
gradle build
```
This, amongst other things, creates a deployable version of your function (`build/distributions/aws-gateway-usage-example.zip`) using the dependent task `buildZip`.

Now you can deploy the function using `serverless`:

```bash
serverless deploy
```

If `serverless` is configured correctly, it should show you an endpoint in its output.
```
...
endpoints
  ANY - https://<SOMEID>.execute-api.eu-central-1.amazonaws.com/dev/sample/{proxy+}
...
```

Hit the endpoint:

```sh
curl -H 'Accept: application/json' 'https://<SOMEID>.execute-api.eu-central-1.amazonaws.com/dev/sample/health'
# {"statusMessage":"up and running"}
```

## Modules
JRestless is split up into multiple modules whereas one has to depend on the \*-handler modules, only. [jrestless-aws-gateway-handler](aws/gateway/jrestless-aws-gateway-handler) is probably the most interesting one.

All modules are available in jcenter.

* **jrestless-aws-gateway-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-gateway-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-gateway-handler/_latestVersion)
  * Provides an AWS Lambda RequestHandler that delegates requests from AWS API Gateway to Jersey.
  * [Read More...](aws/gateway/jrestless-aws-gateway-handler)
* **jrestless-aws-service-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-handler/_latestVersion)
  * Provides an AWS Lambda RequestHandler that delegates requests - in an HTTP format - to Jersey. This is intended but not limited to call one Lambda function from another.
  * [Read More...](aws/service/jrestless-aws-service-handler)
* **jrestless-aws-sns-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-sns-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-sns-handler/_latestVersion)
  * Provides an AWS Lambda RequestHandler that delegates pushes from SNS to Jersey. This is intended to call a Lambda function asynchronously from another.
  * [Read More...](aws/sns/jrestless-aws-sns-handler)
* **jrestless-aws-core** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-core/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-core/_latestVersion)
  * Contains interfaces and classes used by [jrestless-aws-gateway-handler](aws/gateway/jrestless-aws-gateway-handler), [jrestless-aws-service-handler](aws/service/jrestless-aws-service-handler), [jrestless-aws-sns-handler](aws/sns/jrestless-aws-sns-handler) and [jrestless-aws-service-feign-client](aws/service/jrestless-aws-service-feign-client), and might be of interest for local development, as well.
  * [Read More...](aws/core/jrestless-aws-core)
* **jrestless-aws-service-feign-client** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-feign-client/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-feign-client/_latestVersion)
  * Provides a [feign](https://github.com/OpenFeign/feign) client to call Lambda functions that use [jrestless-aws-service-handler](aws/service/jrestless-aws-service-handler) a.k.a. Lambda service functions. This allows to call Lambda service functions transparently through feign.
  * [Read More...](aws/service/jrestless-aws-service-feign-client)
* **jrestless-core-container** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-core-container/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-core-container/_latestVersion)
  * Provides a generic (provider independent) Jersey container that handles requests in the form of POJOs.
  * [Read More...](core/jrestless-core-container)
* **jrestless-aws-core-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-core-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-core-handler/_latestVersion)
  * Common functionality shared across AWS handlers: [jrestless-aws-gateway-handler](aws/gateway/jrestless-aws-gateway-handler), [jrestless-aws-service-handler](aws/service/jrestless-aws-service-handler) and [jrestless-aws-sns-handler](aws/sns/jrestless-aws-sns-handler)
  * [Read More...](aws/core/jrestless-aws-core-handler)
* **jrestless-test** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-test/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-test/_latestVersion)
  * Provides common test functionality.
  * [Read More...](test/jrestless-test)

## Alternative Projects

### AWS

* Java
  * [lambadaframework](https://github.com/lambadaframework/lambadaframework) provides similar functionality like JRestless. It implements some features of the JAX-RS standard and includes deployment functionality within the framework itself.
  * [ingenieux/lambada](https://github.com/ingenieux/lambada) Non-JAX-RS Java framework
  * [aws-lambda-servlet](https://github.com/bleshik/aws-lambda-servlet) run JAX-RS applications - uses Jersey and pretends to run in a servlet container
* JavaScript
  * [aws-serverless-express](https://github.com/awslabs/aws-serverless-express) - run [express](https://github.com/expressjs/express) applications
* Python
  * [Zappa](https://github.com/Miserlou/Zappa) - run and deploy Python applications
  * [Chalice](https://github.com/awslabs/chalice) - run and deploy Python applications

## Limitations

### AWS

* for all function types
  * stateless only (you could utilize some cache like Redis, though)
  * AWS Lambda functions have a maximum execution time of 5 minutes
* _Gateway functions_
  * AWS API Gateway has a timeout of 30 seconds
  * Multiple headers with same name are not supported

## Release History
[CHANGELOG](CHANGELOG.md)

## Meta

### License

Distributed under Apache 2.0 license. See [License](https://github.com/bbilger/jrestless/blob/master/LICENSE) for more information.
