/**
 * 
 */
package fr.mcgivrer.samples.jpa2.jpaunittest.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.base.Objects;

/**
 * @author frederic
 * 
 */
@Entity
public class Instrument implements Serializable {
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, length = 12)
	private String isin;
	@Column(nullable = true, length = 150)
	private String description;

	// Required for JPA2

	protected Instrument() {
	}

	public Instrument(final String isin) {
		this(isin, null);
	}

	public Instrument(final String isin, final String description) {
		if (isin == null) {
			throw new NullPointerException("ISIN cannot be null");
		}
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
		// Google Guava
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
		Instrument that = (Instrument) o;
		return Objects.equal(that.isin, isin); // Google Guava
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(isin); // Google Guava
	}
}
