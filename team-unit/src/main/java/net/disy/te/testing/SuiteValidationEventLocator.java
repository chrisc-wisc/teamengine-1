package net.disy.te.testing;

import org.apache.commons.lang.Validate;

import com.occamlab.te.index.SuiteEntry;

public class SuiteValidationEventLocator implements ValidationEventLocator {

	private SuiteEntry suite;

	public SuiteValidationEventLocator(SuiteEntry suite) {
		Validate.notNull(suite);
		this.suite = suite;
	}

	@Override
	public String toString() {
		String prefix = suite.getPrefix();
		String localName = suite.getLocalName();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Suite ");
		stringBuilder.append((prefix == null ? "" : prefix + ":"));
		stringBuilder.append(":");
		stringBuilder.append(localName);
		return stringBuilder.toString();
	}

}
