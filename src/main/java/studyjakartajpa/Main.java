package studyjakartajpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import studyjakartajpa.dao.PersonDAO;
import studyjakartajpa.model.Person;
import studyjakartajpa.util.DateUtils;

public class Main {
	
	private static final Logger log = LogManager.getLogger(Main.class);
	
	static PersonDAO personDAO = new PersonDAO();
	
	public static void main(String[] args) {
		
		Person p1 = new Person("Person Test 1", 'M', DateUtils.today());
		
		Person p1Saved = personDAO.save(p1);
		
		log.info(p1Saved);
	}
	
}
