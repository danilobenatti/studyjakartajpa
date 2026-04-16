package studyjakartajpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(value = { AuditListener.class })
@Table(name = "wishlists", catalog = "jpaforbeginners", schema = "public")
public class WishList implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static Locale locale = Locale.getDefault();
	
	static final NumberFormat CF = NumberFormat.getCurrencyInstance(locale);
	
	@Id
	@EqualsAndHashCode.Include
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "wishlists_seq_generator", sequenceName = "wishlists_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "wishlists_seq_generator")
	private Long id;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "description")
	private String description;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateupdate", insertable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "wishlists_products", catalog = "jpaforbeginners",
		schema = "public",
		joinColumns = @JoinColumn(name = "wishlist_id",
			referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "product_id",
			referencedColumnName = "id"))
	private Set<Product> products = new HashSet<>();
	
	public void setProduct(Product product) {
		Objects.requireNonNull(products, "Products can't be null.");
		this.products.add(product);
	}
	
	public void setProducts(Collection<Product> products) {
		Objects.requireNonNull(products, "Products can't be null.");
		this.products.addAll(products);
	}
	
	public void setProducts(Product... products) {
		Objects.requireNonNull(products, "Products can't be null.");
		Set.of(products).forEach(this.products::add);
	}
	
	@Builder(builderMethodName = "maker", buildMethodName = "done")
	public static WishList of(String title, String description, Person person) {
		
		Objects.requireNonNull(title, "Title can't be null.");
		title = title.trim();
		if (title == null || title.isBlank())
			throw new IllegalArgumentException("Title can't be null or blank.");
		
		Objects.requireNonNull(description, "Description can't be null.");
		description = description.trim();
		if (description == null || description.isBlank())
			throw new IllegalArgumentException(
					"Description can't be null or blank.");
		
		Objects.requireNonNull(person, "Person can't be null.");
		if (person.getClass() == null)
			throw new IllegalArgumentException("Person must be persisted.");
		
		WishList wishList = new WishList();
		wishList.setTitle(title);
		wishList.setDescription(description);
		wishList.setPerson(person);
		return wishList;
	}
	
	@Builder(builderMethodName = "maker", buildMethodName = "done")
	public static WishList of(String title, String description, Person person,
			Collection<Product> products) {
		WishList wishList = new WishList();
		wishList.setTitle(title);
		wishList.setDescription(description);
		wishList.setPerson(person);
		wishList.setProducts(products);
		return wishList;
	}
	
	@Builder(setterPrefix = "with")
	public static WishList of(String title, String description, Person person,
			Product... products) {
		WishList wishList = new WishList();
		wishList.setTitle(title);
		wishList.setDescription(description);
		wishList.setPerson(person);
		wishList.setProducts(products.length > 0 ? Set.of(products) : Set.of());
		return wishList;
	}
	
	@Override
	public String toString() {
		List<String> listProducts = getProducts().stream()
				.map(Product::getProductInfo).sorted().toList();
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("person", this.getPerson().getFirstname())
				.append("title", this.getTitle())
				.append("description", this.getDescription())
				.append("total", CF.format(this.getPriceTotal()))
				.append("products", listProducts).build();
	}
	
	public BigDecimal getPriceTotal() {
		Optional<BigDecimal> total = this.getProducts().stream()
				.map(Product::getPriceWithDiscount).reduce(BigDecimal::add);
		if (total.isPresent())
			return total.get();
		return BigDecimal.ZERO;
	}
}
