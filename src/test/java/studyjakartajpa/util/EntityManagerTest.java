package studyjakartajpa.util;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

@SuppressWarnings("all")
@TestInstance(Lifecycle.PER_CLASS)
public class EntityManagerTest extends EntityManagerFactoryTest {
	
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	protected static EntityManager em;
	
	@BeforeEach
	void setUpBeforeEach() {
		em = emf.createEntityManager();
	}
	
	@AfterEach
	void tearDownAfterEach() {
		if (em.isOpen()) {
			em.close();
		}
	}
	
}
