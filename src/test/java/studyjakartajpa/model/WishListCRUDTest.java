package studyjakartajpa.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import studyjakartajpa.model.balance.WishListBalance;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class WishListCRUDTest extends EntityManagerTest {
	
	@Test
	@Order(1)
	void includeWishListTest() {
		Person person = em.find(Person.class, 10L);
		
		Product product1 = em.find(Product.class, 10L);
		Product product2 = em.find(Product.class, 11L);
		Product product3 = em.find(Product.class, 12L);
		
		WishList officeList = WishList.of("Office supplies",
				"Monthly purchase for the office", person);
		officeList.setProducts(product1, product2, product3);
		
		em.getTransaction().begin();
		em.persist(officeList);
		em.getTransaction().commit();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<WishList> cq = cb.createQuery(WishList.class);
		Root<WishList> root = cq.from(WishList.class);
		
		cq.select(root).where(cb.equal(root.get(WishList_.person), person));
		
		WishList wishList = em.createQuery(cq).getSingleResult();
		
		System.out.println(wishList);
		Assertions.assertEquals(3, wishList.getProducts().size());
	}
	
	@Test
	@Order(2)
	void selectWishListTest() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<WishList> cq = cb.createQuery(WishList.class);
		Root<WishList> root = cq.from(WishList.class);
		Join<WishList, Person> joinPerson = root.join(WishList_.person);
		
		cq.select(root).where(cb.equal(joinPerson.get(Person_.id), 12L));
		
		List<WishList> wishLists = em.createQuery(cq).getResultList();
		
		wishLists.forEach(System.out::println);
		Assertions.assertEquals(1, wishLists.size());
	}
	
	@Test
	@Order(3)
	void selectWishListTest2() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		
		Root<WishList> root = cq.from(WishList.class);
		SetJoin<WishList, Product> joinProduct = root.join(WishList_.products);
		Join<WishList, Person> joinPerson = root.join(WishList_.person);
		
		Expression<BigDecimal> average = cb.function("round", BigDecimal.class,
				cb.avg(joinProduct.get(Product_.unitPrice)), cb.literal(2));
		
		cq.select(cb.tuple(joinPerson.get(Person_.firstname).alias("firstname"),
				root.get(WishList_.title).alias("title"),
				cb.sum(joinProduct.get(Product_.unitPrice)).alias("total"),
				average.alias("average"),
				cb.count(joinProduct.get(Product_.id)).alias("counter")));
		
		cq.where(cb.equal(joinPerson.get(Person_.id), 12));
		
		cq.groupBy(joinPerson.get(Person_.id), root.get(WishList_.id));
		
		TypedQuery<Tuple> query = em.createQuery(cq);
		
		List<Tuple> result = query.getResultList();
		ArrayList<Object[]> list = new ArrayList<>();
		for (Tuple tuple : result) {
			list.add(new Object[] { tuple.get("firstname"), tuple.get("title"),
					tuple.get("total"), tuple.get("average"),
					tuple.get("counter") });
		}
		list.forEach(o -> System.out.println(Arrays.toString(o)));
		Assertions.assertEquals(1, list.size());
		
		List<WishListBalance> wishListBalances = query.getResultList().stream()
				.map(WishListBalance::new).toList();
		wishListBalances.forEach(System.out::println);
		Assertions.assertEquals(1, wishListBalances.size());
		
	}
	
	@Test
	@Order(4)
	void updateWishListTest() {
		
		Product product = em.find(Product.class, 10L);
		
		WishList wishList = em.find(WishList.class, 10L);
		
		boolean remove = wishList.getProducts().remove(product);
		
		em.getTransaction().begin();
		em.merge(wishList);
		em.getTransaction().commit();
		
		Assertions.assertTrue(remove);
		Assertions.assertEquals(2, wishList.getProducts().size());
		Assertions.assertEquals(19.25, wishList.getPriceTotal());
	}
	
	@Test
	@Order(5)
	void deleteWishListTest() {
		WishList wishList = em.find(WishList.class, 10L);
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaDelete<WishList> delete = cb
				.createCriteriaDelete(WishList.class);
		
		Root<WishList> root = delete.from(WishList.class);
		
		delete.where(cb.equal(root.get(WishList_.id), wishList.getId()));
		
		em.getTransaction().begin();
		int i = em.createQuery(delete).executeUpdate();
		em.getTransaction().commit();
		
		Assertions.assertEquals(1, i);
		Assertions.assertNull(em.find(WishList.class, wishList.getId()));
	}
	
}
