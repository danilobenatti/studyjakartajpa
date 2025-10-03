package studyjakartajpa.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.PrivateOwned;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
			referencedColumnName = "id"))
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
	
	@PrivateOwned
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", fetch = FetchType.EAGER,
		orphanRemoval = true, cascade = CascadeType.ALL)
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
		if (address.isPresent())
			return address.get();
		return null;
	}
	
	@PrivateOwned
	@CascadeOnDelete
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "tbl_persons_emails", catalog = "jpaforbeginners",
		schema = "public", joinColumns = @JoinColumn(name = "person_id"))
	@Column(name = "col_email", length = 150, nullable = false)
	private Set<String> emails = new HashSet<>();
	
	public void setEmail(String email) {
		this.emails.add(email);
	}
	
	public void setEmails(Set<String> emails) {
		this.emails.addAll(emails);
	}
	
	public void setEmails(String... email) {
		Set.of(email).forEach(this.emails::add);
	}
	
	@NonNull
	@Column(name = "col_birthday", nullable = false)
	private LocalDate birthdate;
	
	@Column(name = "col_deathdate")
	private LocalDate deathdate;
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateinsert", updatable = false,
		columnDefinition = "timestamp with time zone")
	private LocalDateTime dateCreate;
	
	@jakarta.persistence.PrePersist
	protected void prePersist() {
		this.dateCreate = LocalDateTime.now();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateupdate", insertable = false,
		columnDefinition = "timestamp with time zone")
	private LocalDateTime dateUpdate;
	
	@jakarta.persistence.PreUpdate
	protected void preUpdate() {
		this.dateUpdate = LocalDateTime.now();
	}
	
	@Builder(setterPrefix = "with")
	public static Person of(String firstname, Character gender,
			LocalDate birthdate) {
		Person person = new Person();
		person.setFirstname(firstname);
		person.setGender(gender);
		person.setBirthdate(birthdate);
		return person;
		
	}
	
	@Builder(setterPrefix = "with")
	public static Person of(String firstname, Character gender,
			LocalDate birthdate, Map<Character, String> phones) {
		Person person = new Person();
		person.setFirstname(firstname);
		person.setGender(gender);
		person.setBirthdate(birthdate);
		person.setPhones(phones);
		return person;
		
	}
	
	@Builder(setterPrefix = "with")
	public static Person of(String firstname, Character gender,
			LocalDate birthdate, Map<Character, String> phones,
			Set<String> emails) {
		Person person = new Person();
		person.setFirstname(firstname);
		person.setGender(gender);
		person.setBirthdate(birthdate);
		person.setPhones(phones);
		person.setEmails(emails);
		return person;
		
	}
	
	public boolean isAlive() {
		return getDeathdate() == null && getBirthdate() != null;
	}
	
	public Character getSymbol() {
		/**
		 * '\u2605' BLACK STAR '\u271D' LATIN CROSS '\u272E' HEAVY OUTLINED
		 * BLACK STAR '\u271F' OUTLINED LATIN CROSS
		 **/
		return isAlive() ? '\u272E' : '\u271F';
	}
	
	public int getAge() {
		if (isAlive() && getBirthdate().isBefore(LocalDate.now()))
			return Period.between(getBirthdate(), LocalDate.now()).getYears();
		else
			return Period.between(getBirthdate(), getDeathdate()).getYears();
	}
	
	public String getAgeWithSymbol() {
		return StringUtils.join(getAge(), getSymbol());
	}
	
	public boolean validyDeathDate(LocalDate deathdate) {
		return deathdate.isAfter(getBirthdate());
	}
	
	public void personDiedNow() {
		if (isAlive() && validyDeathDate(LocalDate.now()))
			setDeathdate(LocalDate.now());
	}
	
	public void personDiedIn(LocalDate deathDate) {
		if (isAlive() && validyDeathDate(deathDate))
			setDeathdate(deathDate);
	}
	
	public void personDiedIn(Date deathDate) {
		ZoneId zoneId = ZoneId.systemDefault();
		LocalDate date = deathDate.toInstant().atZone(zoneId).toLocalDate();
		personDiedIn(date);
	}
	
	public void personDiedIn(Date deathDate, ZoneId zone) {
		LocalDate date = deathDate.toInstant().atZone(zone).toLocalDate();
		personDiedIn(date);
	}
	
}
