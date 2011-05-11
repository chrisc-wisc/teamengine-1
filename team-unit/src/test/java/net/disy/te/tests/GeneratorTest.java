package net.disy.te.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import net.opengis.cat.csw.v_2_0_2.profiles.apiso.v_1_0_0.Level6TeamSuite;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.TeamSuite;
import com.occamlab.te.index.Index;

public class GeneratorTest {

//	@Ignore
	@Test
	public void generatesFiles() throws SAXException, IOException, Exception {
		final File workingDir = net.disy.te.io.FileUtils
				.createTemporaryDirectory("cite");
		workingDir.mkdir();

		final Map<URL, File> sources = new LinkedHashMap<URL, File>();

		final ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		final TeamSuite teamSuite = new Level6TeamSuite();

		for (String resourceName : teamSuite.getResourceNames()) {
			final URL url = classLoader.getResource(resourceName);

			final File file = new File(workingDir, URLEncoder.encode(
					resourceName, "UTF-8"));
			file.mkdirs();
			sources.put(url, file);
		}
		final Index index = new Generator().generateIndex(workingDir,
				sources.entrySet(), true);

		RuntimeOptions runtimeOptions = new RuntimeOptions();
		runtimeOptions.setWorkDir(workingDir);
		runtimeOptions.setSessionId("s0001");
		runtimeOptions
				.addParam("csw.capabilities.url=http://gdi-de.sdisuite.de:8080/soapServices/CSWStartup?request=GetCapabilities&service=CSW");

		TEClassLoader cl = new TEClassLoader(null);
		Engine engine = new Engine(index, "default", cl);
		TECore core = new TECore(engine, index, runtimeOptions);
		core.execute();
	}

}
