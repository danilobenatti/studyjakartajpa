package studyjakartajpa.dao;

import studyjakartajpa.model.Order;

public class OrderDAO extends GenericDAO<Order, Long> {
	
	public OrderDAO() {
		super(Order.class);
	}
	
}
