package studyjakartajpa.dao;

import studyjakartajpa.model.Employee;

public class EmployeeDAO extends GenericDAO<Employee, Long> {
	
	public EmployeeDAO() {
		super(Employee.class);
	}
	
}
