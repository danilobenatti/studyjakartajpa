package studyjakartajpa.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.PrivateOwned;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import studyjakartajpa.util.Imc;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true, builderMethodName = "maker",
	buildMethodName = "done")
@Entity
@EntityListeners(value = { AuditListener.class })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "peType",
	discriminatorType = DiscriminatorType.CHAR, length = 1)
@DiscriminatorValue(value = "P")
@Table(name = "persons", catalog = "jpaforbeginners", schema = "public")
@NamedNativeQuery(name = "Persons.findAllLive", query = """
	select * from persons p where p.deathdate is null limit ?1 offset ?2
	""", resultClass = Person.class)
public class Person implements Serializable {
	
	static LocalDate now = LocalDate.now();
	
	static Locale locale = Locale.getDefault();
	
	static final char BLACK_STAR = '\u272E'; // Heavy Outlined Black Star
	
	static final char LATIN_CROSS = '\u271F'; // Outlined Latin Cross
	
	final transient DateTimeFormatter dtf = DateTimeFormatter
			.ofPattern("d/MMM/yyyy", locale);
	
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
	@Column(name = "firstname", nullable = false, length = 150)
	private String firstname;
	
	@NonNull
	@Column(name = "gender", nullable = false)
	private Character gender;
	
	void toUpperCaseGender() {
		this.gender = Character.toUpperCase(this.gender);
	}
	
	@Builder.Default
	@Column(name = "weight", columnDefinition = "NUMERIC(3,1) default 0.0")
	private float weight = 0.0F;
	
	@Builder.Default
	@Column(name = "height", columnDefinition = "NUMERIC(3,2) default 0.0")
	private float height = 0.0F;
	
	@OneToOne
	@JoinColumn(name = "partner_id", referencedColumnName = "id",
		nullable = true,
		foreignKey = @ForeignKey(name = "FK_persons_partner_id",
			foreignKeyDefinition = "FOREIGN KEY (partner_id) REFERENCES public.persons(ID) ON DELETE SET NULL"))
	private Person partner;
	
	public void setPartner(Person person) {
		this.partner = person;
		person.partner = this.partner;
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "persons_phones", catalog = "jpaforbeginners",
		schema = "public",
		joinColumns = @JoinColumn(name = "person_id", table = "persons",
			referencedColumnName = "id", nullable = false))
	@MapKeyColumn(name = "phType", columnDefinition = "char(1)",
		nullable = false)
	@Column(name = "phoneNumber", length = 20, nullable = false)
	private Map<Character, String> phones = new HashMap<>();
	
	public String setPhone(Character type, String number) {
		if (isAlive())
			return this.phones.put(type, number);
		return null;
	}
	
	public void setPhones(Map<Character, String> phones) {
		if (isAlive())
			this.phones.putAll(phones);
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", fetch = FetchType.EAGER,
		orphanRemoval = true, cascade = CascadeType.ALL)
	private List<Address> addresses = new ArrayList<>();
	
	public boolean setAddress(Address address) {
		address.setPerson(this);
		return this.addresses.add(address);
	}
	
	public boolean setAddresses(List<Address> addresses) {
		addresses.forEach(a -> a.setPerson(this));
		return this.addresses.addAll(addresses);
	}
	
	public boolean setAddresses(Address... addresses) {
		List.of(addresses).forEach(a -> a.setPerson(this));
		return this.addresses.addAll(List.of(addresses));
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "persons_emails", catalog = "jpaforbeginners",
		schema = "public", joinColumns = @JoinColumn(name = "person_id"))
	@Column(name = "email", length = 150, nullable = false)
	private Set<String> emails = new HashSet<>();
	
	public void setEmail(String email) {
		this.emails.add(email);
	}
	
	public void setEmails(Set<String> emails) {
		this.emails.addAll(emails);
	}
	
	public void setEmails(String... emails) {
		Set.of(emails).forEach(this.emails::add);
	}
	
	@Builder.Default
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", fetch = FetchType.EAGER,
		orphanRemoval = true, cascade = CascadeType.REMOVE)
	private Set<WishList> wishLists = new HashSet<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
	private List<Order> orders = new ArrayList<>();
	
	public void setOrder(Order order) {
		this.orders.add(order);
	}
	
	public void setOrders(Order... orders) {
		List.of(orders).forEach(this::setOrder);
	}
	
	@NonNull
	@Column(name = "birthdate", nullable = false)
	private LocalDate birthdate;
	
	@Column(name = "deathdate")
	private LocalDate deathdate;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateupdate", insertable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
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
	
	@Override
	public String toString() {
		var sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		sb.append("id", this.getId());
		sb.append("name", this.getFirstname());
		sb.append("gender", this.getGender());
		sb.append("BMI", Imc.imcByGender(this));
		sb.append("age", getAgeWithSymbolFull());
		if (!this.getPhones().isEmpty())
			sb.append("phones", listPhones());
		if (!this.getEmails().isEmpty())
			sb.append("emails", this.getEmails());
		if (!this.getAddresses().isEmpty())
			sb.append(getMainAddress());
		if (this.getPartner() != null) {
			sb.append("partner", this.getPartner().getFirstname());
		}
		return sb.toString();
	}
	
	public List<String> listPhones() {
		return this.phones.entrySet().stream()
				.map(e -> StringUtils.joinWith("=", e.getKey(), e.getValue()))
				.toList();
	}
	
	public Address getMainAddress() {
		Optional<Address> address = this.addresses.stream()
				.filter(Address::isPrincipal).findFirst();
		if (address.isPresent())
			return address.get();
		return null;
	}
	
	public boolean isAlive() {
		return this.getDeathdate() == null && this.getBirthdate() != null;
	}
	
	public Character getSymbol() {
		return isAlive() ? BLACK_STAR : LATIN_CROSS;
	}
	
	public int getAge() {
		if (isAlive() && this.getBirthdate().isBefore(now))
			return Period.between(this.getBirthdate(), now).getYears();
		else
			return Period.between(this.getBirthdate(), this.getDeathdate())
					.getYears();
	}
	
	public String getAgeWithSymbol() {
		return StringUtils.join(this.getAge(), this.getSymbol());
	}
	
	public String getAgeWithSymbolFull() {
		var sb = new StringBuilder().append(this.getAge()).append("(")
				.append(BLACK_STAR).append(dtf.format(this.getBirthdate()));
		if (!isAlive())
			sb.append("," + LATIN_CROSS + dtf.format(this.getDeathdate()));
		return sb.append(")").toString();
	}
	
	public boolean validyDeathDate(LocalDate deathdate) {
		return deathdate.isAfter(this.getBirthdate());
	}
	
	public void diedNow() {
		if (isAlive() && validyDeathDate(now))
			this.setDeathdate(now);
	}
	
	public void diedIn(LocalDate deathDate) {
		if (isAlive() && validyDeathDate(deathDate))
			this.setDeathdate(deathDate);
	}
	
	public void diedIn(Date deathDate) {
		diedIn(deathDate.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate());
	}
	
	public void diedIn(Date deathDate, ZoneId zone) {
		diedIn(deathDate.toInstant().atZone(zone).toLocalDate());
	}
	
	public String calcIMC() {
		return Imc.imcByGender(this.getWeight(), this.getHeight(),
				this.getGender());
	}
}
