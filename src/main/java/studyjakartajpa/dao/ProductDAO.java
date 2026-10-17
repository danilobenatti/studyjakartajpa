package studyjakartajpa.dao;

import studyjakartajpa.model.Product;

public class ProductDAO extends GenericDAO<Product, Long> {
	
	public ProductDAO() {
		super(Product.class);
	}
	
}
