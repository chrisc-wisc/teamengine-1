package net.disy.te.testing;

import net.disy.te.testing.ValidationEventSeverity.ValidationEventSeverityVisitor;

public class AssertingValidationEventHandler implements ValidationEventHandler {

	public static final ValidationEventHandler INSTANCE = new AssertingValidationEventHandler();

	@Override
	public boolean handleEvent(final ValidationEvent event) {
		return event
				.getSeverity()
				.accept(new ValidationEventSeverityVisitor<Boolean, RuntimeException>() {

					@Override
					public Boolean visitInfo(ValidationEventSeverity severity)
							throws RuntimeException {
						return true;
					}

					@Override
					public Boolean visitWarn(ValidationEventSeverity severity)
							throws RuntimeException {
						throw new AssertionError(event.toString());
					}

					@Override
					public Boolean visitError(ValidationEventSeverity severity)
							throws RuntimeException {
						throw new AssertionError(event.toString());
					}

				});
	}

}
