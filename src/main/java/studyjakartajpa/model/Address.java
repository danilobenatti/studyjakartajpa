package studyjakartajpa.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "tbl_addresses", catalog = "jpaforbeginners", schema = "public")
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
	
	@Column(name = "col_number")
	private String number;
	
	@Column(name = "col_street")
	private String street;
	
	@Column(name = "col_unit")
	private String unit;
	
	@Column(name = "col_city")
	private String city;
	
	@Column(name = "col_state")
	private String state;
	
	@Column(name = "col_country")
	private String country;
	
	@Column(name = "col_zipcode")
	private String zipCode;
	
	@Column(name = "col_isprincipal")
	private boolean isPrincipal;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "person_id", nullable = false,
		foreignKey = @ForeignKey(name = "fk_tbl_addresses_persons_id"))
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
}
