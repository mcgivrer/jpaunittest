/**
 * 
 */
package fr.mcgivrer.samples.jpa2.jpaunittest.tests;

import fr.mcgivrer.samples.jpa2.jpaunittest.model.Instrument;

/**
 * A simple JPA integration test to demonstrate equals and hashCode issues.
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @since 2012-11-21
 */
public class InstrumentJPATest extends PersistenceTest {
	public void testPersistence() {
		try {
			em.getTransaction().begin();

			Instrument instrument = new Instrument("FR1234567890");

			em.persist(instrument);
			assertTrue(em.contains(instrument));

			em.remove(instrument);
			assertFalse(em.contains(instrument));

			em.getTransaction().commit();

		} catch (Exception ex) {
			em.getTransaction().rollback();
			ex.printStackTrace();
			fail("Exception during testPersistence");
		}
	}

}