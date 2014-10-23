package de.mklinger.commons.exec;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class ErrorHandlingRunnableTest {
	@Test
	public void testNoError() {
		final ErrorHandlingRunnable r = new ErrorHandlingRunnable() {
			@Override
			protected void doRun() throws Exception {
				// do nothing
			}
		};
		r.run();
		Assert.assertNull(r.getError());
	}

	@Test
	public void testWithError() {
		final Exception e = new RuntimeException("test");
		final ErrorHandlingRunnable r = new ErrorHandlingRunnable() {
			@Override
			protected void doRun() throws Exception {
				throw e;
			}
		};
		r.run();
		Assert.assertSame(e, r.getError());
	}
}
