package net.opengis.cat.csw.v_2_0_2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.disy.te.testing.TeamSuite;
import net.disy.te.testing.TeamSuiteExecutor;

import org.junit.Test;
import org.xml.sax.SAXException;


public class Level0TeamSuiteTest {

	// @Ignore
	@Test
	public void generatesFiles() throws SAXException, IOException, Exception {
		final TeamSuite teamSuite = new Level0TeamSuite();

		final Map<String, String> params = new HashMap<String, String>();
		params.put(
				"csw.capabilities.url",
				"http://pan.pin.unifi.it:8081/GI-cat/services/cswiso?service=CSW&version=2.0.2&request=GetCapabilities");

		new TeamSuiteExecutor(teamSuite).execute(params);
	}

}
