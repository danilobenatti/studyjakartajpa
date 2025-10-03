package studyjakartajpa;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import studyjakartajpa.model.Address;
import studyjakartajpa.model.Person;

public class Principal {
	
	@PersistenceUnit
	private static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("persistence-unit");
	
	@PersistenceContext
	private static EntityManager em = emf.createEntityManager();
	
	public static void main(String[] args) {
		
		Person p1 = new Person("Jordan", 'M', LocalDate.of(1980, 3, 27));
		p1.setPhone('H', "(11)4242-2323");
		p1.setAddress(new Address(0, "123", "Main St.", "Apt. 4B", "New York",
				"NY", "USA", "10001", true, p1));
		
		Person p2 = new Person();
		p2.setFirstname("Anna");
		p2.setGender('F');
		p2.setBirthdate(LocalDate.of(1948, Month.NOVEMBER, 10));
		p2.setPhone('M', "(11)97878-8787");
		p2.setPhone('H', "(11)2233-3322");
		
		Address a2 = new Address();
		a2.setNumber("789");
		a2.setStreet("Elm St.");
		a2.setCity("Boston");
		a2.setState("MA");
		a2.setCountry("USA");
		a2.setZipCode("02110");
		a2.setPrincipal(true);
		a2.setPerson(p2);
		
		p2.setAddress(a2);
		
		Person p3 = Person.of("Jhon", 'M', LocalDate.of(1982, 7, 3));
		p3.setPhones(Map.of('M', "(11)99999-8888", 'W', "(11)8888-7777"));
		p3.setEmails("jhonjj@mail.com", "jjj@mail.com");
		
		Person p4 = Person.of("Bett", 'F', LocalDate.of(1988, 8, 24),
				Map.of('M', "(11)97777-6666", 'W', "(11)2121-1122"));
		p4.setEmail("nett9@mail.com");
		
		Person p5 = Person.builder().withFirstname("Rony").withGender('M')
				.withBirthdate(LocalDate.of(1950, 1, 5)).build();
		p5.setPhones(Map.ofEntries(Map.entry('M', "(11)98989-9898"),
				Map.entry('W', "(11)5555-4444")));
		p5.setEmails(Set.of("rony123@mail.com","rony@mail.com"));
		
		var a51 = Address.builder().withNumber("350").withStreet("5th Avenue")
				.withCity("New York").withState("NY").withCountry("USA")
				.withZipCode("10118").withPerson(p5).build();
		a51.setPrincipal(false);
		
		var a52 = Address.of("233", "Paulista Avenue", "7th", "São Paulo", "SP",
				"Brazil", "01310-100", true, p5);
		
		p5.setAddresses(a51, a52);
		
		List<Person> persons = new ArrayList<>();
		persons.add(p1);
		persons.add(p2);
		persons.add(p3);
		persons.add(p4);
		persons.add(p5);
		
		try {
			em.getTransaction().begin();
			
			persons.forEach(em::persist);
			
			em.getTransaction().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (em.isOpen())
				em.close();
			if (emf.isOpen())
				emf.close();
		}
	}
	
}
