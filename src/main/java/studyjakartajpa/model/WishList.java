package studyjakartajpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "wishlists", catalog = "jpaforbeginners", schema = "public")
public class WishList implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@PrePersist
	protected void whenPersist() {
		this.dateCreate = LocalDateTime.now();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateupdate", insertable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
	@PreUpdate
	protected void whenUpdate() {
		this.dateUpdate = LocalDateTime.now();
	}
	
	@ManyToOne
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
		this.products.add(product);
	}
	
	public void setProducts(Set<Product> products) {
		this.products.addAll(products);
	}
	
	public void setProducts(List<Product> products) {
		this.products.addAll(products);
	}
	
	public void setProducts(Product... products) {
		Arrays.asList(products).forEach(this.products::add);
	}
	
	@Builder(setterPrefix = "with")
	public static WishList of(String title, String description, Person person) {
		WishList wishList = new WishList();
		wishList.setTitle(title);
		wishList.setDescription(description);
		wishList.setPerson(person);
		return wishList;
	}
	
	@Builder(setterPrefix = "with")
	public static WishList of(String title, String description, Person person,
			Set<Product> products) {
		WishList wishList = new WishList();
		wishList.setTitle(title);
		wishList.setDescription(description);
		wishList.setPerson(person);
		wishList.setProducts(products);
		return wishList;
	}
	
	@Override
	public String toString() {
		List<String> productsList = getProducts().stream()
				.map(Product::getTitle).sorted().collect(Collectors.toList());
		NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("title", getTitle())
				.append("description", getDescription())
				.append("person", getPerson().getFirstname())
				.append("total", cf.format(getPriceTotal()))
				.append("products", productsList).build();
	}
	
	public double getPriceTotal() {
		Optional<BigDecimal> total = this.products.stream()
				.map(Product::getPriceWithDiscount).reduce(BigDecimal::add);
		if (total.isPresent()) {
			return total.get().doubleValue();
		} else {
			return BigDecimal.ZERO.doubleValue();
		}
	}
}
