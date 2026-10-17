package studyjakartajpa.base;

import jakarta.persistence.metamodel.SingularAttribute;

public record SortOrder<E>(SingularAttribute<E, ?> attribute, SortDirection direction) {
	
	public enum SortDirection {
		ASC, DESC
	}
	
	public static <T> SortOrder<T> asc(SingularAttribute<T, ?> attribute) {
		return new SortOrder<>(attribute, SortDirection.ASC);
	}
	
	public static <T> SortOrder<T> desc(SingularAttribute<T, ?> attribute) {
		return new SortOrder<>(attribute, SortDirection.DESC);
	}
	
}
