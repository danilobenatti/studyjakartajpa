package studyjakartajpa.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.DAO;
import studyjakartajpa.model.enums.ProductUnit;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class ProductCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includeProductTest() {
		
		Product prd = Product.of("Title Product Test1 Include",
				"Description Product Test", 0.15F, 15.5, ProductUnit.UNITY);
		prd.setValidity(18, ChronoUnit.MONTHS);
		
		em.getTransaction().begin();
		em.persist(prd);
		em.getTransaction().commit();
		
		String qlString = """
			select p from Product p
			where lower(p.title) like lower(concat('%', :name, '%'))
			""";
		Query query = em.createQuery(qlString);
		query.setParameter("name", "test1");
		Product product = (Product) query.getSingleResult();
		
		System.out.println(product);
		Assertions.assertEquals("Title Product Test1 Include",
				product.getTitle());
		Assertions.assertEquals(15.5, product.getUnitPrice().doubleValue());
		Assertions.assertEquals(13.18,
				product.getPriceWithDiscount().doubleValue());
	}
	
	@Test
	@Order(2)
	void includeProductTest2() {
		DAO<Product> dao = new DAO<>(Product.class);
		
		Product product = Product.of("Title Product Test2 Include",
				"Description Product Test2", 0.15F, 15.5, ProductUnit.UNITY);
		product.setValidity(18, ChronoUnit.MONTHS);
		
		dao.addEntity(product).end();
		
		Assertions.assertEquals(101, product.getId());
	}
	
	@Test
	@Order(3)
	void selectProductTest() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<Product> cq = cb.createQuery(Product.class);
		
		Root<Product> root = cq.from(Product.class);
		
		cq.select(root).where(cb.equal(root.get(Product_.title),
				"Title Product Test1 Include"));
		
		Product product = em.createQuery(cq).getSingleResult();
		
		Assertions.assertEquals("Title Product Test1 Include",
				product.getTitle());
		Assertions.assertEquals(13.18, product.getPriceWithDiscount()
				.round(new MathContext(4)).doubleValue());
	}
	
	@Test
	@Order(4)
	void selectProductTest2() {
		TypedQuery<Product> query = em.createNamedQuery("Product.willExpire",
				Product.class);
		query.setParameter("date", LocalDate.now().plusYears(1));
		
		List<Product> products = query.getResultList();
		products.forEach(System.out::println);
		Assertions.assertEquals(2, products.size());
	}
	
	@Test
	@Order(5)
	void updateProductTest() {
		Product product = em.find(Product.class, 11L);
		product.setTitle("Title Product 2 Update");
		product.setDescription("Description Product 2 Update");
		product.setDiscount(0.5F);
		
		em.getTransaction().begin();
		em.merge(product);
		em.getTransaction().commit();
		
		String qlString = """
			select p from Product p where p.id = :id
			""";
		Product p = (Product) em.createQuery(qlString).setParameter("id", 11L)
				.getSingleResult();
		
		System.out.println(p);
		MathContext mc = new MathContext(3);
		Assertions.assertEquals(BigDecimal.valueOf(5.25),
				p.getPriceWithDiscount().round(mc));
	}
	
	@Test
	@Order(6)
	void updateProductTest2() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
		
		Root<Product> root = update.from(Product.class);
		
		update.set(Product_.title, "Title Product 2 Update2");
		update.set(Product_.description, "Description Product 2 Update2");
		update.set(Product_.discount, 0.5F);
		
		update.where(cb.equal(root.get(Product_.id), 11L));
		
		em.getTransaction().begin();
		int i = em.createQuery(update).executeUpdate();
		em.getTransaction().commit();
		
		Assertions.assertEquals(1, i);
		MathContext mc = new MathContext(3);
		Assertions.assertEquals(BigDecimal.valueOf(5.25),
				((Product) em
						.createQuery("select p from Product p where p.id = 11")
						.getSingleResult()).getPriceWithDiscount().round(mc));
	}
	
	@Test
	@Order(7)
	void deleteProductTest() {
		Product product = em.find(Product.class, 100L);
		System.out.println(product);
		if (product != null) {
			em.getTransaction().begin();
			em.remove(product);
			em.getTransaction().commit();
		}
		Assertions.assertNull(em.find(Product.class, product.getId()));
	}
	
	@Test
	@Order(8)
	void deleteProductTest2() {
		DAO<Product> dao = new DAO<>(Product.class);
		
		Product product = dao.findById(101L);
		System.out.println(product);
		
		if (product != null) {
			dao.deleteEntity(product).end();
		}
		
		Assertions.assertNull(em.find(Product.class, product.getId()));
	}
}
