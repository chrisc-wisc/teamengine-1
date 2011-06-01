package net.disy.te.testing.tests;

import net.disy.te.testing.DefaultValidationEvent;
import net.disy.te.testing.ValidationEvent;
import net.disy.te.testing.ValidationEventSeverity;

import org.junit.Assert;
import org.junit.Test;

public class ValidationEventTest {

	@Test
	public void printsToString() {

		{
			ValidationEvent event = new DefaultValidationEvent(
					ValidationEventSeverity.INFO, "Hello world.");
			Assert.assertEquals("<unknown> INFO: Hello world.",
					event.toString());
		}
		{
			Assert.assertNotNull(new DefaultValidationEvent(
					ValidationEventSeverity.INFO, "Hello world.",
					new Exception()).toString());
		}

	}
}
