package studyjakartajpa.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUnit;

@SuppressWarnings("all")
@TestInstance(Lifecycle.PER_CLASS)
public class EntityManagerFactoryTest {
	
	@PersistenceUnit(unitName = "persistence-unit")
	protected static EntityManagerFactory emf;
	
	@BeforeAll
	public static void setUpBeforeAll() {
		emf = Persistence.createEntityManagerFactory("persistence-unit");
	}
	
	@AfterAll
	public static void tearDownAfterAll() {
		if (emf.isOpen()) {
			emf.close();
		}
	}
}
