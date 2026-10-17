package studyjakartajpa.model;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import studyjakartajpa.util.DateUtils;
import studyjakartajpa.util.Imc;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
@Entity
@EntityListeners(value = { AuditListener.class })
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "petype", columnDefinition = "char(1)", discriminatorType = DiscriminatorType.CHAR,
	length = 1)
@DiscriminatorValue(value = "P")
@Table(name = "persons", catalog = "jpaforbeginners", schema = "public",
	uniqueConstraints = @UniqueConstraint(name = "uk_persons_partner_id", columnNames = { "partner_id" }))
@NamedNativeQuery(name = "Persons.findAllLive", resultClass = Person.class,
	query = "select p.id as id, p.peType as peType, "
			+ "p.first_name, p.gender, p.weight, p.height, p.birth_date, p.death_date, "
			+ "p.date_insert, p.date_update, p.partner_id "
			+ "from persons p where p.death_date is null limit ?1 offset ?2")
@SequenceGenerator(catalog = "jpaforbeginners", schema = "public", name = "persons_seq_generator",
	sequenceName = "persons_id_seq", initialValue = 100, allocationSize = 50)
public class Person implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	static final DateTimeFormatter DTF = ofPattern("d/MMM/yyyy", DateUtils.getLocale());
	
	static final char BLACK_STAR = '\u272E'; // Heavy Outlined Black Star
	
	static final char LATIN_CROSS = '\u271F'; // Outlined Latin Cross
	
	@Id
	@Setter(value = AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persons_seq_generator")
	private Long id;
	
	@lombok.NonNull
	private String firstName;
	
	@lombok.NonNull
	private Character gender;
	
	public void setGender(Character gender) {
		if (gender != null) {
			this.gender = Character.toUpperCase(gender);
		}
	}
	
	void toUpperCaseGender() {
		if (this.gender != null) {
			this.gender = Character.toUpperCase(this.gender);
		}
	}
	
	@Builder.Default
	private float weight = 0.0F;
	
	@Builder.Default
	private float height = 0.0F;
	
	@OneToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	@JoinColumn(name = "partner_id", referencedColumnName = "id", nullable = true,
		foreignKey = @ForeignKey(name = "FK_persons_partner_id",
			foreignKeyDefinition = "FOREIGN KEY (partner_id) REFERENCES public.persons(ID) ON DELETE SET NULL"))
	private Person partner;
	
	public void setPartner(Person person) {
		// fix bidirectional link and allow unlinking
		if (this.partner == person)
			return;
		Person old = this.partner;
		this.partner = person;
		if (old != null && old.partner == this) {
			old.partner = null;
		}
		if (person != null && person.partner != this) {
			person.partner = this;
		}
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "persons_phones", catalog = "jpaforbeginners", schema = "public",
		joinColumns = @JoinColumn(name = "person_id", table = "persons", referencedColumnName = "id", nullable = false))
	@MapKeyColumn(name = "phType", columnDefinition = "char(1)", nullable = false)
	@Column(name = "phone_number", length = 20, nullable = false)
	private Map<Character, String> phones = new HashMap<>();
	
	public String setPhone(Character type, String number) {
		if (isAlive() && type != null) {
			return this.phones.put(type, number);
		}
		return null;
	}
	
	public void setPhones(Map<Character, String> phones) {
		if (isAlive() && phones != null) {
			this.phones.putAll(phones);
		}
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<Address> addresses = new ArrayList<>();
	
	public boolean setAddress(Address address) {
		if (address == null)
			return false;
		address.setPerson(this);
		return this.addresses.add(address);
	}
	
	public boolean setAddresses(List<Address> addresses) {
		if (addresses == null)
			return false;
		addresses.forEach(a -> a.setPerson(this));
		return this.addresses.addAll(addresses);
	}
	
	public boolean setAddresses(Address... addresses) {
		if (addresses == null)
			return false;
		List.of(addresses).forEach(a -> a.setPerson(this));
		return this.addresses.addAll(List.of(addresses));
	}
	
	@Builder.Default
	@PrivateOwned
	@CascadeOnDelete
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "persons_emails", catalog = "jpaforbeginners", schema = "public",
		joinColumns = @JoinColumn(name = "person_id"))
	@Column(name = "email", length = 150, nullable = false)
	private Set<String> emails = new HashSet<>();
	
	public void setEmail(String email) {
		if (email != null) {
			this.emails.add(email);
		}
	}
	
	public void setEmails(Collection<String> emails) {
		if (emails != null) {
			this.emails.addAll(emails);
		}
	}
	
	public void setEmails(String... emails) {
		if (emails != null) {
			Set.of(emails).forEach(this.emails::add);
		}
	}
	
	@Builder.Default
	@CascadeOnDelete
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.REMOVE)
	private Set<WishList> wishLists = new HashSet<>();
	
	@Builder.Default
	@OneToMany(mappedBy = "person", fetch = FetchType.LAZY)
	private List<Order> orders = new ArrayList<>();
	
	public void setOrder(Order order) {
		if (order != null) {
			this.orders.add(order);
		}
	}
	
	public void setOrders(Order... orders) {
		if (orders != null) {
			List.of(orders).forEach(this::setOrder);
		}
	}
	
	@lombok.NonNull
	private LocalDate birthDate;
	
	public void setBirthDate(LocalDate birthDate) {
		if (isValidBirthDate(birthDate)) {
			this.birthDate = birthDate;
		}
	}
	
	private LocalDate deathDate;
	
	private void setDeathdate(LocalDate deathDate) {
		if (isValidDeathDate(deathDate))
			this.deathDate = deathDate;
	}
	
	@Setter(value = AccessLevel.PROTECTED)
	private LocalDateTime dateCreate;
	
	@Setter(value = AccessLevel.PROTECTED)
	private LocalDateTime dateUpdate;
	
	public static Person of(String firstname, Character gender, LocalDate birthdate, Map<Character, String> phones,
			Set<String> emails) {
		return Person.builder().firstName(firstname).gender(gender).birthDate(birthdate).phones(phones).emails(emails)
				.build();
	}
	
	@Override
	public String toString() {
		var sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		sb.append("id", getId());
		sb.append("firstname", getFirstName());
		sb.append("gender", getGender());
		sb.append("BMI", Imc.imcByGender(this));
		sb.append("age", getAgeWithSymbolFull());
		if (!getPhones().isEmpty())
			sb.append("phones", listPhones());
		if (!getEmails().isEmpty())
			sb.append("emails", getEmails());
		if (!getAddresses().isEmpty() && getMainAddress().isPresent())
			sb.append(getMainAddress().map(Object::toString).get());
		if (getPartner() != null) {
			sb.append("partner", getPartner().getFirstName());
		}
		return sb.toString();
	}
	
	public List<String> listPhones() {
		if (this.phones.isEmpty() || this.phones.values().stream().allMatch(StringUtils::isBlank))
			return Collections.emptyList();
		return this.phones.entrySet().stream().map(e -> StringUtils.joinWith("=", e.getKey(), e.getValue())).toList();
	}
	
	public Optional<Address> getMainAddress() {
		return this.addresses.stream().filter(Objects::nonNull).filter(Address::isPrincipal).findFirst();
	}
	
	public boolean isAlive() {
		return this.getBirthDate() != null && this.getDeathDate() == null;
	}
	
	public boolean isValidBirthDate(LocalDate birthdate) {
		if (birthdate == null)
			return false;
		LocalDate today = DateUtils.today();
		LocalDate maxAge = today.minusYears(130);
		return !birthdate.isAfter(today) && birthdate.isAfter(maxAge);
	}
	
	public boolean isValidDeathDate(LocalDate deathdate) {
		if (deathdate == null || this.getBirthDate() == null)
			return false;
		return !deathdate.isAfter(DateUtils.today()) && !deathdate.isBefore(this.getBirthDate());
	}
	
	public Character getSymbol() {
		return isAlive() ? BLACK_STAR : LATIN_CROSS;
	}
	
	public int getAge() {
		if (isAlive())
			return Period.between(this.getBirthDate(), DateUtils.today()).getYears();
		else
			return Period.between(this.getBirthDate(), this.getDeathDate()).getYears();
	}
	
	public String getAgeWithSymbol() {
		return StringUtils.join(this.getAge(), this.getSymbol());
	}
	
	public String getAgeWithSymbolFull() {
		var sb = new StringBuilder().append(this.getAge()).append("(").append(BLACK_STAR)
				.append(DTF.format(this.getBirthDate()));
		if (!isAlive())
			sb.append("," + LATIN_CROSS).append(DTF.format(this.getDeathDate()));
		return sb.append(")").toString();
	}
	
	public void diedNow() {
		LocalDate today = DateUtils.today();
		if (isAlive() && isValidDeathDate(today))
			this.setDeathdate(today);
	}
	
	public void diedIn(LocalDate deathDate) {
		if (isAlive() && isValidDeathDate(deathDate))
			this.setDeathdate(deathDate);
	}
	
	public void diedIn(Date deathDate) {
		if (deathDate != null)
			diedIn(deathDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}
	
	public void diedIn(Date deathDate, ZoneId zone) {
		if (deathDate != null && zone != null)
			diedIn(deathDate.toInstant().atZone(zone).toLocalDate());
	}
	
	public String calcIMC() {
		return Imc.imcByGender(this.getWeight(), this.getHeight(), this.getGender());
	}
}
