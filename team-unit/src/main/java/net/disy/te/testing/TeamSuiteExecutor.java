package net.disy.te.testing;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.disy.te.io.FileUtils;
import net.disy.te.testing.ValidationEventSeverity.ValidationEventSeverityVisitor;

import org.apache.commons.lang.Validate;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;

public class TeamSuiteExecutor {

	private final ValidationEventHandler validationEventHandler;
	private final File workingDir;
	private final TeamSuite teamSuite;
	private final Index index;

	public TeamSuiteExecutor(TeamSuite teamSuite) throws Exception {
		this(teamSuite, new DefaultValidationEventHandler());
	}

	public TeamSuiteExecutor(TeamSuite teamSuite,
			ValidationEventHandler validationEventHandler) throws Exception {
		Validate.notNull(teamSuite);
		this.teamSuite = teamSuite;
		this.validationEventHandler = validationEventHandler;
		this.workingDir = FileUtils.createTemporaryDirectory("cite");
		workingDir.mkdir();

		final Map<URL, File> sources = new LinkedHashMap<URL, File>();

		final ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		for (String resourceName : teamSuite.getResourceNames()) {
			final URL url = classLoader.getResource(resourceName);

			final File file = new File(workingDir, URLEncoder.encode(
					resourceName, "UTF-8"));
			file.mkdirs();
			sources.put(url, file);
		}
		this.index = new Generator().generateIndex(workingDir,
				sources.entrySet(), true);
	}

	public void execute(Map<String, String> params) throws Exception {

		final RuntimeOptions runtimeOptions = new RuntimeOptions();
		runtimeOptions.setWorkDir(workingDir);
		runtimeOptions.setSessionId("s0001");

		for (Entry<String, String> entry : params.entrySet()) {
			runtimeOptions.addParam(entry.getKey() + "=" + entry.getValue());
		}

		TEClassLoader cl = new TEClassLoader(null);
		Engine engine = new Engine(index, "default", cl);
		TECore core = new TECore(engine, index, runtimeOptions);
		core.execute();

		for (String suiteName : index.getSuiteKeys()) {
			final SuiteEntry suite = index.getSuite(suiteName);
			boolean _continue = executeSuite(core, suite,
					runtimeOptions.getParams());
			// Should we continue processing?
			if (!_continue) {
				break;
			}
		}

	}

	public boolean executeSuite(TECore core, SuiteEntry suite,
			List<String> params) throws Exception {
		List<String> kvps = new ArrayList<String>(params);
		kvps.addAll(params);
		String name = suite.getPrefix() + ":" + suite.getLocalName();
		System.out.println("Testing suite " + name + "...");
		int result = core.execute_test(suite.getStartingTest().toString(),
				kvps, null);
		if (result == TECore.FAIL || result == TECore.INHERITED_FAILURE) {
			return this.validationEventHandler
					.handleEvent(new DefaultValidationEvent(
							new SuiteValidationEventLocator(suite),
							ValidationEventSeverity.ERROR, "Suite "
									+ suite.getPrefix() + ":"
									+ suite.getLocalName() + " failed."));
		} else {
			return this.validationEventHandler
					.handleEvent(new DefaultValidationEvent(
							new SuiteValidationEventLocator(suite),
							ValidationEventSeverity.INFO, "Suite "
									+ suite.getPrefix() + ":"
									+ suite.getLocalName() + " passed."));
		}
	}

	private static class DefaultValidationEventHandler implements
			ValidationEventHandler {
		@Override
		public boolean handleEvent(final ValidationEvent event) {
			final ValidationEventLocator locator = event.getLocator();
			final String location = locator == null ? "<unknown>" : locator
					.toString();

			return event
					.getSeverity()
					.accept(new ValidationEventSeverityVisitor<Boolean, RuntimeException>() {

						@Override
						public Boolean visitInfo(
								ValidationEventSeverity severity)
								throws RuntimeException {
							System.out.println(location + " passed.");
							return true;
						}

						@Override
						public Boolean visitWarn(
								ValidationEventSeverity severity)
								throws RuntimeException {
							System.out.println(location + " failed.");
							return true;
						}

						@Override
						public Boolean visitError(
								ValidationEventSeverity severity)
								throws RuntimeException {
							System.out.println(location + " failed.");
							return true;
						}

					});
		}
	}

}
