package net.opengis.cat.csw.v_2_0_2.profiles.apiso.v_1_0_0.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.disy.te.testing.AssertingValidationEventHandler;
import net.disy.te.testing.TeamSuite;
import net.disy.te.testing.TeamSuiteExecutor;
import net.opengis.cat.csw.v_2_0_2.profiles.apiso.v_1_0_0.Level1TeamSuite;

import org.junit.Test;
import org.xml.sax.SAXException;

public class RunLevel1TeamSuite {

	// @Ignore
	@Test
	public void generatesFiles() throws SAXException, IOException, Exception {
		final TeamSuite teamSuite = new Level1TeamSuite();

		final Map<String, String> params = new HashMap<String, String>();
		params.put(
				"csw.capabilities.url",
				"http://gdi-de.sdisuite.de:8080/soapServices/CSWStartup?request=GetCapabilities&service=CSW");

		new TeamSuiteExecutor(teamSuite,
				AssertingValidationEventHandler.INSTANCE).execute(params);
	}

}
