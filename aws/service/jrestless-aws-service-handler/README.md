# jrestless-aws-service-handler

[ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-handler/_latestVersion)

This module provides an AWS Lambda RequestHandler that delegates requests - in a HTTP format - to Jersey. This is intended but not limited to call one Lambda function from another.


```java
import com.jrestless.aws.service.ServiceResourceConfig;
import com.jrestless.aws.service.handler.ServiceRequestObjectHandler;

// this is your AWS Lambda function
public class RequestHandler extends ServiceRequestObjectHandler {
  public RequestHandler() {
    /*
     * This will start a Jersey container that delegates all requests
     * to this Lambda function to your JAX-RS resources.
     */
    init(new ServiceResourceConfig().packages("<your package name containing JAX-RS resources>"));
    start();
  }
}
```

```java
import com.jrestless.aws.service.io.ServiceRequest;

@Path("/")
public class SampleResource {
  /*
   * You can inject the original request to your Lambda function into
   * any of your JAX-RS endpoints.
   */
  @GET
  @Path("/request-injection")
  public void serviceRequestInjection(@Context ServiceRequest serviceRequest) {
    ...
  }
  /*
  * You can inject the Lambda context for a request into
  * any of your JAX-RS endpoints.
  */
  @GET
  @Path("/lambda-context-injection")
  public void lambdaContextInjection(@Context com.amazonaws.services.lambda.runtime.Context lambdaContext) {
    ...
  }
}
```

The request schema is:

```json
{
  "type": "object",
  "title": "com.jrestless.aws.service.io.ServiceRequest",
  "properties": {
    "body": {
      "type": "string"
    },
    "headers": {
      "type": "object",
        "additionalProperties": {
          "type": "array",
          "items": {
            "type": "string"
          }
        }
    },
    "httpMethod": {
      "type": "string"
    },
    "requestUri": {
      "type": "string"
    }
  },
	"required": ["httpMethod", "requestUri"]
}
```
The response schema is:

```json
{
  "type": "object",
  "title": "com.jrestless.aws.service.io.ServiceResponse",
  "properties": {
    "body": {
      "type": "string"
    },
    "headers": {
      "type": "object",
      "additionalProperties": {
        "type": "array",
        "items": {
          "type": "string"
        }
      }
    },
    "reasonPhrase": {
      "type": "string"
    },
    "statusCode": {
      "type": "integer"
    }
  },
	"required": ["statusCode"]
}
```
