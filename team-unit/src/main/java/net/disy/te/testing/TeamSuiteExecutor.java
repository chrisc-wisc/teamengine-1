package net.disy.te.testing;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.disy.te.io.FileUtils;

import org.apache.commons.lang.Validate;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;

public class TeamSuiteExecutor {

	private final File workingDir;
	private final TeamSuite teamSuite;
	private final Index index;

	public TeamSuiteExecutor(TeamSuite teamSuite) throws Exception {
		Validate.notNull(teamSuite);
		this.teamSuite = teamSuite;
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

	}

}
