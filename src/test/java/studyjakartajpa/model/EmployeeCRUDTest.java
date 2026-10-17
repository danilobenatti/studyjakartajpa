package studyjakartajpa.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import studyjakartajpa.dao.EmployeeDAO;
import studyjakartajpa.model.enums.JobFunctions;
import studyjakartajpa.util.JpaUtils;

@TestMethodOrder(OrderAnnotation.class)
class EmployeeCRUDTest {
	
	public static Logger log = LogManager.getLogger();
	private final EmployeeDAO employeeDAO = new EmployeeDAO();
	
	@BeforeEach
	void setUp() {
		JpaUtils.closeEntityManager();
	}
	
	@AfterAll
	static void tearDownClass() {
		JpaUtils.closeEntityManager();
	}
	
	@Test
	@Order(1)
	void includeEmployeeTest() {
		Employee e = new Employee("Employee Include Test1", 'M', LocalDate.of(1990, Month.JANUARY, 1), "988677",
				JobFunctions.ENG_JR, 1999.995, LocalDate.now().minusYears(3));
		e.setWeight(89.5F);
		e.setHeight(1.80F);
		e.setEmails("employeeTest@wail.tw", "employeeTest2@wail.tw");
		e.setPhones(Map.of('W', "(18)7979-8787", 'M', "(18)95656-1414"));
		e.setAddress(
				Address.of("1486", "Buena Vista Dr", "Lake Buena Vista", "Orlando", "FL", "USA", "32830", true, e));
		
		employeeDAO.save(e);
		
		log.info(e);
		Assertions.assertEquals("Employee Include Test1", e.getFirstName());
	}
	
	@Test
	@Order(2)
	void updateEmployeeTest() {
		Employee e = employeeDAO.findById(10L).get();
		
		e.setWeight(89.8F);
		e.setHeight(1.95F);
		
		employeeDAO.save(e);
		
		log.info(e);
		Assertions.assertEquals("[23.6] at ideal weight", e.calcIMC());
	}
	
	@Test
	@Order(3)
	void deleteEmployeeTest() {
		Employee e = employeeDAO.findById(100L).orElse(null);
		
		employeeDAO.delete(e);
		
		log.info(e);
		Assertions.assertEquals(Optional.empty(), employeeDAO.findById(100L));
	}
	
	@Test
	@Order(4)
	void findEmployeeByIdTest() {
		Employee e = employeeDAO.findById(12L).get();
		
		log.info(e);
		Assertions.assertNotNull(e);
	}
}
