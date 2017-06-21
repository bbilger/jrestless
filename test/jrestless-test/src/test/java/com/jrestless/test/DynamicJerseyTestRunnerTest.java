package com.jrestless.test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.jrestless.test.DynamicJerseyTestRunner.ThrowingConsumer;

public class DynamicJerseyTestRunnerTest {

	private DynamicJerseyTestRunner runner = new DynamicJerseyTestRunner();
	private final JerseyTest jerseyTest = mock(JerseyTest.class);
	@SuppressWarnings("unchecked")
	private final ThrowingConsumer<JerseyTest> consumer = mock(ThrowingConsumer.class);

	@Test
	public void testTearsDownTestOnSetupFailure() throws Exception {
		doThrow(new RuntimeException()).when(jerseyTest).setUp();
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
		}
		verify(jerseyTest).setUp();
		verify(jerseyTest).tearDown();
	}

	@Test
	public void testRethrowsExceptionOnStartupFailure() throws Exception {
		RuntimeException thrownException = new RuntimeException("whatever");
		doThrow(thrownException).when(jerseyTest).setUp();
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
			assertSame(thrownException, re.getCause());
		}
	}

	@Test
	public void testDoesNotCallConsumerOnStartupFailure() throws Exception {
		doThrow(new RuntimeException()).when(jerseyTest).setUp();
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
		}
		verifyZeroInteractions(consumer);;
	}

	@Test
	public void testRethrowsExceptionOnTearDownFailure() throws Exception {
		RuntimeException thrownException = new RuntimeException("whatever");
		doThrow(thrownException).when(jerseyTest).tearDown();
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (RuntimeException re) {
			assertSame(thrownException, re.getCause());
		}
	}

	@Test
	public void testCallSetupConsumerAndTearDownInOrder() throws Exception {
		runner.runJerseyTest(jerseyTest, consumer);
		InOrder inOrder = Mockito.inOrder(jerseyTest, consumer);
		inOrder.verify(jerseyTest).setUp();
		inOrder.verify(consumer).accept(jerseyTest);
		inOrder.verify(jerseyTest).tearDown();
	}

	@Test
	public void testPassesJerseyTestToConsumer() throws Exception {
		runner.runJerseyTest(jerseyTest, consumer);
		verify(consumer).accept(jerseyTest);
	}

	@Test
	public void testCallsTearDownOnConsumerException() throws Exception {
		doThrow(new Exception("whatever")).when(consumer).accept(jerseyTest);
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (Exception e) {
			;
		}
		verify(jerseyTest).tearDown();
	}

	@Test
	public void testRethrowsExceptionOnConsumerFailure() throws Exception {
		Exception thrownException = new Exception("whatever");
		doThrow(thrownException).when(consumer).accept(jerseyTest);
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (Exception e) {
			assertSame(thrownException, e);
		}
	}

	@Test
	public void testRethrowsTearDownExceptionOnConsumerAndTearDownFailure() throws Exception {
		Exception consumerException = new Exception("whatever");
		doThrow(consumerException).when(consumer).accept(jerseyTest);
		RuntimeException tearDownException = new RuntimeException("whatever");
		doThrow(tearDownException).when(jerseyTest).tearDown();
		try {
			runner.runJerseyTest(jerseyTest, consumer);
			fail("expected exception to be thrown");
		} catch (Exception e) {
			assertSame(tearDownException, e.getCause());
		}
	}
}
