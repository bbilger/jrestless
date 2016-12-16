# 0.4.0 (2016-12-14)

## New Features

- Binary support: binary data can be returned from endpoints, now. (#13)
- SNS support: SNS events can be handled by JAX-RS endpoints, now. By default the topic and the subject are used to generate the request URI and the message body is assumed to be JSON. Those assumption can easily be overwritten in the SnsRequestObjectHandler. (#5)
- Security: when a _Custom Authorizer_ or a _Cognito User Pool Authorizer_ is configured for an API Gateway endpoint, then the data returned by the authorizer automatically gets translated into a Principal including all claims, that can easily be injected into a resource via the SecurityContext. (#12)
- Jersey: Version 2.25 of Jersey is used, now.

## Fixed Bugs

- #14: Maven can be used, now without any workarounds
- #6: Request objects can be injected into resources as proxies, now. (Context, GatewayRequest, ServiceRequest and SNSRecord)

## Module Changes

- _com.jrestless.aws:jrestless-aws-gateway-core_ and _com.jrestless.aws:jrestless-aws-service-core_ have been merged into _com.jrestless.aws:jrestless-aws-core_.
- _com.jrestless.aws:jrestless-aws-sns-handler_ has been introduced.

## Incompatible Changes

- \*Impl classes have been renamed to Default\*.
- The visibility of most methods in *Handler classes has been changed from _public_ to _protected_.
- GatewayResourceConfig and ServiceResourceConfig have been removed. Use org.glassfish.jersey.server.ResourceConfig and register GatewayFeature, ServiceFeature or SnsFeature depending on the type of function you are using.

# 0.3.0 (2016-10-17)

  - Add support for `AWS Lambda service functions`.
  - Add support to call `AWS Lambda service functions` using [feign](https://github.com/OpenFeign/feign) and the AWS SDK - allowing you to call those functions transparently via REST without using API Gateway.

# 0.2.0 (2016-10-09)

  - use the new `Lambda Function Proxy` and catch-all paths provided by API Gateway (this reduces the framework's complexity and limitations significantly especially since the generation of a swagger definition is not necessary anymore)
  
# 0.1.0 (-SNAPSHOT) (2016-09-07)

  - (never released since the new features `Lambda Function Proxy` and catch-all paths were released before)
  - add support for dynamic response Content-Type
  - add support for custom response headers
  
# 0.0.1 (2016-08-17)

  - intial release
