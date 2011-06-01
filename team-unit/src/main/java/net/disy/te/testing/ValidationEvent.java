package net.disy.te.testing;

public interface ValidationEvent {

	public ValidationEventSeverity getSeverity();

	public String getMessage();

	public Throwable getException();

	public ValidationEventLocator getLocator();

}
