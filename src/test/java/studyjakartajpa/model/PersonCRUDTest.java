package studyjakartajpa.model;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Query;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class PersonCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includePersonTest() {
		Person person = new Person("Test Include", 'M',
				LocalDate.of(1990, Month.JANUARY, 1));
		em.getTransaction().begin();
		em.persist(person);
		em.getTransaction().commit();
		
		String qlString = "select p from Person p where p.firstname = :name";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "Test Include");
		Person result = (Person) query.getSingleResult();
		
		System.out.println(result);
		Assertions.assertEquals("Test Include", result.getFirstname());
	}
	
	@Test
	@Order(2)
	void updatePersonTest() {
		String qlString = "select p from Person p where p.firstname = :name";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "Test2");
		Person person = (Person) query.getSingleResult();
		
		person.setFirstname("Test2 Update");
		person.getEmails().clear();
		person.getPhones().clear();
		person.setDeathdate(LocalDate.now());
		
		em.getTransaction().begin();
		em.merge(person);
		em.getTransaction().commit();
		
		Assertions.assertEquals(LocalDate.now(), person.getDeathdate());
	}
	
	@Test
	@Order(3)
	void deletePersonTest() {
		Person person = em.find(Person.class, 10L);
		if (person != null) {
			em.getTransaction().begin();
			em.remove(person);
			em.getTransaction().commit();
		}
		Assertions.assertNull(em.find(Person.class, 10L));
	}
	
}
