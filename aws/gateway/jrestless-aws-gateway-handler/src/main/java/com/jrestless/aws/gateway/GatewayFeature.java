package com.jrestless.aws.gateway;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import com.jrestless.aws.gateway.dpi.GatewayRequestContextFactory;
import com.jrestless.aws.gateway.io.GatewayRequest;

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
 * </table>
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
			}
		});
		return true;
	}
}
