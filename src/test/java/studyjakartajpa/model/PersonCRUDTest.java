package studyjakartajpa.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.PersonDAO;
import studyjakartajpa.util.Imc;
import studyjakartajpa.util.JpaUtils;

@TestMethodOrder(OrderAnnotation.class)
class PersonCRUDTest {
	
	public static Logger log = LogManager.getLogger();
	private PersonDAO personDAO;
	
	@BeforeEach
	void setUpBeforeEach() {
		personDAO = new PersonDAO();
	}
	
	@AfterEach
	void cleanUpEachThread() {
		JpaUtils.closeEntityManager();
	}
	
	@Test
	@Order(1)
	void includePersonTest() {
		Person person = Person.builder().firstName("Person Include Test").gender('m').weight(82.5F).height(1.83F)
				.birthDate(LocalDate.of(1990, Month.FEBRUARY, 1)).build();
		person.diedNow();
		person.setPartner(personDAO.findById(11L).orElse(null));
		
		Address address = Address.maker().street("Street Name").number("10").city("City").state("State")
				.country("Country").zipCode("00000").person(person).isPrincipal(true).done();
		person.setAddress(address);
		
		Person p = personDAO.save(person);
		log.info(p);
		
		Assertions.assertNotNull(p, "The object should not be null");
	}
	
	@Test
	@Order(2)
	void includePersonTest2() {
		Person p1 = Person.builder().firstName("Person Include Test2").gender('m').weight(82.5F).height(1.83F)
				.birthDate(LocalDate.of(1990, Month.JANUARY, 1)).build();
		p1.setEmail("pTest2@mail.com");
		p1.setPhone('M', "(19)98989-9898");
		p1.diedNow();
		
		p1.setPartner(personDAO.findById(15L).orElse(null));
		
		p1.setAddress(Address.of("1486", "Buena Vista Dr", "Lake Buena Vista", "Orlando", "FL", "USA", "32830",
				true, p1));
		
		Person p2 = Person.builder().firstName("Person Include Test3").gender('F').weight(60.8F).height(1.53F)
				.birthDate(LocalDate.of(1995, Month.DECEMBER, 12)).build();
		p2.setEmail("p2Test@mail.com");
		
		List<Person> persons = personDAO.saveAll(List.of(p1, p2));
		persons.forEach(log::info);
		
		Assertions.assertEquals("Person Include Test2", p1.getFirstName());
	}
	
	@Test
	@Order(3)
	void selectPersonTest() {
		Double avg = JpaUtils.executeResult(em -> {
			String qlString = "select avg(p.weight) from Person p where p.weight > 0 and p.deathDate is null";
			TypedQuery<Double> query = em.createQuery(qlString, Double.class);
			query.setMaxResults(5);
			return query.getSingleResult();
		});
		BigDecimal avgBigDecimal = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
		log.info(avgBigDecimal);
		
		Assertions.assertEquals(74.7, avgBigDecimal.doubleValue());
	}
	
	@Test
	@Order(4)
	void selectPersonTest2() {
		Double avg = JpaUtils.executeResult(em -> {
			// create criteriaBuilder
			CriteriaBuilder cb = em.getCriteriaBuilder();
			// create criteriaQuery
			CriteriaQuery<Double> cq = cb.createQuery(Double.class);
			// define root (from clause)
			Root<Person> root = cq.from(Person.class);
			// construct predicates (where clause)
			Predicate greaterThan = cb.greaterThan(root.get(Person_.weight), 0F);
			Predicate isNull = cb.isNull(root.get(Person_.deathDate));
			// set select and where clauses
			cq.select(cb.avg(root.get(Person_.weight))).where(greaterThan, isNull);
			// create e execute TypedQuery
			return em.createQuery(cq).getSingleResult();
		});
		log.info(avg);
		BigDecimal avgPrice = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
		
		Assertions.assertEquals(74.7, avgPrice.doubleValue());
	}
	
	@Test
	@Order(5)
	void selectPersonTest3() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Person> cq = cb.createQuery(Person.class);
			Root<Person> root = cq.from(Person.class);
			cq.where(cb.equal(root.get(Person_.id), 10L));
			return em.createQuery(cq).getResultList();
		});
		persons.forEach(log::info);
		
		Assertions.assertEquals("unknown", Imc.imcByGender(persons.getFirst()));
	}
	
	@Test
	@Order(6)
	void selectPersonTest4() {
		Person p = personDAO.findById(10L).get();
		log.info(p);
		Assertions.assertEquals("John", p.getFirstName());
	}
	
	@Test
	@Order(7)
	void selectPersonTest6() {
		List<Person> persons = personDAO.findAll(4, 2);
		persons.forEach(log::info);
		
		Assertions.assertEquals(4, persons.size());
	}
	
	@Test
	@Order(8)
	void updatePersonTest() {
		LocalDate date = LocalDate.now().minus(120, ChronoUnit.DAYS);
		Person updatedPerson = JpaUtils.executeResult(em -> {
			// lower(p.firstname) like lower(concat('%', :name, '%'))
			String qlString = """
					select p from Person p
					where lower(p.firstName) like lower(concat('%', :name, '%'))
				""";
			TypedQuery<Person> query = em.createQuery(qlString, Person.class);
			query.setParameter("name", "MARY");
			Person person = query.getSingleResult();
			person.setFirstName("Mary Update");
			person.setGender('f'); // callback it's work
			person.getAddresses().clear();
			person.getEmails().clear();
			person.getPhones().clear();
			person.diedIn(date);
			return em.merge(person);
		});
		log.info(updatedPerson);
		
		Assertions.assertEquals(date, updatedPerson.getDeathDate());
	}
	
	@Test
	@Order(9)
	void updatePersonTest2() {
		Integer rowsUpdated = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaUpdate<Person> update = cb.createCriteriaUpdate(Person.class);
			Root<Person> root = update.from(Person.class);
			
			update.set(root.get(Person_.firstName), "John Update");
			update.set(root.get(Person_.gender), 'm'); // callback it's not work
			update.set(root.get(Person_.deathDate), LocalDate.now());
			
			Predicate like = cb.like(root.get(Person_.firstName), "John");
			update.where(like);
			
			return em.createQuery(update).executeUpdate();
		});
		log.info(rowsUpdated);
		
		Assertions.assertEquals(1, rowsUpdated);
	}
	
	@Test
	@Order(10)
	void deletePersonTest() {
		Long id = 13L;
		JpaUtils.execute(em -> {
			Person person = em.find(Person.class, id);
			if (person != null) {
				em.remove(person);
			}
		});
		boolean exists = personDAO.existsById(id);
		
		Assertions.assertFalse(exists, "The entity should have been removed from database context.");
	}
	
	@Test
	@Order(11)
	void personsWithPartnersTest() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			TypedQuery<Person> query = em.createNamedQuery("Persons.withPartners", Person.class);
			return query.getResultList();
			
		});
		
		persons.forEach(log::info);
		
		Assertions.assertEquals(4, persons.size());
	}
	
	@Test
	@Order(12)
	void personsByAgeTest() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			TypedQuery<Person> query = em.createNamedQuery("Persons.greaterOrEqualtoAge", Person.class);
			query.setParameter(1, 25);
			return query.getResultList();
		});
		
		persons.forEach(log::info);
		
		Assertions.assertEquals(2, persons.size());
	}
	
	@Test
	@Order(13)
	
	void personsWithPhonesTest() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			TypedQuery<Person> query = em.createNamedQuery("Persons.withPhoners", Person.class);
			query.setFirstResult(0); // offset
			query.setMaxResults(10); // limit
			return query.getResultList();
		});
		
		persons.forEach(log::info);
		
		Assertions.assertEquals(6, persons.size());
	}
	
	@Test
	@Order(14)
	void personsWithEmailsTest() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			TypedQuery<Person> query = em.createNamedQuery("Persons.withEmails", Person.class);
			query.setFirstResult(0);
			query.setMaxResults(10);
			return query.getResultList();
		});
		
		persons.forEach(log::info);
		
		Assertions.assertEquals(6, persons.size());
	}
	
	@Test
	@Order(15)
	void findAllPersonsLiveTest() {
		List<Person> persons = JpaUtils.executeResult(em -> {
			TypedQuery<Person> query = em.createNamedQuery("Persons.findAllLive", Person.class);
			// Select 5 records, starting from record 2 (skipping the first 2)
			query.setParameter(1, 5);
			query.setParameter(2, 2);
			return query.getResultList();
		});
		
		persons.forEach(log::info);
		
		Assertions.assertEquals(2, persons.size());
	}
}
