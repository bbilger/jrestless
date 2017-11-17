# 0.6.0 (2017-11-17)

## New Features

- Jersey version 2.26 is used now: https://github.com/bbilger/jrestless/issues/41
- (initial) support for Fn Project has been added; thanks to this PR: https://github.com/bbilger/jrestless/pull/43

## Breaking Changes / Migration

There are a few breaking changes (https://jersey.github.io/documentation/latest/user-guide.html#mig-2.26) in Jersey 2.26 but most of these changes affect JRestless' internals, only.

You are, however, required to add an additional dependency:

``` 
# Gradle
compile group: 'org.glassfish.jersey.inject', name: 'jersey-hk2', version: '2.26'

# Maven
<dependency>
    <groupId>org.glassfish.jersey.inject</groupId>
    <artifactId>jersey-hk2</artifactId>
    <version>2.26</version>
</dependency>

```

## Known Issues

Aside from the HK2 injection manager (jersey-hk2) mentioned above, Jersey offers an alternative CDI 2 SE injection manager (jersey-cdi2-se).
The CDI 2 SE injection manager (jersey-cdi2-se) is, however, not yet supported by JRestless because of a bug which by the way affects all containers: https://github.com/jersey/jersey/issues/3621


# 0.5.1 (2017-04-22)

## New Features

JRestless now supports Cognito (Federated) Identity and IAM authentication. (https://github.com/bbilger/jrestless/issues/35)

The new AwsSecurityContextFilter sets the according Principal (CognitoUserPoolAuthorizerPrincipal, CustomAuthorizerPrincipal, CognitoIdentityPrincipal or IamPrincipal) when available. AwsSecurityContextFilter gets registered through the GatewayFeature.

## Breaking Changes

CustomAuthorizerFilter and CustomUserPoolAuthorizerFilter are replaced by AwsSecurityContextFilter.

# 0.5.0 (2017-03-12)

## New Features

- baseUri and requestUri are handled correctly, now: https://github.com/bbilger/jrestless-docs/blob/master/src/docs/asciidoc/uri_handling.adoc
- support for @ApplicationPath was added
- usage of *Features (e.g. GatewayFeature) is optional, now but highly recommended since a couple of useful filters are registered
- a CORSFilter has been added: [example](https://github.com/bbilger/jrestless-examples/tree/master/aws/gateway/aws-gateway-cors-frontend)
- gzip encoding will be applied for binary responses only
- Jersey's CDI exstenion can be used (#32)

## Breaking changes

- *TYPES are not exposed in the *Features anymore => define them yourself
- SimpleRequestHandler#init(Application, Binder, ServiceLocator) has been removed => use #init(Application, ServiceLocator) and override #createBinder

# 0.4.0 (2016-12-14)

## New Features
- Binary support: binary data can be returned from endpoints, now. (#13)
- SNS support: SNS events can be handled by JAX-RS endpoints, now. By default the topic and the subject are used to generate the request URI and the message body is assumed to be JSON. Those assumption can easily be overwritten in the SnsRequestObjectHandler. (#5)
- Security: when a _Custom Authorizer_ or a _Cognito User Pool Authorizer_ is configured for an API Gateway endpoint, then the data returned by the authorizer automatically gets translated into a Principal including all claims, that can easily be injected into a resource via the SecurityContext. (#12)
- Jersey: Version 2.25 of Jersey is used now.

## Fixed Bugs
- #14: Maven can be used, now without any workarounds
- #6: Request objects can be injected into resources as proxies, now. (Context, GatewayRequest, ServiceRequest and SNSRecord)

## Module Changes
- _com.jrestless.aws:jrestless-aws-gateway-core_ and _com.jrestless.aws:jrestless-aws-service-core_ have been merged into _com.jrestless.aws:jrestless-aws-core_.
- _com.jrestless.aws:jrestless-aws-sns-handler_ has been introduced.

## Incompatible Changes
- \*Impl classes have been renamed to Default\*.
- The visibility of most methods in *Handler classes has been changed from _public_ to _protected_.
- GatewayIdentity and GatewayRequestContext are not injectable directly anymore. So one has to inject GatewayRequest and access GatewayRequestContext like thisgatewayRequest.getRequestContext() and GatewayIdentity like this gatewayRequest.getRequestContext().getIdentity().
- GatewayResourceConfig, and ServiceResourceConfig have been dropped. Use a plain ResourceConfig and register GatewayFeature, ServiceFeature or SnsFeature; depending on the function type you use.
- GatewayRequest and ServiceRequest are no longer available in the ContainerRequestContext but they can be injected (@Inject) everywhere.

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
