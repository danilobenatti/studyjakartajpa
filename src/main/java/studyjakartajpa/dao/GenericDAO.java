package studyjakartajpa.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import studyjakartajpa.base.SortOrder;
import studyjakartajpa.domain.exception.EntityNotFoundException;
import studyjakartajpa.domain.exception.RepositoryException;
import studyjakartajpa.util.JpaUtils;

public abstract class GenericDAO<E, I> implements DAO<E, I> {
	
	private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
	
	private final Class<E> clazzEntity;
	
	protected GenericDAO(Class<E> clazzEntity) {
		this.clazzEntity = clazzEntity;
	}
	
	private List<Expression<?>> getIdExpressions(EntityManager em, Root<E> root) {
		EntityType<E> entityType = em.getMetamodel().entity(this.clazzEntity);
		
		// 1. Scenario: Simple @Id or full @EmbeddedId
		if (entityType.hasSingleIdAttribute()) {
			return getSimpleIdExpression(entityType, root);
		}
		// 2. Scenario: @IdClass (Multiple attributes annotated with @Id within the
		// class itself)
		return getCompositeIdExpressions(entityType, root);
	}
	
	private List<Expression<?>> getSimpleIdExpression(EntityType<E> entityType, Root<E> root) {
		String idName = entityType.getId(entityType.getIdType().getJavaType()).getName();
		return List.of(root.get(idName));
	}
	
	private List<Expression<?>> getCompositeIdExpressions(EntityType<E> entityType, Root<E> root) {
		try {
			return entityType.getIdClassAttributes().stream().map(attr -> root.get(attr.getName()))
					.collect(Collectors.toList());
		} catch (Exception ex) {
			return List.of(root.get("id"));
		}
	}
	
	@Override
	public E save(E entity) { // INSERT (if new) or UPDATE (if already existing)
		if (entity == null) {
			return null;
		}
		return JpaUtils.executeResult(em -> {
			E e = em.merge(entity);
			em.flush();
			return e;
		});
	}
	
	@Override
	public List<E> saveAll(Collection<E> entities) {
		if (entities == null || entities.isEmpty()) {
			return Collections.emptyList();
		}
		return JpaUtils.executeResult(em -> {
			List<E> list = new ArrayList<>();
			
			for (E entity : entities) {
				if (entity == null) {
					continue;
				}
				E saved = em.merge(entity);
				list.add(saved);
			}
			em.flush();
			return list;
		});
	}
	
	@Override
	public void deleteById(I id) {
		if (id == null) {
			return;
		}
		JpaUtils.execute(em -> {
			E entity = em.find(this.clazzEntity, id);
			if (entity == null) {
				throw new EntityNotFoundException(String.format("Could not delete: %s with ID %s not found.",
						this.clazzEntity.getSimpleName(), id));
			}
			em.remove(entity);
		});
	}
	
	@Override
	public void delete(E e) {
		if (e == null) {
			return;
		}
		JpaUtils.execute(em -> {
			E entity = em.contains(e) ? e : em.merge(e);
			em.remove(entity);
		});
	}
	
	public void delete(Optional<E> e) {
		e.ifPresent(this::delete);
	}
	
	@Override
	public boolean existsById(I id) {
		if (id == null) {
			return false;
		}
		return JpaUtils.executeResult(em -> {
			try {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Object> query = cb.createQuery(Object.class);
				Root<E> root = query.from(this.clazzEntity);
				
				// 1. Gets the key expressions
				List<Expression<?>> idExpressions = getIdExpressions(em, root);
				List<Predicate> predicates = new ArrayList<>(idExpressions.size());
				
				// 2. If it is a simple key or @EmbeddedId (only 1 expression returned)
				if (idExpressions.size() == 1) {
					predicates.add(cb.equal(idExpressions.get(0), id));
				}
				
				// 3. If it is @IdClass (Multiple expressions returned)
				else {
					EntityType<E> entityType = em.getMetamodel().entity(this.clazzEntity);
					var idClassAttributes = entityType.getIdClassAttributes();
					for (var attr : idClassAttributes) {
						// Extracts the value from the composite ID object using reflection/getter
						Object fieldValue = getFieldValue(id, attr.getName());
						Expression<?> expression = idExpressions.stream()
								.filter(exp -> exp.toString().contains(attr.getName())).findFirst()
								.orElse(root.get(attr.getName()));
						predicates.add(cb.equal(expression, fieldValue));
					}
					
				}
				// Use SELECT 1 so that EclipseLink does not waste processing power fetching
				// unnecessary data.
				query.select(cb.literal(1)).where(cb.and(predicates.toArray(new Predicate[0])));
				
				// setMaxResults(1) ensures that the database stops searching as soon as it
				// finds the first occurrence.
				Object result = em.createQuery(query).setMaxResults(1).getSingleResult();
				
				return result != null;
			} catch (NoResultException e) {
				return false; // If Jakarta Persistence finds nothing, the ID does not exist
			} catch (Exception e) {
				throw new RuntimeException("Error checking for the existence of the record by ID", e);
			}
		});
	}
	
	/**
	 * Extracts the attribute value from the ID class using only public methods
	 * (getters or records). Resolves security warnings related to deep reflection
	 * and generic exception handling.
	 * 
	 * @param object    The object representing the Composite ID.
	 * @param fieldName The name of the field from which the value is to be
	 *                  obtained.
	 * @return The value of the extracted field
	 * @throws RepositoryException If a reflection failure occurs (replace with your
	 *                             infrastructure exception).
	 */
	private Object getFieldValue(Object object, String fieldName) {
		if (object == null || fieldName == null || fieldName.isBlank()) {
			throw new RepositoryException("Invalid object or field name for ID extraction");
		}
		
		String cacheKey = object.getClass().getName() + "#" + fieldName;
		
		try {
			Method method = METHOD_CACHE.computeIfAbsent(cacheKey, k -> {
				try {
					String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
					return object.getClass().getMethod(getterName);
				} catch (NoSuchMethodException e) {
					try {
						return object.getClass().getMethod(fieldName);
					} catch (NoSuchMethodException ex) {
						throw new RuntimeException(ex);
					}
				}
			});
			return method.invoke(object);
		} catch (Exception e) {
			throw new RepositoryException("Failure when accessing the identifier via reflection: " + fieldName, e);
		}
	}
	
	@Override
	public Optional<E> findById(I id) {
		if (id == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(JpaUtils.executeResult(em -> em.find(this.clazzEntity, id)));
	}
	
	@Override
	public List<E> findAll(int maxResult, int startPosition) {
		return JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<E> cq = cb.createQuery(this.clazzEntity);
			Root<E> root = cq.from(this.clazzEntity);
			cq.select(root);
			
			List<Expression<?>> idExpressions = getIdExpressions(em, root);
			List<Order> orders = idExpressions.stream().map(cb::asc).toList();
			cq.orderBy(orders);
			
			TypedQuery<E> query = em.createQuery(cq);
			configurePagination(query, maxResult, startPosition);
			return query.getResultList();
		});
	}
	
	public List<E> execute(String qlString, Map<String, Object> namedParameters, int maxResult, int startPosition) {
		return JpaUtils.executeResult(em -> {
			TypedQuery<E> query = em.createQuery(qlString, clazzEntity);
			if (namedParameters != null) {
				namedParameters.forEach(query::setParameter);
			}
			configurePagination(query, maxResult, startPosition);
			return query.getResultList();
		});
	}
	
	public List<E> execute(String qlString, Map<String, Object> namedParameters) {
		return this.execute(qlString, namedParameters, -1, 0);
	}
	
	public List<E> executePaginated(String qlString, Map<String, Object> namedParameters) {
		return this.execute(qlString, namedParameters, 10, 0);
	}
	
	/**
	 * Counts the number of records in the database by applying filters based on
	 * simple equality (field = :value). Usage example: Map.of("status",
	 * Status.ATIVO, "city", "São Paulo")
	 */
	@Override
	public Long count(Map<String, Object> filters) {
		return JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<E> root = cq.from(this.clazzEntity);
			
			cq.select(cb.count(root));
			
			if (filters != null && !filters.isEmpty()) {
				List<Predicate> predicates = new ArrayList<>();
				filters.forEach((attribute, value) -> {
					if (value instanceof String strValue) {
						if (strValue.contains("%")) {
							Expression<String> stringExpression = root.get(attribute).as(String.class);
							predicates.add(cb.like(cb.lower(stringExpression), strValue.toLowerCase()));
						} else {
							predicates.add(cb.equal(root.get(attribute), value));
						}
					} else {
						predicates.add(cb.equal(root.get(attribute), value));
					}
				});
				cq.where(cb.and(predicates.toArray(Predicate[]::new)));
			}
			return em.createQuery(cq).getSingleResult();
		});
	}
	
	@Override
	public List<E> findByCriteria(Map<SingularAttribute<E, ?>, Object> filters, Set<SortOrder<E>> sorts, int maxResult,
			int startPosition) {
		return JpaUtils.executeResult(em -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<E> cq = cb.createQuery(this.clazzEntity);
			Root<E> root = cq.from(this.clazzEntity);
			
			cq.select(root);
			
			// 1. Dynamic Application of Filters (WHERE)
			if (filters != null && !filters.isEmpty()) {
				List<Predicate> predicates = new ArrayList<>();
				filters.forEach((attribute, value) -> {
					if (value instanceof String strValue) {
						if (strValue.contains("%")) {
							Expression<String> stringExpression = root.get(attribute).as(String.class);
							predicates.add(cb.like(cb.lower(stringExpression), strValue.toLowerCase()));
						} else {
							predicates.add(cb.equal(root.get(attribute), value));
						}
					} else {
						predicates.add(cb.equal(root.get(attribute), value));
					}
				});
				cq.where(cb.and(predicates.toArray(new Predicate[0])));
			}
			// 2. Dynamic Sorting Application (ORDER BY)
			if (sorts != null && !sorts.isEmpty()) {
				List<Order> jpaOrders = sorts.stream().map(sort -> switch (sort.direction()) {
					case ASC -> cb.asc(root.get(sort.attribute()));
					case DESC -> cb.desc(root.get(sort.attribute()));
				}).toList();
				cq.orderBy(jpaOrders);
			}
			// 3. Query Execution with Pagination Limits
			TypedQuery<E> query = em.createQuery(cq);
			configurePagination(query, maxResult, startPosition);
			return query.getResultList();
		});
	}
	
	private void configurePagination(TypedQuery<E> query, int maxResult, int startPosition) {
		if (maxResult < -1 || startPosition < 0) {
			throw new IllegalArgumentException("Invalid pagination parameters. Values cannot be negative");
		}
		if (maxResult > 0) {
			query.setMaxResults(maxResult);
		}
		if (startPosition > 0) {
			query.setFirstResult(startPosition);
		}
	}
}
