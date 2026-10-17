package studyjakartajpa.util;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public final class JpaUtils {
	
	private static final String PERSISTENCE_UNIT_NAME = "jpaStudyPU";
	private static final Logger log = LogManager.getLogger(JpaUtils.class);
	private static final EntityManagerFactory emf;
	private static final ThreadLocal<EntityManager> threadLocal = new ThreadLocal<>();
	
	private JpaUtils() { // Prevents instantiation
		throw new AssertionError("Do not instantiate this utility class");
	}
	
	static {
		try {
			emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
			log.info("EntityManagerFactory successfully started");
			Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() -> {
				log.info("Shutdown hook activated. Closing JPA resources");
				closeEntityManagerFactory();
				LogManager.shutdown();
			}));
		} catch (Exception e) {
			log.error("Failed to initialize the EntityManagerFactory", e);
			LogManager.shutdown();
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static EntityManager getEntityManager() {
		EntityManager em = threadLocal.get();
		if (em == null || !em.isOpen()) {
			em = emf.createEntityManager();
			threadLocal.set(em);
		}
		return em;
	}
	
	public static void beginTransaction() {
		EntityManager em = getEntityManager();
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
	}
	
	public static void commitTransaction() {
		EntityManager em = threadLocal.get();
		if (em != null && em.getTransaction().isActive()) {
			em.getTransaction().commit();
		}
	}
	
	public static void rollbackTransaction() {
		EntityManager em = threadLocal.get();
		if (em != null) {
			try {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			} finally {
				closeEntityManager();
			}
		}
		
	}
	
	public static void closeEntityManager() {
		EntityManager em = threadLocal.get();
		if (em != null) {
			try {
				if (em.isOpen()) {
					em.close();
				}
			} finally {
				threadLocal.remove();
			}
		}
	}
	
	public static void closeEntityManagerFactory() {
		if (emf != null && emf.isOpen()) {
			emf.close();
			log.info("EntityManagerFactory successfully closed");
		}
	}
	
	private static void rollbackIfActive(EntityTransaction transaction, Throwable throwable) {
		if (transaction != null && transaction.isActive()) {
			log.warn("Active transaction detected at close. Execute rollback");
			try {
				transaction.rollback();
			} catch (Exception e) {
				log.error("Failed to perform automatic rollback", e);
				if (throwable != null) {
					throwable.addSuppressed(e);
				}
			}
		}
	}
	
	/**
	 * Executes a standalone, non-returning operation within a transaction.
	 * Recommended only for quick, independent script operations.
	 * 
	 * @param action
	 */
	public static void execute(Consumer<EntityManager> action) {
		executeResult(em -> {
			action.accept(em);
			return null;
		});
	}
	
	/**
	 * 
	 * @param <R>
	 * @param action
	 * @return
	 */
	public static <R> R executeResult(Function<EntityManager, R> action) {
		EntityManager em = getEntityManager();
		EntityTransaction et = em.getTransaction();
		boolean alreadyActive = et.isActive();
		try {
			if (!alreadyActive) {
				et.begin();
			}
			R result = action.apply(em);
			if (!alreadyActive) {
				et.commit();
			}
			return result;
		} catch (Throwable e) {
			if (!alreadyActive) {
				rollbackIfActive(et, e);
			}
			throw (e instanceof RuntimeException re) ? re : new RuntimeException(e);
		} finally {
			if (!alreadyActive) {
				closeEntityManager();
			}
		}
	}
	
}
