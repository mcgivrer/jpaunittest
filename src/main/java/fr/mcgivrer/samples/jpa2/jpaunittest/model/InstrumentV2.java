/**
 * 
 */
package fr.mcgivrer.samples.jpa2.jpaunittest.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.sql.DriverManager;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;

/**
 * @author frederic
 * 
 */
@Entity
@NamedQuery(name = "byIsin", query = "FROM InstrumentV2 I WHERE I.isin=:pisin")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "ID", "ISIN" }))
public class InstrumentV2 implements Serializable {
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 12, name = "ISIN")
	private String isin;
	@Column(nullable = true, length = 150)
	private String description;

	@PersistenceContext
	static EntityManager em;

	static {
		System.out.println("--- Initializing context ---");
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			DriverManager.getConnection(
					"jdbc:derby:memory:unit-testing-jpa;create=true").close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			EntityManagerFactory emFactory = Persistence
					.createEntityManagerFactory("testPU");
			em = emFactory.createEntityManager();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Required for JPA2

	protected InstrumentV2() {
	}

	public InstrumentV2(final String isin) {
		this(isin, null);
	}

	public InstrumentV2(final String isin, String description) {
		checkNotNull(isin, "Isin cannot be null");
		this.isin = isin;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getIsin() {
		return isin;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", getId())
				.add("isin", getIsin()).add("description", getDescription())
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InstrumentV2 that = (InstrumentV2) o;
		return Objects.equal(that.isin, isin);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isin);
	}

	public static InstrumentV2 find(String isin) {
		Query q = em.createNamedQuery("byIsin").setParameter("pisin", isin);
		return (InstrumentV2) q.getSingleResult();
	}

	public static void persist(InstrumentV2 instrument) {
		checkNotNull(instrument);
		em.getTransaction().begin();
		em.persist(instrument);
		em.getTransaction().commit();
	}

	@SuppressWarnings("unchecked")
	public static List<InstrumentV2> findAll() {
		return em.createQuery("from InstrumentV2").getResultList();
	}
}