package studyjakartajpa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceUnit;
import studyjakartajpa.model.Address;
import studyjakartajpa.model.Employee;
import studyjakartajpa.model.Order;
import studyjakartajpa.model.OrderItem;
import studyjakartajpa.model.Person;
import studyjakartajpa.model.Product;
import studyjakartajpa.model.WishList;
import studyjakartajpa.model.enums.JobFunctions;
import studyjakartajpa.model.enums.OrderStatus;
import studyjakartajpa.model.enums.ProductUnit;

public class Principal {
	
	private static final LocalDate NOW = LocalDate.now();
	
	@PersistenceUnit(unitName = "persistence-unit")
	private static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("persistence-unit");
	
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private static EntityManager em = emf.createEntityManager();
	
	static Logger log = LogManager.getLogger();
	
	public static void main(String[] args) {
		
		Configurator.initialize(Principal.class.getName(),
				"./src/main/resources/log4j2.properties");
		
		log.info(Month.JANUARY.name());
		log.info(Month.JANUARY::toString);
		log.info(() -> String.format("%s(%s)", NOW.getMonth(),
				NOW.getMonth().getValue()));
		log.info(() -> StringUtils.capitalize(Month.JANUARY
				.getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"))));
		log.info(() -> NOW.getDayOfWeek().getDisplayName(
				TextStyle.FULL_STANDALONE, Locale.getDefault()));
		log.info(() -> StringUtils.capitalize(NOW.getDayOfWeek()
				.getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"))));
		
		log.info(Float.isNaN(1.0F));
		log.info(Float.isNaN(0.0F));
		log.info(Float.isNaN(-1.0F));
		
		Person p1 = Person.maker().firstname("Jordan").gender('M').weight(80.5F)
				.height(1.81F).birthdate(LocalDate.of(1980, 3, 27)).done();
		p1.setPhone('H', "(11)4242-2323");
		p1.setAddress(Address.of("123", "Main St.", "Apt.4B", "New York", "NY",
				"USA", "10001", true, p1));
		
		Person p2 = Person.maker().firstname("Anna").gender('F').weight(62.8F)
				.height(1.52F).birthdate(LocalDate.of(1948, 11, 10))
				.phones(Map.of('M', "(11)97878-8787", 'H', "(11)2233-3322"))
				.done();
		p2.setAddress(Address.of("789", "Elm St.", null, "Boston", "MA", "USA",
				"02110", true, p2));
		p2.setPartner(p1);
		
		Person p3 = Person.maker().firstname("Jack").gender('M').weight(89.8F)
				.height(1.78F).birthdate(LocalDate.of(1982, 7, 3))
				.phones(Map.of('M', "(11)99999-8888", 'W', "(11)8888-7777"))
				.done();
		p3.setEmails("jhonjj@mail.com", "jjj@mail.com");
		
		Person p4 = Person.maker().firstname("Bett").gender('F').weight(70.8F)
				.height(1.78F).birthdate(LocalDate.of(1988, 8, 24)).done();
		p4.setPhones(Map.of('M', "(11)97777-6666", 'W', "(11)2121-1122"));
		p4.setEmail("nett9@mail.com");
		p4.setAddress(Address.of("1486", "Buena Vista Dr", "Lake Buena Vista",
				"Orlando", "FL", "USA", "32830", true, p4));
		
		Person p5 = Person.of("Rony", 'M', LocalDate.of(1950, 1, 5),
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
		
		/**
		 * $37.30 desc 5% = $35.44
		 */
		Order o1 = Order.of(NOW.plusDays(40), p5, 0.05F, OrderStatus.WAITING);
		
		p5.setOrder(o1);
		
		OrderItem i1 = OrderItem.of(o1, prd1, 1); // $5.50 - 5% = $5.22
		log.info(i1.calcSubTotal());
		
		OrderItem i2 = OrderItem.of(o1, prd2, 2); // ($10.50 - 10%) x 2 = $18.90
		log.info(i2.calcSubTotal());
		
		OrderItem i3 = OrderItem.of(o1, prd3, 1); // ($15.50 - 15%) x 2 = $13.18
		log.info(i3.calcSubTotal());
		
		o1.setOrderItems(i1, i2, i3);
		log.info(o1.calcTotal());
		
		Order o2 = Order.of(NOW.plusDays(40), p4, 0.025F, OrderStatus.PAID);
		p4.setOrder(o2);
		
		OrderItem i4 = OrderItem.of(o2, prd1, 1);
		
		OrderItem i5 = OrderItem.of(o2, prd2, 2);
		
		o2.setOrderItems(i4, i5);
		
		List<Order> orders = List.of(o1, o2);
		
		Employee e1 = new Employee();
		e1.setFirstname("Employee Test1");
		e1.setGender('m');
		e1.setBirthdate(LocalDate.of(1981, Month.JUNE, 15));
		e1.setRegister("789456");
		e1.setJobFunction(JobFunctions.ENG_JR);
		e1.setHiringDate(NOW.minus(Period.ofYears(3).plusMonths(6)));
		e1.setSalary(1500.899);
		e1.setEmail("employeetest1@mail.gw");
		e1.setPhone('W', "(45)94545-5454");
		
		Employee e2 = new Employee("Employee Test2", 'f',
				LocalDate.of(1980, 7, 30), "987654", JobFunctions.ENG_JR,
				1899.999, NOW.minus(Period.ofYears(6).plusMonths(3)));
		e2.setPhones(Map.of('W', "(56)4565-1234", 'M', "(56)94545-1234"));
		e2.setEmails(Set.of("emptest2@mail.tw", "emptest2@hmail.com"));
		
		Employee e3 = Employee.maker().firstname("Employee Test3").gender('m')
				.birthdate(LocalDate.of(1982, 7, 3)).register("321654")
				.jobFunction(JobFunctions.ENG_PL.getCode())
				.hiringDate(NOW.minus(Period.ofYears(10))).done();
		e3.setAddress(Address.of("11", "Street Name", "10", "City", "State",
				"Country", "147852", true, e3));
		e3.diedIn(NOW.minusYears(1).minusMonths(6).minusDays(15));
		
		Employee e4 = Employee.maker().firstname("Employee Test4").gender('f')
				.height(1.58F).weight(65.8F)
				.birthdate(LocalDate.of(1971, 10, 9)).register("001002")
				.salary(new BigDecimal("15.99998E2"))
				.hiringDate(NOW.minus(Period.ofYears(25).minusMonths(7)))
				.done();
		e4.setJobFunction(JobFunctions.ENG_SR);
		
		List<Employee> employees = List.of(e1, e2, e3, e4);
		
		try {
			log.info("Begin Transaction");
			em.getTransaction().begin();
			
			log.info("Persist Persons");
			persons.forEach(em::persist);
			log.info("Persist Employees");
			employees.forEach(em::persist);
			log.info("Persist Products");
			products.forEach(em::persist);
			log.info("Persist WishLists");
			wishLists.forEach(em::persist);
			log.info("Persist Orders");
			orders.forEach(em::persist);
			
			log.info("Commit Transaction");
			em.getTransaction().commit();
			
			log.info("Start Results");
			persons.forEach(log::info);
			employees.forEach(log::info);
			products.forEach(log::info);
			wishLists.forEach(log::info);
			orders.forEach(log::info);
			log.info("End Results");
			
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
