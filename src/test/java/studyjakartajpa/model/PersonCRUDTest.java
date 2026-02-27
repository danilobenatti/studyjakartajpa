package studyjakartajpa.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
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
		Person person = Person.maker().firstname("Person Include Test")
				.gender('m').weight(82.5F).height(1.83F)
				.birthdate(LocalDate.of(1990, Month.FEBRUARY, 1)).done();
		person.diedNow();
		person.setPartner(em.find(Person.class, 11L));
		
		Address address = Address.maker().street("Street Name").number("10")
				.city("City").state("State").country("Country").zipCode("00000")
				.person(person).isPrincipal(true).done();
		person.setAddress(address);
		
		em.getTransaction().begin();
		em.persist(person);
		em.getTransaction().commit();
		
		String qlString = """
				select p from Person p
				where lower(p.firstname) like lower(concat('%', :name, '%'))
			""";
		Person p = em.createQuery(qlString, Person.class)
				.setParameter("name", "person includ").getResultList().stream()
				.findFirst().orElse(null);
		
		log.info(p);
		Assertions.assertNotNull(p, "The object should not be null");
	}
	
	@Test
	@Order(2)
	void includePersonTest2() {
		Person person = Person.maker().firstname("Person Include Test2")
				.gender('m').weight(82.5F).height(1.83F)
				.birthdate(LocalDate.of(1990, Month.JANUARY, 1)).done();
		person.diedNow();
		person.setPartner(em.find(Person.class, 15L));
		person.setEmail("pTest2@mail.com");
		person.setPhone('M', "(19)98989-9898");
		
		person.setAddress(
				Address.of("1486", "Buena Vista Dr", "Lake Buena Vista",
						"Orlando", "FL", "USA", "32830", true, person));
		
		DAO<Person> dao = new DAO<>(Person.class);
		
		dao.addEntity(person).end();
		
		log.info(person);
		Assertions.assertEquals("Person Include Test2", person.getFirstname());
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
		
		log.info(avg);
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
		
		BigDecimal avg = BigDecimal.valueOf(avgPrice).setScale(1,
				RoundingMode.HALF_UP);
		
		log.info(avg);
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
		persons.forEach(log::info);
		Assertions.assertEquals("unknown", Imc.imcByGender(persons.get(0)));
	}
	
	@Test
	@Order(6)
	void selectPersonTest4() {
		DAO<Person> dao = new DAO<>(Person.class);
		Person person = dao.searchById(10L);
		dao.end();
		log.info(person);
		Assertions.assertEquals("John", person.getFirstname());
	}
	
	@Test
	@Order(7)
	void selectPersonTest5() {
		DAO<Person> dao = new DAO<>(Person.class);
		Person person = dao.findById(10L);
		dao.end();
		log.info(person);
		Assertions.assertEquals("John", person.getFirstname());
	}
	
	@Test
	@Order(8)
	void selectPersonTest6() {
		DAO<Person> dao = new DAO<>(Person.class);
		List<Person> persons = dao.listAll(4, 2);
		dao.end();
		persons.forEach(log::info);
		Assertions.assertEquals(4, persons.size());
	}
	
	@Test
	@Order(9)
	void updatePersonTest() {
		// lower(p.firstname) like lower(concat('%', :name, '%'))
		String qlString = """
				select p from Person p
				where lower(p.firstname) like lower(concat('%', :name, '%'))
			""";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "MARY");
		Person person = (Person) query.getSingleResult();
		
		person.setFirstname("Mary Update");
		person.setGender('f'); // callback it's work
		person.getAddresses().clear();
		person.getEmails().clear();
		person.getPhones().clear();
		LocalDate date = LocalDate.now().minus(120, ChronoUnit.DAYS);
		person.diedIn(date);
		
		em.getTransaction().begin();
		em.merge(person);
		em.getTransaction().commit();
		
		log.info(person);
		Assertions.assertEquals(date, person.getDeathdate());
	}
	
	@Test
	@Order(10)
	void updatePersonTest2() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaUpdate<Person> update = cb.createCriteriaUpdate(Person.class);
		
		Root<Person> root = update.from(Person.class);
		
		update.set(root.get(Person_.firstname), "John Update");
		update.set(root.get(Person_.gender), 'm'); // callback it's not work
		update.set(root.get(Person_.deathdate), LocalDate.now());
		
		Predicate like = cb.like(root.get(Person_.firstname), "John");
		
		update.where(like);
		
		em.getTransaction().begin();
		int i = em.createQuery(update).executeUpdate();
		em.getTransaction().commit();
		
		log.info(i);
		Assertions.assertEquals(1, i);
	}
	
	@Test
	@Order(11)
	void deletePersonTest() {
		Person person = em.find(Person.class, 13L);
		log.info(person);
		if (person != null) {
			em.getTransaction().begin();
			em.remove(person);
			em.getTransaction().commit();
		}
		Assertions.assertNull(em.find(Person.class, person.getId()));
	}
	
	@Test
	@Order(12)
	void personsWithPartnersTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withPartners",
				Person.class);
		List<Person> persons = query.getResultList();
		
		persons.forEach(log::info);
		Assertions.assertEquals(3, persons.size());
	}
	
	@Test
	@Order(13)
	void personsByAgeTest() {
		TypedQuery<Person> query = em
				.createNamedQuery("Persons.greaterOrEqualtoAge", Person.class);
		query.setParameter("age", 25);
		List<Person> persons = query.getResultList();
		
		persons.forEach(log::info);
		Assertions.assertEquals(2, persons.size());
	}
	
	@Test
	@Order(14)
	void personsWithPhonesTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withPhoners",
				Person.class);
		query.setFirstResult(0); // offset
		query.setMaxResults(10); // limit
		List<Person> persons = query.getResultList();
		
		persons.forEach(log::info);
		Assertions.assertEquals(5, persons.size());
	}
	
	@Test
	@Order(15)
	void personsWithEmailsTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.withEmails",
				Person.class);
		query.setFirstResult(0);
		query.setMaxResults(10);
		List<Person> persons = query.getResultList();
		
		persons.forEach(log::info);
		Assertions.assertEquals(6, persons.size());
	}
	
	@Test
	@Order(16)
	void findAllPersonsLiveTest() {
		TypedQuery<Person> query = em.createNamedQuery("Persons.findAllLive",
				Person.class);
		// Select 5 records, starting from record 2 (skipping the first 2)
		query.setParameter(1, 5);
		query.setParameter(2, 2);
		List<Person> persons = query.getResultList();
		
		persons.forEach(log::info);
		Assertions.assertEquals(2, persons.size());
	}
}
