package studyjakartajpa.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import studyjakartajpa.dao.DAO;
import studyjakartajpa.model.enums.JobFunctions;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class EmployeeCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includeEmployeeTest() {
		Employee e = new Employee("Employee Include Test1", 'M',
				LocalDate.of(1990, Month.JANUARY, 1), "987654",
				JobFunctions.ENG_JR, 1999.995, LocalDate.now().minusYears(3));
		e.setEmails("employeeTest@wail.tw", "employeeTest2@wail.tw");
		e.setPhones(Map.of('W', "(18)7979-8787", 'M', "(18)95656-1414"));
		e.setAddress(Address.of("1486", "Buena Vista Dr", "Lake Buena Vista",
				"Orlando", "FL", "USA", "32830", true, e));
		
		DAO<Employee> dao = new DAO<>(Employee.class);
		
		dao.addEntity(e).end();
		
		log.info(e);
		Assertions.assertEquals("Employee Include Test1", e.getFirstname());
	}
	
}
