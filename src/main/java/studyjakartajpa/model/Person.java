package studyjakartajpa.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.PrivateOwned;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Entity
@Table(name = "tbl_persons", catalog = "jpaforbeginners", schema = "public")
public class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@EqualsAndHashCode.Include
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "persons_seq_generator", sequenceName = "persons_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "persons_seq_generator")
	private long id;
	
	@NonNull
	@Column(name = "col_firstname", nullable = false, length = 150)
	private String firstname;
	
	@NonNull
	@Column(name = "col_gender", nullable = false)
	private Character gender;
	
	@Column(name = "col_weight")
	private float weight;
	
	@Column(name = "col_height")
	private float height;
	
	@PrivateOwned
	@CascadeOnDelete
	@Setter(value = AccessLevel.NONE)
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tbl_persons_phones", catalog = "jpaforbeginners",
		schema = "public",
		joinColumns = @JoinColumn(name = "person_id", table = "tbl_persons",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_tbl_persons_phones_person_id",
				foreignKeyDefinition = "FOREIGN KEY (person_id) REFERENCES jpaforbeginners.public.tbl_persons (ID)")))
	@MapKeyColumn(name = "col_type", columnDefinition = "char(1)",
		nullable = false)
	@Column(name = "col_number", length = 20, nullable = false)
	private Map<Character, String> phones = new HashMap<>();
	
	public String setPhone(Character type, String number) {
		return this.phones.put(type, number);
	}
	
	public void setPhones(Map<Character, String> phones) {
		this.phones.putAll(phones);
	}
	
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", cascade = CascadeType.ALL,
		fetch = FetchType.EAGER)
	private List<Address> addresses = new ArrayList<>();
	
	public boolean setAddress(Address address) {
		address.setPerson(this);
		return this.addresses.add(address);
	}
	
	public void setAddresses(Address... addresses) {
		this.addresses.addAll(List.of(addresses));
		List.of(addresses).forEach(a -> a.setPerson(this));
		
	}
	
	public Address getPrincipalAddress() {
		Optional<Address> address = this.addresses.stream()
				.filter(Address::isPrincipal).findFirst();
		if (address.isPresent()) {
			return address.get();
		}
		return null;
	}
	
	@NonNull
	@Column(name = "col_birthday", nullable = false)
	private LocalDate birthdate;
	
	@Column(name = "col_deathdate")
	private LocalDate deathdate;
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateinsert", columnDefinition = "timestamp",
		updatable = false)
	private LocalDateTime dateCreate;
	
	@jakarta.persistence.PrePersist
	protected void prePersist() {
		this.dateCreate = LocalDateTime.now();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateupdate", columnDefinition = "timestamp",
		insertable = false)
	private LocalDateTime dateUpdate;
	
	@jakarta.persistence.PreUpdate
	protected void preUpdate() {
		this.dateUpdate = LocalDateTime.now();
	}
	
	@Builder(setterPrefix = "with")
	public static Person of(String name, Character gender, LocalDate birth) {
		Person person = new Person();
		person.setFirstname(name);
		person.setGender(gender);
		person.setBirthdate(birth);
		return person;
		
	}
	
	@Builder(setterPrefix = "with")
	public static Person of(String name, Character gender, LocalDate birth,
			Map<Character, String> phones) {
		Person person = new Person();
		person.setFirstname(name);
		person.setGender(gender);
		person.setBirthdate(birth);
		person.setPhones(phones);
		return person;
		
	}
	
}
