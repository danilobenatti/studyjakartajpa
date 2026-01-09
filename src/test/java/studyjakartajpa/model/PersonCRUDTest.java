package studyjakartajpa.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.DAO;
import studyjakartajpa.util.EntityManagerTest;
import studyjakartajpa.util.Imc;

@TestMethodOrder(OrderAnnotation.class)
class PersonCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includePersonTest() {
		Person person = Person.of("Person Include Test", 'M', 82.5F, 1.83F,
				LocalDate.of(1990, Month.JANUARY, 1));
		person.setDeathdate(LocalDate.now());
		
		person.setPartner(em.find(Person.class, 11L));
		
		Address address = Address.builder().withStreet("Street Name")
				.withNumber("10").withCity("City").withState("State")
				.withCountry("Country").withZipCode("00000").withPerson(person)
				.withIsPrincipal(true).build();
		person.setAddress(address);
		
		em.getTransaction().begin();
		em.persist(person);
		em.getTransaction().commit();
		
		String qlString = """
				select p from Person p
				where lower(p.firstname) like lower(concat('%', :name, '%'))
			""";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "PERSON");
		Person result = (Person) query.getSingleResult();
		
		System.out.println(result);
		Assertions.assertEquals("Person Include Test", result.getFirstname());
		Assertions.assertEquals("Mary", result.getPartner().getFirstname());
	}
	
	@Test
	@Order(2)
	void includePersonTest2() {
		Person person = Person.of("Person Include Test2", 'M', 82.5F, 1.83F,
				LocalDate.of(1990, Month.JANUARY, 1));
		person.personDiedNow();
		person.setPartner(em.find(Person.class, 13L));
		
		person.setAddress(
				Address.of("1486", "Buena Vista Dr", "Lake Buena Vista",
						"Orlando", "FL", "USA", "32830", true, person));
		
		DAO<Person> dao = new DAO<>(Person.class);
		
		dao.addEntity(person);
		
		Person p = dao.findById(person.getId());
		System.out.println(p);
		Assertions.assertEquals("Person Include Test2", p.getFirstname());
	}
	
	@Test
	@Order(3)
	void selectPersonTest() {
		String qlString = """
			select avg(p.weight) from Person p
			where p.weight > 0 and p.deathdate is null
			""";
		Query query = em.createQuery(qlString, Double.class);
		Double avgPrice = (Double) query.getSingleResult();
		
		BigDecimal avg = BigDecimal.valueOf(avgPrice).setScale(1,
				RoundingMode.HALF_UP);
		
		System.out.println(avg);
		Assertions.assertEquals(74.7, avg.doubleValue());
	}
	
	@Test
	@Order(4)
	void selectPersonTest2() {
		// create criteriaBuilder
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		// create criteriaQuery
		CriteriaQuery<Double> cq = cb.createQuery(Double.class);
		
		// define root (from clause)
		Root<Person> root = cq.from(Person.class);
		
		// construct predicates (where clause)
		Predicate greaterThan = cb.greaterThan(root.get(Person_.weight), 0F);
		Predicate isNull = cb.isNull(root.get(Person_.deathdate));
		
		// set select and where clauses
		cq.select(cb.avg(root.get(Person_.weight))).where(greaterThan, isNull);
		
		// create e execute TypedQuery
		Double avgPrice = em.createQuery(cq).getSingleResult();
		
		MathContext mc = new MathContext(3, RoundingMode.HALF_UP);
		BigDecimal avg = BigDecimal.valueOf(avgPrice).round(mc);
		
		System.out.println(avg);
		Assertions.assertEquals(74.7, avg.doubleValue());
	}
	
	@Test
	@Order(5)
	void selectPersonTest3() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> root = cq.from(Person.class);
		cq.where(cb.equal(root.get(Person_.id), 10L));
		TypedQuery<Person> query = em.createQuery(cq);
		List<Person> persons = query.getResultList();
		persons.forEach(System.out::println);
		assertEquals("unknown", Imc.imcByGender(persons.get(0)));
	}
	
	@Test
	@Order(6)
	void updatePersonTest() {
		// lower(p.firstname) like lower(concat('%', :name, '%'))
		String qlString = """
				select p from Person p
				where lower(p.firstname) like lower(concat('%', :name, '%'))
			""";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "Mary");
		Person person = (Person) query.getSingleResult();
		
		person.setFirstname("Mary Update");
		person.getAddresses().clear();
		person.getEmails().clear();
		person.getPhones().clear();
		person.personDiedNow();
		
		em.getTransaction().begin();
		em.merge(person);
		em.getTransaction().commit();
		
		System.out.println(person);
		Assertions.assertEquals(LocalDate.now(), person.getDeathdate());
	}
	
	@Test
	@Order(7)
	void updatePersonTest2() {
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaUpdate<Person> update = cb.createCriteriaUpdate(Person.class);
		
		Root<Person> root = update.from(Person.class);
		
		update.set(root.get(Person_.firstname), "John Update");
		update.set(root.get(Person_.deathdate), LocalDate.now());
		
		update.set(root.get(Person_.dateUpdate), LocalDateTime.now());
		
		Predicate like = cb.like(root.get(Person_.firstname), "John");
		
		update.where(like);
		
		em.getTransaction().begin();
		int i = em.createQuery(update).executeUpdate();
		em.getTransaction().commit();
		
		System.out.println(i);
		Assertions.assertEquals(1, i);
	}
	
	@Test
	@Order(8)
	void deletePersonTest() {
		Person person = em.find(Person.class, 13L);
		System.out.println(person);
		if (person != null) {
			em.getTransaction().begin();
			em.remove(person);
			em.getTransaction().commit();
		}
		Assertions.assertNull(em.find(Person.class, person.getId()));
	}
	
	@Test
	@Order(9)
	void personsWithPartnersTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withPartners",
				Person.class);
		List<Person> persons = query.getResultList();
		
		persons.forEach(System.out::println);
		Assertions.assertEquals(2, persons.size());
	}
	
	@Test
	@Order(10)
	void personsByAgeTest() {
		TypedQuery<Person> query = em
				.createNamedQuery("Persons.greaterOrEqualtoAge", Person.class);
		query.setParameter("age", 25);
		List<Person> persons = query.getResultList();
		
		persons.forEach(System.out::println);
		Assertions.assertEquals(2, persons.size());
	}
	
	@Test
	@Order(11)
	void personsWithPhonesTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withPhoners",
				Person.class);
		query.setFirstResult(0); // offset
		query.setMaxResults(10); // limit
		List<Person> persons = query.getResultList();
		
		persons.forEach(System.out::println);
		Assertions.assertEquals(5, persons.size());
	}
	
	@Test
	@Order(12)
	void personsWithEmailsTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withEmails",
				Person.class);
		query.setFirstResult(0);
		query.setMaxResults(10);
		List<Person> persons = query.getResultList();
		
		persons.forEach(System.out::println);
		Assertions.assertEquals(5, persons.size());
	}
	
	@Test
	@Order(13)
	void findAllPersonsLiveTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.findAllLive",
				Person.class);
		// Select 5 records, starting from record 2 (skipping the first 2)
		query.setParameter(1, 5);
		query.setParameter(2, 2);
		List<Person> persons = query.getResultList();
		
		persons.forEach(System.out::println);
		Assertions.assertEquals(2, persons.size());
	}
}
