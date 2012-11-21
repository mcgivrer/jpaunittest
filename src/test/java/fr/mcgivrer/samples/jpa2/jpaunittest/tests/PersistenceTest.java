/**
 * 
 */
package fr.mcgivrer.samples.jpa2.jpaunittest.tests;

import org.apache.derby.impl.io.VFMemoryStorageFactory;
import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLNonTransientConnectionException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

/**
 * Default persistence test Unit master class. Extracted from
 * http://www.touilleur-express.fr/2010/08/06/jpa-et-tests-dintegrations/
 * 
 * @author Frédéric Delorme<frederic.delorme@gmail.com>
 * @Since 20121121
 * @see http://www.touilleur-express.fr/2010/08/06/jpa-et-tests-dintegrations/
 * 
 */
public abstract class PersistenceTest extends TestCase {
	private static Logger logger = Logger.getLogger(PersistenceTest.class
			.getName());
	private EntityManagerFactory emFactory;
	protected EntityManager em;

	public PersistenceTest() {

	}

	public PersistenceTest(String testName) {
		super(testName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try {
			logger.info("Starting in-memory database for unit tests");
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			DriverManager.getConnection(
					"jdbc:derby:memory:unit-testing-jpa;create=true").close();
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception during database startup.");
		}
		try {
			logger.info("Building JPA EntityManager for unit tests");
			emFactory = Persistence.createEntityManagerFactory("testPU");
			em = emFactory.createEntityManager();
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("Exception during JPA EntityManager instanciation.");
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		logger.info("Shuting down Hibernate JPA layer.");
		if (em != null) {
			em.close();
		}
		if (emFactory != null) {
			emFactory.close();
		}
		logger.info("Stopping in-memory database.");
		try {
			DriverManager.getConnection(
					"jdbc:derby:memory:unit-testing-jpa;shutdown=true").close();
		} catch (SQLNonTransientConnectionException ex) {
			if (ex.getErrorCode() != 45000) {
				throw ex;
			}
			// Shutdown success
		}
		//VFMemoryStorageFactory.purgeDatabase(new File("unit-testing-jpa").getCanonicalPath());
	}

}
