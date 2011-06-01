package net.disy.te.testing;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

import org.apache.commons.lang.Validate;

public class DefaultValidationEvent implements ValidationEvent {

	private final ValidationEventLocator locator;
	private final ValidationEventSeverity severity;
	private final String message;
	private final Throwable exception;

	public DefaultValidationEvent(String message) {
		this(null, ValidationEventSeverity.ERROR, message, null);
	}

	public DefaultValidationEvent(String message, Throwable exception) {
		this(null, ValidationEventSeverity.ERROR, message, exception);
	}

	public DefaultValidationEvent(ValidationEventSeverity severity,
			String message) {
		this(null, severity, message, null);
	}

	public DefaultValidationEvent(ValidationEventSeverity severity,
			String message, Throwable exception) {
		this(null, severity, message, exception);
	}

	public DefaultValidationEvent(ValidationEventLocator locator,
			ValidationEventSeverity severity, String message) {
		this(locator, severity, message, null);
	}

	public DefaultValidationEvent(ValidationEventLocator locator,
			ValidationEventSeverity severity, String message,
			Throwable exception) {
		Validate.notNull(severity);
		Validate.notNull(message);
		this.locator = locator;
		this.message = message;
		this.exception = exception;
		this.severity = severity;
	}

	public ValidationEventLocator getLocator() {
		return locator;
	}

	public ValidationEventSeverity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getException() {
		return exception;
	}

	@Override
	public String toString() {

		final ValidationEventLocator locator = getLocator();
		final String locatorAsString = locator == null ? "unknown" : locator
				.toString();
		final String severityAsString = getSeverity().toString();
		final String exceptionAsString;
		if (exception == null) {
			exceptionAsString = "";
		} else {
			final StringWriter sw = new StringWriter();
			exception.printStackTrace(new PrintWriter(sw));
			exceptionAsString = "\n" + sw.toString();
		}

		final String message = getMessage();

		return MessageFormat.format("<{0}> {1}: {2}{3}", locatorAsString,
				severityAsString, message, exceptionAsString);
	}
}
