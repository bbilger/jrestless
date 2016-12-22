# jrestless-aws-gateway-handler

[ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-gateway-handler/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-gateway-handler/_latestVersion)

This module provides an AWS Lambda RequestHandler that delegates requests from AWS API Gateway to Jersey.

```java
import com.jrestless.aws.gateway.GatewayResourceConfig;
import com.jrestless.aws.gateway.handler.GatewayRequestObjectHandler;

// this is your AWS Lambda function
public class RequestHandler extends GatewayRequestObjectHandler {
  public RequestHandler() {
    /*
     * This will start a Jersey container that delegates all requests proxied through from AWS API Gateway
     * to this Lambda function to your JAX-RS resources.
     */
    init(new GatewayResourceConfig().packages("<your package name containing JAX-RS resources>"));
    start();
  }
}
```

```java
import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;

@Path("/")
public class SampleResource {
  /*
   * You can inject the original request proxied through from AWS API Gateway to your Lambda function into
   * any of your JAX-RS endpoints.
   * You can inject GatewayRequestContext and GatewayIdentity, as well. Those are part of GatewayRequest.
   */
  @GET
  @Path("/request-injection")
  public Response gatewayRequestInjection(@Context GatewayRequest gatewayRequest) {
    ...
  }
  /*
  * You can inject the Lambda context for a request into
  * any of your JAX-RS endpoints.
  */
  @GET
  @Path("/lambda-context-injection")
  public Response lambdaContextInjection(@Context com.amazonaws.services.lambda.runtime.Context lambdaContext) {
    ...
  }
}
```

## Request Schema 

```json
{
  "type": "object",
  "id": "com.jrestless.aws.gateway.io.GatewayRequest",
  "properties": {
    "headers": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "pathParameters": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "path": {
      "type": "string"
    },
    "requestContext": {
      "type": "object",
      "title": "com.jrestless.aws.gateway.io.GatewayRequestContext",
      "properties": {
        "accountId": {
          "type": "string"
        },
        "resourceId": {
          "type": "string"
        },
        "stage": {
          "type": "string"
        },
        "requestId": {
          "type": "string"
        },
        "identity": {
          "type": "object",
          "title": "com.jrestless.aws.gateway.io.GatewayIdentity",
          "properties": {
            "cognitoIdentityPoolId": {
              "type": "string"
            },
            "accountId": {
              "type": "string"
            },
            "cognitoIdentityId": {
              "type": "string"
            },
            "caller": {
              "type": "string"
            },
            "apiKey": {
              "type": "string"
            },
            "sourceIp": {
              "type": "string"
            },
            "cognitoAuthenticationType": {
              "type": "string"
            },
            "cognitoAuthenticationProvider": {
              "type": "string"
            },
            "userArn": {
              "type": "string"
            },
            "userAgent": {
              "type": "string"
            },
            "user": {
              "type": "string"
            }
          }
        },
        "resourcePath": {
          "type": "string"
        },
        "httpMethod": {
          "type": "string"
        },
        "apiId": {
          "type": "string"
        }
      },
      "authorizer": {
        "type": "object",
        "additionalProperties": {
          "type": "string"
        },
        "description": "empty for requests without authorizers; for custom authorizers this map will contain the principalId and all additional claims (from the context); for Cognito user pool authorizers it will contain all claims of the ID token"
      }
    },
    "resource": {
      "type": "string"
    },
    "queryStringParameters": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "stageVariables": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "httpMethod": {
      "type": "string"
    },
    "body": {
      "type": "string"
    },
    "isBase64Encoded": {
      "type": "boolean",
      "description": "binary requests get base64 encoded if the request's Content-Type header matches a configured binaryMediaType"
    }
  }
}
```

## Response Schema

```json
{
  "type": "object",
  "title": "com.jrestless.aws.gateway.io.GatewayResponse",
  "properties": {
    "headers": {
      "type": "object",
      "additionalProperties": {
        "type": "string"
      }
    },
    "body": {
      "type": "string"
    },
    "statusCode": {
      "type": "integer"
    },
    "isBase64Encoded": {
      "type": "boolean",
      "description": "optional; if set to true and the request's accept header matches a configured binary media type, API Gateway will respond with the decoded binary"
    }
  }
}
```
