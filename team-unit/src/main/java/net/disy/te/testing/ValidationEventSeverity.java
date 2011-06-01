package net.disy.te.testing;

public enum ValidationEventSeverity {

	INFO {

		@Override
		public <T, E extends Exception> T accept(
				ValidationEventSeverityVisitor<T, E> visitor) throws E {
			return visitor.visitInfo(this);
		}

	},
	WARN {
		@Override
		public <T, E extends Exception> T accept(
				ValidationEventSeverityVisitor<T, E> visitor) throws E {
			return visitor.visitWarn(this);
		}

	},
	ERROR {
		@Override
		public <T, E extends Exception> T accept(
				ValidationEventSeverityVisitor<T, E> visitor) throws E {
			return visitor.visitError(this);
		}

	};

	public abstract <T, E extends Exception> T accept(
			ValidationEventSeverityVisitor<T, E> visitor) throws E;

	public static interface ValidationEventSeverityVisitor<T, E extends Exception> {
		public T visitInfo(ValidationEventSeverity severity) throws E;

		public T visitWarn(ValidationEventSeverity severity) throws E;

		public T visitError(ValidationEventSeverity severity) throws E;

	}
}
