package com.jrestless.aws.gateway;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.jrestless.aws.gateway.dpi.GatewayIdentityContextFactory;
import com.jrestless.aws.gateway.dpi.GatewayRequestContextContextFactory;
import com.jrestless.aws.gateway.dpi.GatewayRequestContextFactory;
import com.jrestless.aws.gateway.io.GatewayIdentity;
import com.jrestless.aws.gateway.io.GatewayRequest;
import com.jrestless.aws.gateway.io.GatewayRequestContext;

/**
 * Binds Gateway specific values.
 *
 * <table border="1" summary="injected objects">
 * <tr>
 * <th>injectable object
 * <th>proxiable
 * <th>scope
 * <th>factory
 * </tr>
 *
 * <tr>
 * <td>{@link GatewayRequest}
 * <td>true
 * <td>request
 * <td>{@link GatewayRequestContextFactory}
 * </tr>
 *
 * <tr>
 * <td>{@link GatewayRequestContext}
 * <td>false
 * <td>request
 * <td>{@link GatewayRequestContextContextFactory}
 * </tr>
 *
 * <tr>
 * <td>{@link GatewayIdentity}
 * <td>false
 * <td>request
 * <td>{@link GatewayIdentityContextFactory}
 * </tr>
 * </table>
 *
 * Note: GatewayRequestContext and GatewayIdentity are not proxiable since they are
 * not necessarily available for each request.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayFeature implements Feature {
	@Override
	public boolean configure(FeatureContext context) {
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(GatewayRequestContextFactory.class)
					.to(GatewayRequest.class)
					.proxy(true)
					.proxyForSameScope(false)
					.in(RequestScoped.class);
				bindFactory(GatewayRequestContextContextFactory.class)
					.to(GatewayRequestContext.class)
					.proxy(false)
					.in(RequestScoped.class);
				bindFactory(GatewayIdentityContextFactory.class)
					.to(GatewayIdentity.class)
					.proxy(false)
					.in(RequestScoped.class);
			}
		});
		return true;
	}
}
