/**
 * 
 */
package fr.mcgivrer.samples.jpa2.jpaunittest.tests;

import java.util.List;

import junit.framework.TestCase;
import fr.mcgivrer.samples.jpa2.jpaunittest.model.InstrumentV2;

/**
 * @author frederic
 * 
 */

public class InstrumentV2Test extends TestCase {

	public void testShouldCreateAndFindAnEntity() {

		InstrumentV2 instrument = new InstrumentV2("FR123456");
		InstrumentV2 instrument2 = new InstrumentV2("US94394949");

		InstrumentV2.persist(instrument);
		InstrumentV2.persist(instrument2);

		List<InstrumentV2> results = InstrumentV2.findAll();
		assertEquals(2, results.size());

		assertEquals(instrument, InstrumentV2.find("FR123456"));

	}

}
