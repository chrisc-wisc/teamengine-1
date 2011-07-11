
package net.opengis.cat.csw.v_2_0_2;

import net.disy.te.testing.AbstractTeamSuite;

public class Level0TeamSuite extends AbstractTeamSuite {

	public Level0TeamSuite() {
//		super("ctl", new String[]{Constants.SUITE_CTL_NAME, Constants.PARSERS_CTL_NAME,
//				Constants.LEVEL1_CTL_NAME});
		super("ctl", new String[]{"all.xml"});
	}

}
