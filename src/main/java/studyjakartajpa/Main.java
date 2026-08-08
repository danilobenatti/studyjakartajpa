package studyjakartajpa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.persistence.EntityManager;
import studyjakartajpa.util.JpaUtil;

public class Main {
	
	static Logger log = LogManager.getLogger(Main.class);
	
	public static void main(String[] args) {
		
		EntityManager em = JpaUtil.getEntityManager();
		
		try {
			em.getTransaction().begin();
			/*
			 * make this
			 */
			log.info("INFO");
			log.warn("WARN");
			log.error("ERROR");
			
			em.getTransaction().commit();
		} catch (Exception e) {
			log.error(e.getMessage());
			if (em.isOpen()) {
				em.getTransaction().rollback();
			}
		} finally {
			if (em.isOpen()) {
				em.close();
				log.traceExit();
			}
		}
	}
	
}
