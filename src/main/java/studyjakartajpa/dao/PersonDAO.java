package studyjakartajpa.dao;

import studyjakartajpa.model.Person;

public class PersonDAO extends GenericDAO<Person, Long> {
	
	public PersonDAO() {
		super(Person.class);
	}
	
}
