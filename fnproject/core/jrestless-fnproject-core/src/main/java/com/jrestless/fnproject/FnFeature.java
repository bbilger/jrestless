package com.jrestless.fnproject;

import com.jrestless.core.filter.ApplicationPathFilter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class FnFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(ApplicationPathFilter.class);
		return true;
	}
}
