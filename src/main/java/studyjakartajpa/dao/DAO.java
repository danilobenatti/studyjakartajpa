package studyjakartajpa.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.metamodel.SingularAttribute;
import studyjakartajpa.base.SortOrder;

public interface DAO<E, I> {
	
	E save(E entity);
	
	List<E> saveAll(Collection<E> entities);
	
	@SuppressWarnings("unchecked")
	default List<E> saveAll(E... e) {
		return saveAll(List.of(e));
	}
	
	void deleteById(I id);
	
	void delete(E entity);
	
	Optional<E> findById(I id);
	
	boolean existsById(I id);
	
	List<E> findAll(int maxResult, int startPosition);
	
	Long count(Map<String, Object> filters);
	
	/**
	 * Advanced, paginated, and sorted queries with full type safety.
	 */
	List<E> findByCriteria(Map<SingularAttribute<E, ?>, Object> filters, Set<SortOrder<E>> sorts, int maxResult,
			int startPosition);
	
	default List<E> findByCriteria(Map<SingularAttribute<E, ?>, Object> filters, Set<SortOrder<E>> sorts) {
		return this.findByCriteria(filters, sorts, Integer.MAX_VALUE, 0);
	}
	
}
