package studyjakartajpa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.DAO;
import studyjakartajpa.model.balance.OrderSale;
import studyjakartajpa.model.enums.OrderStatus;
import studyjakartajpa.util.EntityManagerTest;

@TestMethodOrder(OrderAnnotation.class)
class OrderCRUDTest extends EntityManagerTest {
	
	@Test
	@org.junit.jupiter.api.Order(1)
	void includeOrderTest() {
		Person person = em.find(Person.class, 10L);
		
		Product prd1 = em.find(Product.class, 10L);
		Product prd2 = em.find(Product.class, 11L);
		Product prd3 = em.find(Product.class, 12L);
		Product prd4 = em.find(Product.class, 13L);
		
		Order o1 = Order.of(LocalDate.now().plusDays(60), person, 0.05F,
				OrderStatus.WAITING);
		
		OrderItem i1 = OrderItem.of(o1, prd1, 2);
		OrderItem i2 = OrderItem.of(o1, prd2, 1);
		OrderItem i3 = OrderItem.of(o1, prd3, 2);
		OrderItem i4 = OrderItem.of(o1, prd4, 0.5);
		
		o1.setOrderItems(i1, i2, i3, i4);
		
		Order o2 = Order.of(LocalDate.now().plusDays(30), person, 0.1F,
				OrderStatus.WAITING);
		
		o2.setOrderItem(OrderItem.of(o2, prd3, 1));
		
		em.getTransaction().begin();
		em.persist(o1);
		em.persist(o2);
		em.getTransaction().commit();
		
		Order oTest = em.find(Order.class, o1.getId());
		
		System.out.println(oTest);
		Assertions.assertEquals(4, oTest.getOrderItems().size());
		Assertions.assertEquals(57.47, oTest.getTotal());
		
	}
	
	@Test
	@org.junit.jupiter.api.Order(2)
	void includeOrderTest2() {
Person person = em.find(Person.class, 10L);
		
		Product prd1 = em.find(Product.class, 10L);
		Product prd2 = em.find(Product.class, 11L);
		Product prd3 = em.find(Product.class, 12L);
		Product prd4 = em.find(Product.class, 13L);
		
		Order o1 = Order.of(LocalDate.now().plusDays(60), person, 0.05F,
				OrderStatus.WAITING);
		
		OrderItem i1 = OrderItem.of(o1, prd1, 2);
		OrderItem i2 = OrderItem.of(o1, prd2, 1);
		OrderItem i3 = OrderItem.of(o1, prd3, 2);
		OrderItem i4 = OrderItem.of(o1, prd4, 0.5);
		
		o1.setOrderItems(i1, i2, i3, i4);
		
		Order o2 = Order.of(LocalDate.now().plusDays(30), person, 0.1F,
				OrderStatus.WAITING);
		
		o2.setOrderItem(OrderItem.of(o2, prd3, 1));
		
		DAO<Order> dao = new DAO<>(Order.class);
		
		dao.addEntity(o1).addEntity(o2).end();;
		
		Order oTest = new DAO<>(Order.class).findById(o1.getId());
		
		System.out.println(oTest);
		Assertions.assertEquals(4, oTest.getOrderItems().size());
		Assertions.assertEquals(57.47, oTest.getTotal());
	}
	
	@Test
	@org.junit.jupiter.api.Order(3)
	void selectOrderTest() {
		TypedQuery<OrderSale> query = em.createNamedQuery("Orders.AvgMonthly",
				OrderSale.class);
		
		int yearOfOrders = 2025;
		Byte paid = OrderStatus.PAID.getCode();
		query.setParameter(1, yearOfOrders);
		query.setParameter(2, paid);
		
		List<?> results = query.getResultList();
		
		results.forEach(System.out::println);
		Assertions.assertEquals(2, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(4)
	void selectOrderTest1() {
		TypedQuery<OrderSale> query = em.createNamedQuery("Orders.AvgMonthly2",
				OrderSale.class);
		
		int yearOfOrders = 2025;
		Byte paid = OrderStatus.PAID.getCode();
		query.setParameter(1, yearOfOrders);
		query.setParameter(2, paid);
		
		List<OrderSale> results = query.getResultList();
		
		results.forEach(System.out::println);
		Assertions.assertEquals(2, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(5)
	void selectOrderTest2() {
		String jpql = """
			select new studyjakartajpa.model.balance.OrderSale(
				extract(YEAR from o.billingDate),
				extract(MONTH from o.billingDate),
				round(avg(o.total), 2),
				count(o.id), 
				o.status)
			from Order o
			where extract(YEAR from o.billingDate) = :year
			and o.status in :status
			group by extract(YEAR from o.billingDate), extract(MONTH from o.billingDate), o.status
			order by extract(MONTH from o.billingDate) asc
			""";
		TypedQuery<OrderSale> query = em.createQuery(jpql, OrderSale.class);
		
		int yearOfOrders = 2025;
		Byte paid = OrderStatus.PAID.getCode();
		Byte waiting = OrderStatus.WAITING.getCode();
		List<Byte> statuses = Arrays.asList(paid, waiting);
		
		query.setParameter("year", yearOfOrders);
		query.setParameter("status", statuses);
		
		List<OrderSale> results = query.getResultList();
		results.forEach(System.out::println);
		Assertions.assertEquals(2, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(6)
	void selectOrderTest3() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Order> order = cq.from(Order.class);
		
		Expression<LocalDate> billingDate = order.get(Order_.billingDate);
		
		Expression<Integer> yearPart = cb.function("date_part", Integer.class,
				cb.literal("year"), billingDate);
		Expression<Integer> monthPart = cb.function("date_part", Integer.class,
				cb.literal("month"), billingDate);
		
		Expression<BigDecimal> avgTotal = cb.function("round", BigDecimal.class,
				cb.avg(order.get(Order_.total)), cb.literal(2));
		
		Expression<Long> count = cb.count(order.get(Order_.id));
		Expression<Byte> status = order.get(Order_.status);
		
		cq.select(cb.tuple(yearPart.alias("year"), monthPart.alias("month"),
				avgTotal.alias("average"), count.alias("counter"),
				status.alias("status")));
		
		int yearOfOrders = 2025;
		Byte paid = OrderStatus.PAID.getCode();
		Byte waiting = OrderStatus.WAITING.getCode();
		List<Byte> statuses = Arrays.asList(paid, waiting);
		
		cq.where(cb.and(cb.equal(yearPart, yearOfOrders), status.in(statuses)));
		
		cq.groupBy(yearPart, monthPart, status, billingDate);
		
		cq.orderBy(cb.asc(monthPart));
		
		TypedQuery<Tuple> query = em.createQuery(cq);
		List<OrderSale> os = query.getResultList().stream().map(OrderSale::new)
				.toList();
		os.forEach(System.out::println);
		Assertions.assertEquals(3, os.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(7)
	void selectOrderTest4() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<Order> order = cq.from(Order.class);
		
		Expression<LocalDate> billingDate = order.get(Order_.billingDate);
		
		Expression<String> formattedDate = cb.function("to_char", String.class,
				billingDate, cb.literal("YYYY-MM"));
		
		Expression<Long> countOrders = cb.count(order);
		
		cq.select(cb.tuple(formattedDate.alias("date"),
				countOrders.alias("count")));
		
		cq.groupBy(formattedDate, billingDate);
		
		TypedQuery<Tuple> query = em.createQuery(cq);
		List<String> list = query.getResultList().stream().map(
				t -> StringUtils.joinWith(",", t.get("date"), t.get("count")))
				.toList();
		System.out.println(list);
		Assertions.assertNotNull(list);
	}
	
	@Test
	@org.junit.jupiter.api.Order(8)
	void updateOrderTest() {
		Order o1 = em.find(Order.class, 10L);
		
		List<OrderItem> items = o1.getOrderItems();
		
		java.util.function.Predicate<OrderItem> isNotActive = i -> !i
				.getProduct().isActive();
		
		items.removeIf(isNotActive);
		
		em.getTransaction().begin();
		em.merge(o1);
		em.getTransaction().commit();
		
		System.out.println(o1);
		Assertions.assertEquals(3, o1.getOrderItems().size());
		Assertions.assertEquals(22.99, o1.getTotal());
	}
	
	@Test
	@org.junit.jupiter.api.Order(9)
	void deleteOrderTest() {
		Order order = em.find(Order.class, 10L);
		
		em.getTransaction().begin();
		em.remove(order);
		em.getTransaction().commit();
		
		Assertions.assertNull(em.find(Order.class, order.getId()));
	}
}
