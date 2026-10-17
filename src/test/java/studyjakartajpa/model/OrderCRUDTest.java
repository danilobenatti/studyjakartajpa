package studyjakartajpa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.LocalDateField;
import jakarta.persistence.criteria.Root;
import studyjakartajpa.dao.OrderDAO;
import studyjakartajpa.model.balance.OrderSale;
import studyjakartajpa.model.enums.OrderStatus;
import studyjakartajpa.util.JpaUtils;

@TestMethodOrder(OrderAnnotation.class)
class OrderCRUDTest {
	
	private static final int CURRENT_YEAR = LocalDate.now().getYear();
	
	public static Logger log = LogManager.getLogger();
	private OrderDAO orderDAO;
	private EntityManager em;
	
	@BeforeEach
	void setUp() {
		em = JpaUtils.getEntityManager();
		orderDAO = new OrderDAO();
	}
	
	@AfterAll
	static void tearDownClass() {
		JpaUtils.closeEntityManager();
	}
	
	@Test
	@org.junit.jupiter.api.Order(1)
	void includeOrderTest() {
		Person p1 = em.find(Person.class, 10L);
		Person p2 = em.find(Person.class, 11L);
		
		Product prd1 = em.find(Product.class, 10L);
		Product prd2 = em.find(Product.class, 11L);
		Product prd3 = em.find(Product.class, 12L);
		Product prd4 = em.find(Product.class, 13L);
		
		Order o1 = Order.of(LocalDate.now().plusDays(60), p1, 0.05F, OrderStatus.WAITING);
		
		OrderItem oi1 = OrderItem.of(o1, prd1, 2);
		OrderItem oi2 = OrderItem.of(o1, prd2, 1);
		OrderItem oi3 = OrderItem.of(o1, prd3, 2);
		OrderItem oi4 = OrderItem.of(o1, prd4, 0.5);
		
		o1.addOrderItems(List.of(oi1, oi2, oi3, oi4));
		
		Order o2 = Order.maker().billingDate(LocalDate.now().plusDays(30)).person(p2).discount(0.1F)
				.status(OrderStatus.WAITING).done();
		o2.addOrderItem(OrderItem.of(o2, prd3, 1));
		
		List<Order> orders = orderDAO.saveAll(List.of(o1, o2));
		orders.forEach(log::info);
		
		Order oTest = orderDAO.findById(orders.get(0).getId()).orElse(null);
		log.info(oTest);
		
		Assertions.assertEquals(3, oTest.getOrderItems().size());
		Assertions.assertEquals(new BigDecimal("38.00"), oTest.getTotal());
	}
	
	@Test
	@org.junit.jupiter.api.Order(2)
	void includeOrderTest2() {
		Person p1 = em.find(Person.class, 10L);
		
		Product prd1 = em.find(Product.class, 10L);
		Product prd2 = em.find(Product.class, 11L);
		Product prd3 = em.find(Product.class, 12L);
		Product prd4 = em.find(Product.class, 13L);
		
		Order o1 = Order.of(LocalDate.now().plusDays(60), p1, 0.05F, OrderStatus.WAITING);
		
		OrderItem oi1 = OrderItem.of(o1, prd1, 2);
		OrderItem oi2 = OrderItem.of(o1, prd2, 1);
		OrderItem oi3 = OrderItem.of(o1, prd3, 2);
		OrderItem oi4 = OrderItem.of(o1, prd4, 0.5);
		
		o1.addOrderItems(oi1, oi2, oi3, oi4);
		
		Order o2 = Order.of(LocalDate.now().plusDays(30), p1, 0.1F, OrderStatus.WAITING);
		o2.addOrderItem(OrderItem.of(o2, prd3, 1));
		
		Order o1Saved = orderDAO.save(o1);
		log.info(o1Saved);
		
		Order o2Saved = orderDAO.save(o2);
		log.info(o2Saved);
		
		Assertions.assertEquals(3, o1Saved.getOrderItems().size());
		Assertions.assertEquals(new BigDecimal("38.00"), o1Saved.getTotal());
	}
	
	@Test
	@org.junit.jupiter.api.Order(3)
	void selectOrderTest() {
		List<OrderSale> results = JpaUtils.executeResult(em -> {
			TypedQuery<OrderSale> query = em.createNamedQuery("Orders.AvgMonthly", OrderSale.class);
			
			Byte paid = OrderStatus.PAID.getCode();
			query.setParameter(1, CURRENT_YEAR);
			query.setParameter(2, paid);
			
			return query.getResultList();
		});
		
		results.forEach(log::info);
		Assertions.assertEquals(2, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(4)
	void selectOrderTest1() {
		List<OrderSale> results = JpaUtils.executeResult(em -> {
			TypedQuery<OrderSale> query = em.createNamedQuery("Orders.AvgMonthly2", OrderSale.class);
			
			Byte paid = OrderStatus.PAID.getCode();
			query.setParameter(1, CURRENT_YEAR);
			query.setParameter(2, paid);
			
			return query.getResultList();
		});
		
		results.forEach(log::info);
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
		List<OrderSale> results = JpaUtils.executeResult(em ->{
			TypedQuery<OrderSale> query = em.createQuery(jpql, OrderSale.class);
			
			Byte paid = OrderStatus.PAID.getCode();
			Byte waiting = OrderStatus.WAITING.getCode();
			List<Byte> statuses = Arrays.asList(paid, waiting);
			
			query.setParameter("year", CURRENT_YEAR);
			query.setParameter("status", statuses);
			
			return query.getResultList();
		});
		
		results.forEach(log::info);
		Assertions.assertEquals(4, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(6)
	void selectOrderTest3() {
		List<OrderSale> results = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Tuple> cq = cb.createTupleQuery();
			Root<Order> order = cq.from(Order.class);
			
			Expression<Integer> yearPart = cb.extract(LocalDateField.YEAR, order.get(Order_.billingDate));
			Expression<Integer> monthPart = cb.extract(LocalDateField.MONTH, order.get(Order_.billingDate));
			
			Expression<BigDecimal> avgTotal = cb.function("round", BigDecimal.class, cb.avg(order.get(Order_.total)),
					cb.literal(2));
			
			Expression<Long> count = cb.count(order.get(Order_.id));
			Expression<Byte> status = order.get(Order_.status);
			
			cq.select(cb.tuple(yearPart.alias("year"), monthPart.alias("month"), avgTotal.alias("average"),
					count.alias("counter"), status.alias("status")));
			
			Byte paid = OrderStatus.PAID.getCode();
			Byte waiting = OrderStatus.WAITING.getCode();
			List<Byte> statuses = Arrays.asList(paid, waiting);
			
			cq.where(cb.and(cb.equal(yearPart, CURRENT_YEAR), status.in(statuses)));
			
			cq.groupBy(yearPart, monthPart, status);
			
			cq.orderBy(cb.asc(yearPart), cb.asc(monthPart));
			
			TypedQuery<Tuple> query = em.createQuery(cq);
			
			return query.getResultList().stream().map(OrderSale::new).toList();
		});
		
		results.forEach(log::info);
		Assertions.assertEquals(4, results.size());
	}
	
	@Test
	@org.junit.jupiter.api.Order(7)
	void selectOrderTest4() {
		List<String> results = JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Tuple> cq = cb.createTupleQuery();
			Root<Order> order = cq.from(Order.class);
			
			Expression<LocalDate> billingDate = order.get(Order_.billingDate);
			
			Expression<String> formattedDate = cb.function("to_char", String.class, billingDate, cb.literal("YYYY-MM"));
			
			Expression<Long> countOrders = cb.count(order);
			
			cq.select(cb.tuple(formattedDate.alias("date"), countOrders.alias("count")));
			
			cq.groupBy(formattedDate, billingDate);
			
			TypedQuery<Tuple> query = em.createQuery(cq);
			return query.getResultList().stream()
					.map(t -> StringUtils.joinWith("=", t.get("date"), t.get("count"))).toList();
			
		});
		
		results.forEach(log::info);
		Assertions.assertNotNull(results);
	}
	
	@Test
	@org.junit.jupiter.api.Order(8)
	void updateOrderTest() {
		Order order = em.find(Order.class, 10L);
		
		List<OrderItem> items = order.getOrderItems();
		
		Predicate<OrderItem> isNotActive = i -> !i.getProduct().isActive();
		
		items.removeIf(isNotActive);
		
		orderDAO.save(order);
		log.info(order);
		
		Assertions.assertEquals(3, order.getOrderItems().size());
		Assertions.assertEquals(new BigDecimal("22.99"), order.getTotal());
	}
	
	@Test
	@org.junit.jupiter.api.Order(9)
	void deleteOrderTest() {
		Order order = em.find(Order.class, 11L);
		
		orderDAO.deleteById(order.getId());
		
		Assertions.assertFalse(orderDAO.findById(11L).isPresent());
	}
}
