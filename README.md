# JRestless

JRestless allows you to create serverless (AWS Lambda) applications using JAX-RS.

![](jrestless_512_256.png)

[![Build Status](https://img.shields.io/travis/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://travis-ci.org/bbilger/jrestless)
[![codecov](https://img.shields.io/codecov/c/github/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://codecov.io/gh/bbilger/jrestless)
[![GitHub issues](https://img.shields.io/github/issues/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/issues)
[![GitHub closed issues](https://img.shields.io/github/issues-closed/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/issues?q=is%3Aissue+is%3Aclosed)
[![License](https://img.shields.io/github/license/bbilger/jrestless.svg?maxAge=60&style=flat-square)](https://github.com/bbilger/jrestless/blob/master/LICENSE)


* [Description](#description)
* [Motivation](#motivation)
* [Usage Example](#usage-example)
  * [AWS Usage Example](#aws-usage-example)
* [Modules](#modules)
* [Release History](#release-history)
* [Meta](#meta)
  * [License](#license)
  * [SonarQube Metrics](#sonarqube-metrics)

## Description

JRestless is a framework allowing you to build serverless JAX-RS applications or rather to run JAX-RS applications in FasS (Function as a Service) environments like AWS Lambda. This is achieved by providing a generic Jersey container that handles requests in the form of POJOs. For each FaaS environment there is a separate module acting as an integration layer between the actual environment and the generic Jersey container.

Since this framework is just a wrapper around Jersey it is possible to use the features provided by JAX-RS. This includes   filters, for example, but also Jersey's custom extensions like **Spring integration** - not Spring MVC, though since this functionality is provided by JAX-RS itself.

AWS Lambda is the only FaaS environment that supports Java at the moment and so it is the only supported environment for now.

## Motivation

The motivation for this project is to avoid a cloud vendor lock-in and to allow developers to run and test their code locally.

## Usage Example

All examples can be found in a separate repository: https://github.com/bbilger/jrestless-examples

### AWS Usage Example

JRestless does not depend on the [serverless framework](https://github.com/serverless/serverless) but it simplifies the necessary AWS configuration tremendously and will be used for this example.

Install `serverless` as described in the docs https://serverless.com/framework/docs/guide/installing-serverless/

Setup your AWS account as described in the docs https://serverless.com/framework/docs/providers/aws/setup/

Create a new function using `serverless`

```bash
mkdir aws-gateway-usage-example
cd aws-gateway-usage-example
serverless create --template aws-java-gradle --name aws-gateway-usage-example
rm -rf src/main/java/hello # remove the classes created by the template
mkdir -p src/main/java/com/jrestless/aws/examples # create the package structure
```
Replace `serverless.yml` with the following contents:

```yml
service: aws-gateway-usage-example-service

provider:
  name: aws
  runtime: java8
  stage: dev
  region: us-west-2

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
    'com.jrestless.aws:jrestless-aws-gateway-handler:0.3.0',
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

Create a new JAX-RS resource and a JAXB DTO (`src/main/java/com/jrestless/aws/examples/SampleResource.java`):

```java
package com.jrestless.aws.examples;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Path("/sample")
public class SampleResource {
  @GET
  @Path("/health")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  public Response getHealth() {
    return Response.ok(new HealthStatusDto("up and running")).build();
  }
  @XmlRootElement // for JAXB
  public static class HealthStatusDto {
    private String statusMessage;
    @SuppressWarnings("unused")
    private HealthStatusDto() {
      // for JAXB
    }
    HealthStatusDto(String statusMessage) {
      this.statusMessage = statusMessage;
    }
    @XmlElement // for JAXB
    public String getStatusMessage() {
      return statusMessage;
    }
  }
}
```

Create the request handler (`src/main/java/com/jrestless/aws/examples/RequestHandler.java`):
```java
package com.jrestless.aws.examples;

import com.jrestless.aws.gateway.GatewayResourceConfig;
import com.jrestless.aws.gateway.handler.GatewayRequestObjectHandler;

public class RequestHandler extends GatewayRequestObjectHandler {
  public RequestHandler() {
    init(new GatewayResourceConfig().packages("com.jrestless.aws.examples"));
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
  ANY - https://<SOMEID>.execute-api.us-west-2.amazonaws.com/dev/sample/{proxy+}
...
```

Hit the endpoint:

```sh
curl -H 'Accept: application/json' 'https://<SOMEID>.execute-api.us-west-2.amazonaws.com/dev/sample/health'
# {"statusMessage":"up and running"}
```

```sh
curl -H 'Accept: application/xml' 'https://<SOMEID>.execute-api.us-west-2.amazonaws.com/dev/sample/health'
# <?xml version="1.0" encoding="UTF-8" standalone="yes"?><healthStatusDto><statusMessage>up and running</statusMessage></healthStatusDto>
```

## Modules
JRestless is split up into multiple modules wheras one has to depend on the \*-handler modules, only. [jrestless-aws-gateway-handler](aws/gateway/jrestless-aws-gateway-handler) is probably the most interesting one.

All modules are available in jcenter.

* **jrestless-aws-gateway-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-gateway-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-gateway-handler/_latestVersion)
  * Provides an AWS Lambda RequestHandler that delegates requests from AWS API Gateway to Jersey.
  * [Read More...](aws/gateway/jrestless-aws-gateway-handler)
* **jrestless-aws-gateway-core** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-gateway-core/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-gateway-core/_latestVersion)
  * Contains interfaces used by [jrestless-aws-gateway-handler](aws/gateway/jrestless-aws-gateway-handler) and might be of interest for local development, as well.
  * [Read More...](aws/gateway/jrestless-aws-gateway-core)
* **jrestless-aws-service-handler** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-handler/_latestVersion)
  * Provides an  AWS Lambda RequestHandler that delegates requests - in a HTTP format - to Jersey. This is intended but not limited to call one Lambda function from another.
  * [Read More...](aws/service/jrestless-aws-service-handler)
* **jrestless-aws-service-core** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-core/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-core/_latestVersion)
  * Contains interfaces and classes used by [jrestless-aws-service-handler](aws/service/jrestless-aws-service-handler), [jrestless-aws-service-feign-client](aws/service/jrestless-aws-service-feign-client) and might be of interest for local development, as well.
  * [Read More...](aws/service/jrestless-aws-service-core)
* **jrestless-aws-service-feign-client** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-feign-client/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-feign-client/_latestVersion)
  * Provides a [feign](https://github.com/OpenFeign/feign) client to call Lambda functions that use [jrestless-aws-service-handler](aws/service/jrestless-aws-service-handler) a.k.a. Lambda service functions. This allows to call Lambda service functions transparently through feign.
  * [Read More...](aws/service/jrestless-aws-service-feign-client)
* **jrestless-core-container** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-core-container/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-core-container/_latestVersion)
  * Provides a generic (provider independent) Jersey container that handles requests in the form of POJOs.
  * [Read More...](core/jrestless-core-container)
* **jrestless-test** [ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-test/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-test/_latestVersion)
  * Provides common test functionality.
  * [Read More...](test/jrestless-test)

## Release History
* 0.3.0
  * Add support for `AWS Lambda service functions`.
  * Add support to call `AWS Lambda service functions` using [feign](https://github.com/OpenFeign/feign) and the AWS SDK - allowing you to call those functions transparently via REST without using API Gateway.
* 0.2.0
  * use the new `Lambda Function Proxy` and catch-all paths provided by API Gateway (this reduces the framework's complexity and limitations significantly especially since the generation of a swagger definition is not necessary anymore)
* 0.1.0 (-SNAPSHOT)
  * (never released since the new features `Lambda Function Proxy` and catch-all paths were released before)
  * add support for dynamic response Content-Type
  * add support for custom response headers
* 0.0.1
  * intial release

## Meta

### License

Distributed under Apache 2.0 license. See [License](https://github.com/bbilger/jrestless/blob/master/LICENSE) for more information.

### SonarQube Metrics

[![SonarQube Coverage](https://img.shields.io/sonar/http/sonarqube.com/jrestless/coverage.svg?maxAge=60&style=flat-square&label=SonarQube%20Coverage)](https://sonarqube.com/component_measures/domain/Coverage?id=jrestless)
[![SonarQube Bugs](https://img.shields.io/sonar/http/sonarqube.com/jrestless/bugs.svg?maxAge=60&style=flat-square&label=SonarQube%20Bugs)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=BUG)
[![SonarQube Vulnerabilities](https://img.shields.io/sonar/http/sonarqube.com/jrestless/vulnerabilities.svg?maxAge=60&style=flat-square&label=SonarQube%20Vulnerabilities)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=VULNERABILITY)
[![SonarQube Tests](https://img.shields.io/sonar/http/sonarqube.com/jrestless/tests.svg?maxAge=60&style=flat-square&label=SonarQube%20Tests)](https://sonarqube.com/component_measures/metric/tests/list?id=jrestless)
[![SonarQube Duplicated Blocks](https://img.shields.io/sonar/http/sonarqube.com/jrestless/duplicated_blocks.svg?maxAge=60&style=flat-square&label=SonarQube%20Duplicated%20Blocks)](https://sonarqube.com/component_measures/metric/duplicated_blocks/list?id=jrestless)
[![SonarQube Technical Debt](https://img.shields.io/sonar/http/sonarqube.com/jrestless/tech_debt.svg?maxAge=60&style=flat-square&label=SonarQube Technical Debt)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|facetMode=effort|types=CODE_SMELL)
[![SonarQube Code Smells](https://img.shields.io/sonar/http/sonarqube.com/jrestless/code_smells.svg?maxAge=60&style=flat-square&label=SonarQube%20Code%20Smells)](https://sonarqube.com/component_issues?id=jrestless#resolved=false|types=CODE_SMELL)
