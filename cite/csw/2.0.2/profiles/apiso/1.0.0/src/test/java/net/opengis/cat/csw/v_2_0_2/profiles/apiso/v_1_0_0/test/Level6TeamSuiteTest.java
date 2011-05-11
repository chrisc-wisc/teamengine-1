package net.opengis.cat.csw.v_2_0_2.profiles.apiso.v_1_0_0.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.disy.te.testing.TeamSuite;
import net.disy.te.testing.TeamSuiteExecutor;
import net.opengis.cat.csw.v_2_0_2.profiles.apiso.v_1_0_0.Level6TeamSuite;

import org.junit.Test;
import org.xml.sax.SAXException;


public class Level6TeamSuiteTest {

	// @Ignore
	@Test
	public void generatesFiles() throws SAXException, IOException, Exception {
		final TeamSuite teamSuite = new Level6TeamSuite();

		final Map<String, String> params = new HashMap<String, String>();
		params.put(
				"csw.capabilities.url",
				"http://gdi-de.sdisuite.de:8080/soapServices/CSWStartup?request=GetCapabilities&service=CSW");

		new TeamSuiteExecutor(teamSuite).execute(params);
	}

}
