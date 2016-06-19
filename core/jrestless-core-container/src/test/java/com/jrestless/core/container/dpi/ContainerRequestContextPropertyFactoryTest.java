package com.jrestless.core.container.dpi;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;

import org.junit.Test;

public class ContainerRequestContextPropertyFactoryTest {

	@Test
	public void provide_ShouldProvidePropertyWithNameFromRequestContext() {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		SomeObject property = mock(SomeObject.class);
		when(requestContext.getProperty("propertyName")).thenReturn(property);
		assertSame(property, new ContainerRequestContextPropertyFactoryImpl(requestContext).provide());
		verify(requestContext, times(1)).getProperty("propertyName");
		verifyNoMoreInteractions(requestContext);
		verifyZeroInteractions(property);
	}

	@Test
	public void dispose_ShouldDoNothing() {
		ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
		SomeObject property = mock(SomeObject.class);
		new ContainerRequestContextPropertyFactoryImpl(requestContext).dispose(property);
		verifyZeroInteractions(property);
		verifyZeroInteractions(requestContext);
	}

	private static class ContainerRequestContextPropertyFactoryImpl extends ContainerRequestContextPropertyFactory<SomeObject> {

		public ContainerRequestContextPropertyFactoryImpl(ContainerRequestContext context) {
			super(context);
		}

		@Override
		protected String getPropertyName() {
			return "propertyName";
		}
	}

	private interface SomeObject {

	}

}
