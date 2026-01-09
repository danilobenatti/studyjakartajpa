package studyjakartajpa.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "addresses", catalog = "jpaforbeginners", schema = "public")
public class Address implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@EqualsAndHashCode.Include
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "addresses_seq_generator", sequenceName = "addresses_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "addresses_seq_generator")
	private long id;
	
	@Column(name = "number")
	private String number;
	
	@Column(name = "street")
	private String street;
	
	@Column(name = "unit")
	private String unit;
	
	@Column(name = "city")
	private String city;
	
	@Column(name = "state")
	private String state;
	
	@Column(name = "country")
	private String country;
	
	@Column(name = "zipcode")
	private String zipCode;
	
	@Column(name = "isprincipal")
	private boolean isPrincipal;
	
	@ManyToOne
	@JoinColumn(name = "person_id", referencedColumnName = "id",
		nullable = false)
	private Person person;
	
	@Builder(setterPrefix = "with")
	public static Address of(String number, String street, String unit,
			String city, String state, String country, String zipCode,
			boolean isPrincipal, Person person) {
		Address address = new Address();
		address.setNumber(number);
		address.setStreet(street);
		address.setUnit(unit);
		address.setCity(city);
		address.setState(state);
		address.setCountry(country);
		address.setZipCode(zipCode);
		address.setPrincipal(isPrincipal);
		address.setPerson(person);
		return address;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("number", getNumber()).append("street", getStreet())
				.append("unit", getUnit()).append("city", getCity())
				.append("state", getState()).append("country", getCountry())
				.append("zipCode", getZipCode())
				.append("principal", isPrincipal()).build();
	}
}
