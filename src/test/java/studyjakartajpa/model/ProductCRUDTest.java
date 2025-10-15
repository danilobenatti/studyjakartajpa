package studyjakartajpa.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.model.enums.ProductUnit;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class ProductCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includeProductTest() {
		
		Product prd = Product.of("Title Product Test Include",
				"Description Product Test", 0.15F, 15.5, ProductUnit.UNITY);
		prd.setValidity(18, ChronoUnit.MONTHS);
		
		em.getTransaction().begin();
		em.persist(prd);
		em.getTransaction().commit();
		
		String qlString = """
			select p from Product p where p.title like :name
			""";
		Query query = em.createQuery(qlString);
		query.setParameter("name", '%' + "Include" + '%');
		Product product = (Product) query.getSingleResult();
		
		System.out.println(product);
		assertEquals("Title Product Test Include", product.getTitle());
		assertEquals(15.5, product.getUnitPrice().doubleValue());
		assertEquals(13.18, product.getPriceWithDiscount().doubleValue());
	}
	
	@Test
	@Order(2)
	void selectProductTest() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Product> cq = cb.createQuery(Product.class);
		
		Root<Product> root = cq.from(Product.class);
		
		cq.select(root).where(cb.equal(root.get(Product_.id), 100));
		
		Product product = em.createQuery(cq).getSingleResult();
		
		assertEquals("Title Product 1", product.getTitle());
		assertEquals(4.95, product.getPriceWithDiscount().doubleValue());
	}
	
	@Test
	@Order(3)
	void updateProductTest() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
		
		Root<Product> root = update.from(Product.class);
		
		update.set(Product_.title, "Title Product 2 Update");
		update.set(Product_.discount, 0.5F);
		
		update.where(cb.equal(root.get(Product_.id), 101));
		
		em.getTransaction().begin();
		int i = em.createQuery(update).executeUpdate();
		em.getTransaction().commit();
		
		assertEquals(1, i);
		assertEquals(BigDecimal.valueOf(5.25),
				((Product) em
						.createQuery("select p from Product p where p.id = 101")
						.getSingleResult()).getPriceWithDiscount());
	}
	
	@Test
	@Order(4)
	void deleteProductTest() {
		Product product = em.find(Product.class, 100L);
		System.out.println(product);
		em.getTransaction().begin();
		em.remove(product);
		em.getTransaction().commit();
		
		assertNull(em.find(Product.class, 100L));
	}
}
