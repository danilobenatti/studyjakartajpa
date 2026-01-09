package studyjakartajpa;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceUnit;
import studyjakartajpa.model.Address;
import studyjakartajpa.model.Order;
import studyjakartajpa.model.OrderItem;
import studyjakartajpa.model.Person;
import studyjakartajpa.model.Product;
import studyjakartajpa.model.WishList;
import studyjakartajpa.model.enums.OrderStatus;
import studyjakartajpa.model.enums.ProductUnit;

public class Principal {
	
	@PersistenceUnit(unitName = "persistence-unit")
	private static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("persistence-unit");
	
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private static EntityManager em = emf.createEntityManager();
	
	public static void main(String[] args) {
		
		Person p1 = Person.of("Jordan", 'M', 80.5F, 1.81F,
				LocalDate.of(1980, 3, 27), Map.of('H', "(11)4242-2323"));
		p1.setAddress(Address.of("123", "Main St.", "Apt.4B", "New York", "NY",
				"USA", "10001", true, p1));
		
		Person p2 = Person.of("Anna", 'F', 62.8F, 1.52F,
				LocalDate.of(1948, 11, 10),
				Map.of('M', "(11)97878-8787", 'H', "(11)2233-s3322"));
		p2.setAddress(Address.of("789", "Elm St.", null, "Boston", "MA", "USA",
				"02110", true, p2));
		
		p1.setPartner(p2);
		
		Person p3 = Person.of("Jack", 'M', 89.8F, 1.78F,
				LocalDate.of(1982, 7, 3),
				Map.of('M', "(11)99999-8888", 'W', "(11)8888-7777"));
		p3.setEmails("jhonjj@mail.com", "jjj@mail.com");
		
		Person p4 = Person.of("Bett", 'F', 70.8F, 1.78F,
				LocalDate.of(1988, 8, 24),
				Map.of('M', "(11)97777-6666", 'W', "(11)2121-1122"));
		p4.setEmail("nett9@mail.com");
		p4.setAddress(Address.of("1486", "Buena Vista Dr", "Lake Buena Vista",
				"Orlando", "FL", "USA", "32830", true, p4));
		
		Person p5 = Person.of("Rony", 'M', 0, 0, LocalDate.of(1950, 1, 5),
				Map.ofEntries(Map.entry('M', "(11)98989-9898"),
						Map.entry('W', "(11)5555-4444")),
				Set.of("rony123@mail.com", "rony@mail.com"));
		
		Address a51 = Address.of("350", "5th Avenue", null, "New York", "NY",
				"USA", "10118", false, p5);
		
		Address a52 = Address.of("233", "Paulista Avenue", "7th", "São Paulo",
				"SP", "Brazil", "01310-100", true, p5);
		
		p5.setAddresses(a51, a52);
		
		List<Person> persons = List.of(p1, p2, p3, p4, p5);
		
		Product prd1 = Product
				.of("Title 1", "Description 1", 0.05F, 5.5, ProductUnit.UNITY)
				.setValidity(6, ChronoUnit.MONTHS);
		
		Product prd2 = Product
				.of("Title 2", "Description 2", 0.1F, 10.5, ProductUnit.UNITY)
				.setValidity(1, ChronoUnit.YEARS);
		
		Product prd3 = Product
				.of("Title 3", "Description 3", 0.15F, 15.5, ProductUnit.UNITY)
				.setValidity(18, ChronoUnit.MONTHS);
		
		List<Product> products = List.of(prd1, prd2, prd3);
		
		WishList p4wl = WishList.of("Market list", "Cleaning supplies", p4,
				Set.of(prd1));
		
		WishList p5wl = WishList.of("Christmas list", "Christmas gifts family",
				p5, Set.of(prd1, prd2, prd3));
		
		List<WishList> wishLists = List.of(p4wl, p5wl);
		
		Order o1 = Order.of(LocalDate.now().plusDays(40), p5, 0.05F,
				OrderStatus.WAITING); // $37.30 desc. 5% = $35.43
		p5.setOrder(o1);
		
		OrderItem i1 = OrderItem.of(o1, prd1, 1); // $5.50 - 5% = $5.22
		
		OrderItem i2 = OrderItem.of(o1, prd2, 2); // ($10.50 - 10%) x 2 = $18.90
		
		OrderItem i3 = OrderItem.of(o1, prd3, 1); // ($15.50 - 15%) x 2 = $13.18
		
		o1.setOrderItems(i1, i2, i3);
		
		try {
			em.getTransaction().begin();
			
			persons.forEach(em::persist);
			products.forEach(em::persist);
			wishLists.forEach(em::persist);
			em.persist(o1);
			
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
