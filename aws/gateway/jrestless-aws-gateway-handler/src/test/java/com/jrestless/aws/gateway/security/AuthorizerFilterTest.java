package com.jrestless.aws.gateway.security;

public abstract class AuthorizerFilterTest {

//	abstract AuthorizerFilter createCognitoAuthorizerFilter(GatewayRequest gatewayRequest);
//
//	protected final GatewayRequest createRequest(Map<String, Object> authorizerData) {
//		GatewayRequestContext gatewayRequestContext = mock(GatewayRequestContext.class);
//		when(gatewayRequestContext.getAuthorizer()).thenReturn(authorizerData);
//
//		GatewayRequest gatewayRequest = mock(GatewayRequest.class);
//		when(gatewayRequest.getRequestContext()).thenReturn(gatewayRequestContext);
//
//		return gatewayRequest;
//	}
//
//	@Test
//	public void noGatewayRequestContextSet_ShouldNotSetSecurityContext() {
//		GatewayRequest gatewayRequest = mock(GatewayRequest.class);
//		filterAndVerifyNoSecurityContextSet(gatewayRequest);
//	}
//
//	@Test
//	public void noAuthorizerDataSet_ShouldNotSetSecurityContext() {
//		GatewayRequestContext gatewayRequestContext = mock(GatewayRequestContext.class);
//		when(gatewayRequestContext.getAuthorizer()).thenReturn(null);
//
//		GatewayRequest gatewayRequest = mock(GatewayRequest.class);
//		when(gatewayRequest.getRequestContext()).thenReturn(gatewayRequestContext);
//
//		filterAndVerifyNoSecurityContextSet(gatewayRequest);
//	}
//
//	@Test
//	public void emptyAuthorizerDataSet_ShouldNotSetSecurityContext() {
//		GatewayRequestContext gatewayRequestContext = mock(GatewayRequestContext.class);
//		when(gatewayRequestContext.getAuthorizer()).thenReturn(Collections.emptyMap());
//
//		GatewayRequest gatewayRequest = mock(GatewayRequest.class);
//		when(gatewayRequest.getRequestContext()).thenReturn(gatewayRequestContext);
//
//		filterAndVerifyNoSecurityContextSet(gatewayRequest);
//	}
//
//	protected final SecurityContext filterAndReturnSetSecurityContext(Map<String, Object> authorizerData) {
//		return filterAndReturnSetSecurityContext(createRequest(authorizerData));
//	}
//
//	protected final SecurityContext filterAndReturnSetSecurityContext(GatewayRequest gatewayRequest) {
//		ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
//		AuthorizerFilter filter = createCognitoAuthorizerFilter(gatewayRequest);
//		ArgumentCaptor<SecurityContext> securityContextCapture = ArgumentCaptor.forClass(SecurityContext.class);
//		try {
//			filter.filter(containerRequestContext);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		verify(containerRequestContext).setSecurityContext(securityContextCapture.capture());
//		return securityContextCapture.getValue();
//	}
//
//	protected final void filterAndVerifyNoSecurityContextSet(Map<String, Object> authorizerData) {
//		filterAndVerifyNoSecurityContextSet(createRequest(authorizerData));
//	}
//
//	protected final void filterAndVerifyNoSecurityContextSet(GatewayRequest gatewayRequest) {
//		ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
//		AuthorizerFilter filter = createCognitoAuthorizerFilter(gatewayRequest);
//		try {
//			filter.filter(containerRequestContext);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		verify(containerRequestContext, times(0)).setSecurityContext(any());
//	}
}
