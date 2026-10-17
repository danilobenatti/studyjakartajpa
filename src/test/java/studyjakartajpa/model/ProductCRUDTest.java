package studyjakartajpa.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.ProductDAO;
import studyjakartajpa.model.enums.ProductUnit;
import studyjakartajpa.util.JpaUtils;

@TestMethodOrder(OrderAnnotation.class)
class ProductCRUDTest {
	
	public static Logger log = LogManager.getLogger();
	private static ProductDAO productDAO;
	
	@BeforeEach
	void setUpBeforeEach() {
		productDAO = new ProductDAO();
	}
	
	@AfterAll
	static void tearDownClass() {
		JpaUtils.closeEntityManager();
	}
	
	@Test
	@Order(1)
	void includeProductTest() {
		
		Product prd = Product.of("Title Product Test1 Include", "Description Product Test1", new BigDecimal("0.15"),
				15.5, ProductUnit.UNITY);
		prd.setValidity(18, ChronoUnit.MONTHS);
		
		productDAO.save(prd);
		
		Product product = JpaUtils.executeResult(em -> {
			String qlString = """
				select p from Product p
				where lower(p.title) like lower(concat('%', :name, '%'))
				""";
			TypedQuery<Product> query = em.createQuery(qlString, Product.class);
			query.setParameter("name", "test1 include");
			return query.getResultList().getFirst();
			
		});
		
		log.info(product);
		Assertions.assertTrue(product.getTitle().contains("Test1 Include"));
		Assertions.assertEquals(15.5, product.getUnitPrice().doubleValue());
		Assertions.assertEquals(13.18, product.getPriceWithDiscount().doubleValue());
	}
	
	@Test
	@Order(2)
	void includeProductTest2() {
		Product product1 = Product.of("Title Product Test2 Include", "Description Product Test2",
				new BigDecimal("0.15"), 15.5, ProductUnit.UNITY);
		product1.setValidity(18, ChronoUnit.MONTHS);
		
		Product product2 = Product.maker().title("Title Product Test3 Include").description("Description Product Test3")
				.unitPrice(new BigDecimal("15.5")).unit(ProductUnit.UNITY).done();
		product2.setValidity(18, ChronoUnit.MONTHS);
		
		List<Product> products = Arrays.asList(product1, product2);
		
		productDAO.saveAll(products);
		
		products.forEach(log::info);
		Assertions.assertNotNull(product1, "Product not found.");
		Assertions.assertNotNull(product2, "Product not found.");
	}
	
	@Test
	@Order(3)
	void selectProductTest() {
		Product product = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			
			CriteriaQuery<Product> cq = cb.createQuery(Product.class);
			
			Root<Product> root = cq.from(Product.class);
			
			cq.select(root).where(cb.equal(root.get(Product_.title), "Title Product 4"));
			
			return em.createQuery(cq).getSingleResult();
			
		});
		
		log.info(product);
		Assertions.assertTrue(StringUtils.containsAny("product 4", product.getTitle()));
		Assertions.assertEquals(41.0, product.getPriceWithDiscount().round(new MathContext(4)).doubleValue());
	}
	
	@Test
	@Order(4)
	void selectProductTest2() {
		List<Product> products = JpaUtils.executeResult(em -> {
			TypedQuery<Product> query = em.createNamedQuery("Product.willExpire", Product.class);
			query.setParameter("date", LocalDate.now().plusYears(1));
			
			return query.getResultList();
		});
		
		products.forEach(log::info);
		Assertions.assertEquals(2, products.size());
	}
	
	@Test
	@Order(5)
	void updateProductTest() {
		Product product = productDAO.findById(11L).orElse(null);
		product.setTitle("Title Product 2 Update");
		product.setDescription("Description Product 2 Update");
		product.setDiscount(new BigDecimal("0.5"));
		
		productDAO.save(product);
		
		Product p = JpaUtils.executeResult(em -> {
			String qlString = """
				select p from Product p where p.id = :id
				""";
			return (Product) em.createQuery(qlString).setParameter("id", 11L).getSingleResult();
		});
		
		log.info(p);
		MathContext mc = new MathContext(3);
		Assertions.assertEquals(BigDecimal.valueOf(5.25), p.getPriceWithDiscount().round(mc));
	}
	
	@Test
	@Order(6)
	void updateProductTest2() {
		int i = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			
			CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
			
			Root<Product> root = update.from(Product.class);
			
			update.set(Product_.title, "Title Product 2 Update2");
			update.set(Product_.description, "Description Product 2 Update2");
			update.set(Product_.discount, new BigDecimal("0.5"));
			
			update.where(cb.equal(root.get(Product_.id), 11L));
			
			return em.createQuery(update).executeUpdate();
			
		});
		
		Assertions.assertEquals(1, i);
		MathContext mc = new MathContext(3);
		Assertions.assertEquals(BigDecimal.valueOf(5.25),
				((Product) JpaUtils.getEntityManager().createQuery("select p from Product p where p.id = 11")
						.getSingleResult()).getPriceWithDiscount().round(mc));
	}
	
	@Test
	@Order(7)
	void deleteProductTest() {
			JpaUtils.execute(em -> {
				Product product = em.find(Product.class, 100L);
				log.info(product);
				em.remove(product);
			});
		
		Assertions.assertFalse(productDAO.findById(100L).isPresent());
	}
	
	@Test
	@Order(8)
	void deleteProductTest2() {
		Product product = productDAO.findById(101L).orElse(null);
		log.info(product);
		
		if (product != null)
			productDAO.delete(product);
		
		Assertions.assertFalse(productDAO.findById(product.getId()).isPresent());
	}
}
