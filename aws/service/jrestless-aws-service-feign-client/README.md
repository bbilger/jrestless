# jrestless-aws-service-feign-client

[ ![Download](https://api.bintray.com/packages/bbilger/maven/jrestless-aws-service-feign-client/images/download.svg) ](https://bintray.com/bbilger/maven/jrestless-aws-service-feign-client/_latestVersion)

Provides a [feign](https://github.com/OpenFeign/feign) client to call Lambda functions that use [jrestless-aws-service-handler](../jrestless-aws-service-handler) a.k.a. Lambda service functions. This allows to call Lambda service functions transparently through feign.

## Client

Lambda service function invocation through feign.

_Note that AWSLambdaClient and the proxy are thread-safe so you can externalize the generation._

```java
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.jrestless.aws.service.client.FeignLambdaServiceInvokerClient;
import com.jrestless.aws.service.client.LambdaServiceFunctionTarget;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

/*
 * AWSLambdaClient's default constructor is sufficient
 * to call one Lambda function from another.
 * If you call the Lambda function from somewhere else
 * you need to pass the credentials.
 */
AWSLambdaClient awsLambdaClient = new AWSLambdaClient();
// the region your Lambda function is deployed to
awsLambdaClient.configureRegion(Regions.EU_CENTRAL_1);
Client lambdaFeignClient = FeignLambdaServiceInvokerClient.builder()
  // the region your Lambda function is deployed to
  .setRegion(Regions.EU_CENTRAL_1)
  .setFunctionName(functionName)
  .build();
YourApiDefinedInFeign inocationProxy = Feign.builder()
  .client(lambdaFeignClient)
  .encoder(new JacksonEncoder()) // you can use any JSON encoder
  .decoder(new JacksonDecoder()) // you can use any JSON decoder
  // we need a custom FunctionTarget since we don't have an absolute URL
  .target(new LambdaServiceFunctionTarget<>(YourApiDefinedInFeign.class));

// invoke your Lambda function
SomeResponseObject response = inocationProxy.ping(new SomeRequestObject(...));
```

A feign API interface could look like this

```java
/*
 * assuming you use JSON for the request/response body
 * but you can use XML or anything else, too
 */
@Headers({
	"Accept: application/json",
	"Content-Type: application/json"
})
public interface YourApiDefinedInFeign {
	@RequestLine("POST /ping")
	SomeResponseObject ping(SomeRequestObject request);
}
```

## Lambda Service Function

The request handler would be the following, as usual

```java
import com.jrestless.aws.service.ServiceResourceConfig;
import com.jrestless.aws.service.handler.ServiceRequestObjectHandler;

public class RequestHandler extends ServiceRequestObjectHandler {
  public RequestHandler() {
    init(new ServiceResourceConfig().packages("<your package name containing JAX-RS resources>"));
    start();
  }
}
```

A JAX-RS resource could look like this

```java
import com.jrestless.aws.service.io.ServiceRequest;

@Path("/")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class SampleApi {
  @POST
  @Path("/ping")
  public Response ping(SomeRequestObject serviceRequest) {
    return Response.ok(new SomeResponseObject(...)).build();
  }
}
```
