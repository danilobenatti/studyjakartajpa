package studyjakartajpa.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.TypedQuery;

public class DAO<E> {
	
	@PersistenceUnit(unitName = "persistence-unit")
	private static EntityManagerFactory emf;
	
	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private EntityManager em;
	
	private Class<E> entity;
	
	static {
		try {
			emf = Persistence.createEntityManagerFactory("persistence-unit");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public DAO() {
		this(null);
	}
	
	public DAO(Class<E> entity) {
		this.entity = entity;
		em = emf.createEntityManager();
	}
	
	public DAO<E> begin() {
		em.getTransaction().begin();
		return this;
	}
	
	public DAO<E> commit() {
		em.getTransaction().commit();
		return this;
	}
	
	public DAO<E> add(E entity) {
		em.persist(entity);
		return this;
	}
	
	public DAO<E> addAll(List<E> entities) {
		entities.forEach(em::persist);
		return this;
	}
	
	public DAO<E> update(E entity) {
		em.merge(entity);
		return this;
	}
	
	public DAO<E> updateAll(List<E> entities) {
		entities.forEach(em::merge);
		return this;
	}
	
	public DAO<E> delete(E entity) {
		em.remove(entity);
		return this;
	}
	
	public DAO<E> deleteAll(List<E> entities) {
		entities.forEach(em::remove);
		return this;
	}
	
	public DAO<E> addEntity(E entity) {
		return this.begin().add(entity).commit();
	}
	
	public DAO<E> addAllEntity(List<E> entities) {
		return this.begin().addAll(entities).commit();
	}
	
	public DAO<E> updateEntity(E entity) {
		return this.begin().update(entity).commit();
	}
	
	public DAO<E> updateAllEntity(List<E> entities) {
		return this.begin().updateAll(entities).commit();
	}
	
	public DAO<E> deleteEntity(E entity) {
		return this.begin().delete(entity).commit();
	}
	
	public DAO<E> deleteAllEntity(List<E> entities) {
		return this.begin().deleteAll(entities).commit();
	}
	
	public E searchById(Object id) {
		if (this.entity == null)
			throw new UnsupportedOperationException("Null class");
		String qlString = StringUtils.joinWith(StringUtils.SPACE,
				"select e from", this.entity.getName(), "e where e.id=", id);
		return em.createQuery(qlString, this.entity).getSingleResult();
	}
	
	public E findById(Object id) {
		if (this.entity == null)
			throw new UnsupportedOperationException("Null class");
		return em.find(this.entity, id);
	}
	
	public List<E> listAll(int maxResult, int startPosition) {
		if (this.entity == null)
			throw new UnsupportedOperationException("Null class");
		String qlString = String.join(StringUtils.SPACE, "select e from",
				this.entity.getName(), "e");
		TypedQuery<E> query = em.createQuery(qlString, this.entity);
		query.setMaxResults(maxResult);
		query.setFirstResult(startPosition);
		return query.getResultList();
	}
	
	public List<E> listAll() {
		return this.listAll(5, 0);
	}
	
	public List<E> execute(String qlString, Object... objects) {
		TypedQuery<E> query = em.createNamedQuery(qlString, this.entity);
		int i = 1;
		for (Object object : objects)
			query.setParameter(i++, object);
		return query.getResultList();
	}
	
	public E getFirst(String qlString, Object... objects) {
		List<E> list = execute(qlString, objects);
		return list.isEmpty() ? null : list.get(0);
	}
	
	public void end() {
		if (em.isOpen())
			em.close();
	}
	
}
