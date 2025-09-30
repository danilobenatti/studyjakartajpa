package studyjakartajpa.model;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Query;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class PersonCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includePersonTest() {
		Person person = new Person("TestInclude", 'M',
				LocalDate.of(1990, Month.JANUARY, 1));
		em.getTransaction().begin();
		em.persist(person);
		em.getTransaction().commit();
		
		Query query = em.createQuery("select p from Person p where p.firstname = :firstName");
		query.setParameter("firstName", "TestInclude");
		Person result = (Person) query.getSingleResult();
		
		Assertions.assertEquals("TestInclude", result.getFirstname());
	}
	
	@Test
	@Order(2)
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
