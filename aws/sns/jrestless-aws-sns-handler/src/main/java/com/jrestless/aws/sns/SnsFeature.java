/*
 * Copyright 2016 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.aws.sns;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;

import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.jrestless.aws.AwsFeature;
import com.jrestless.core.container.dpi.AbstractReferencingBinder;

/**
 * Binds SNS specific values.
 *
 * <table border="1" summary="injected objects">
 * <tr>
 * <th>injectable object
 * <th>proxiable
 * <th>scope
 * </tr>
 *
 * <tr>
 * <td>{@link SNSRecord}
 * <td>true
 * <td>request
 * </tr>
 * </table>
 *
 * Registers {@link AwsFeature}.
 *
 * @author Bjoern Bilger
 *
 */
public class SnsFeature implements Feature {

	public static final Type SNS_RECORD_TYPE = (new TypeLiteral<Ref<SNSRecord>>() { }).getType();

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new Binder());
		context.register(AwsFeature.class);
		return true;
	}

	private static class Binder extends AbstractReferencingBinder {
		@Override
		protected void configure() {
			bindReferencingFactory(SNSRecord.class, ReferencingSnsRecordFactory.class,
					new TypeLiteral<Ref<SNSRecord>>() { });
		}
	}

	private static class ReferencingSnsRecordFactory extends ReferencingFactory<SNSRecord> {
		@Inject
		ReferencingSnsRecordFactory(final Provider<Ref<SNSRecord>> referenceFactory) {
			super(referenceFactory);
		}
	}
}
