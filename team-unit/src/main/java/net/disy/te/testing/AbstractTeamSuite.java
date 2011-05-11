package net.disy.te.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

public abstract class AbstractTeamSuite implements TeamSuite {

	private final List<String> resourceNames;

	public AbstractTeamSuite(String... localResourceNames) {
		Validate.noNullElements(localResourceNames);

		final String packageName = getClass().getPackage().getName();
		final String packageLocation = packageName.replace('.', '/');
		final List<String> draftResourceNames = new ArrayList<String>(
				localResourceNames.length);

		for (String localResourceName : localResourceNames) {
			draftResourceNames.add(packageLocation + '/' + localResourceName);
		}
		this.resourceNames = Collections.unmodifiableList(draftResourceNames);
	}

	@Override
	public List<String> getResourceNames() {
		return resourceNames;
	}
}
