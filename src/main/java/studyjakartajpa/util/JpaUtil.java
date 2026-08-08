package studyjakartajpa.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public final class JpaUtil {
	
	static final String PERSISTENCE_UNIT = "persistence-unit";

	static ThreadLocal<EntityManager> threadEntityManager = new ThreadLocal<>();
	
	static EntityManagerFactory emf;
	
	static Logger log = LogManager.getLogger(JpaUtil.class);
	
	private JpaUtil() { // Prevents instantiation
	}
	
	static {
		try {
			emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
			log.info("Start: {}", emf.getName());
		} catch (Exception e) {
			log.error("Failed to initialize the EntityManagerFactory.", e);
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static EntityManager getEntityManager() {
		EntityManager em = threadEntityManager.get();
		if (em == null || !em.isOpen()) {
			em = emf.createEntityManager();
			threadEntityManager.set(em);
		}
		return em;
	}
	
	public static void closeEntityManager() {
		EntityManager em = threadEntityManager.get();
		if (em != null) {
			try {
				if (em.isOpen()) {
					EntityTransaction transaction = em.getTransaction();
					if (transaction.isActive()) {
						log.warn(
								"""
									Active transaction detected when closing the EntityManager.
									Performing automatic rollback.
									""");
						transaction.rollback();
					}
					em.close();
				}
			} catch (Exception e) {
				log.error("Error closing EntityManager", e);
			} finally {
				threadEntityManager.remove(); // Prevents memory leaks
			}
		}
	}
	
	public static void closeEntityManagerFactory() {
		closeEntityManager();
		if (emf != null && emf.isOpen()) {
			emf.close();
			log.info("EntityManagerFactory closed successfully.");
		}
	}
	
}
