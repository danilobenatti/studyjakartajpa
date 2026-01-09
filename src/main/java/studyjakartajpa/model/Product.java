package studyjakartajpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.persistence.annotations.CascadeOnDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import studyjakartajpa.model.enums.ProductUnit;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "products", catalog = "jpaforbeginners", schema = "public")
@NamedQuery(name = "Product.willExpire", query = """
	select p from Product p where p.validity <= :date
	""", resultClass = Product.class)
public class Product implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "products_seq_generator", sequenceName = "products_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "products_seq_generator")
	private long id;
	
	@NonNull
	@Column(name = "title", length = 150, nullable = false)
	private String title;
	
	@NonNull
	@Column(name = "description", length = 255, nullable = false)
	private String description;
	
	@NonNull
	@Column(name = "unitPrice", precision = 18, scale = 2, nullable = false)
	private BigDecimal unitPrice = BigDecimal.ZERO;
	
	@Column(name = "unit", nullable = false)
	private byte unit = ProductUnit.UNITY.getCode();
	
	public ProductUnit getUnit() {
		return ProductUnit.toEnum(this.unit);
	}
	
	public void setUnit(ProductUnit unit) {
		this.unit = unit.getCode();
		
	}
	
	@Column(name = "discount", nullable = false,
		columnDefinition = "NUMERIC(4,2)")
	private float discount = 0F;
	
	@Column(name = "validity")
	private LocalDate validity;
	
	@CascadeOnDelete
	@ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
	private List<WishList> wishLists = new ArrayList<>();
	
	@Column(name = "active")
	private boolean active = true;
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@PrePersist
	protected void whenPersist() {
		this.dateCreate = LocalDateTime.now();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateupdate", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
	@PreUpdate
	protected void whenUpdate() {
		this.dateUpdate = LocalDateTime.now();
	}
	
	@Builder(setterPrefix = "with")
	public static Product of(String title, String description, float discount,
			double unitPrice, ProductUnit unit) {
		Product product = new Product();
		product.setTitle(title);
		product.setDescription(description);
		product.setDiscount(discount);
		product.setUnitPrice(BigDecimal.valueOf(unitPrice).setScale(3,
				RoundingMode.HALF_EVEN));
		product.setUnit(unit);
		return product;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("title", getTitle())
				.append("description", getDescription())
				.append("price", getPriceFormatted())
				.append("unit", getUnit().getValue())
				.append("validity", getValidity()).build();
	}
	
	public String getPriceFormatted() {
		return this.getPriceFormatted(Locale.getDefault());
	}
	
	public String getPriceFormatted(Locale locale) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(locale);
		NumberFormat pf = NumberFormat.getPercentInstance(locale);
		StringBuilder builder = new StringBuilder(
				cf.format(getPriceWithDiscount()));
		if (getDiscount() > 0)
			builder.append("(").append(pf.format(getDiscount())).append(")");
		return builder.toString();
	}
	
	/**
	 * Gets discounted product price if discount is set.
	 * 
	 * @return BigDecimal value
	 */
	public BigDecimal getPriceWithDiscount() {
		return getUnitPrice().multiply(BigDecimal.valueOf(1 - getDiscount()))
				.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	/**
	 * Gets the product price with an additional discount applied over
	 * getPriceWithDiscount.
	 * 
	 * @param discount
	 * @return BigDecimal value
	 */
	public BigDecimal getPriceWithDiscount(float discount) {
		return getPriceWithDiscount().multiply(BigDecimal.valueOf(1 - discount))
				.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	/**
	 * Define expiration datetime (years, months, days, hours, minutes and
	 * seconds) for product. Considers default zone_id.
	 * 
	 * @param i    amount of time
	 * @param unit unit of time
	 * @return object Product with the set expiration date
	 */
	public Product setValidity(long i, TemporalUnit unit) {
		return setValidity(i, unit, ZoneId.systemDefault());
	}
	
	/**
	 * 
	 * 
	 * @param i      amount of time
	 * @param unit   unit of time
	 * @param zoneId Zone ID used for a manufacturing/creation date
	 * @return object Product with the set expiration date
	 */
	public Product setValidity(long i, TemporalUnit unit, ZoneId zoneId) {
		LocalDate date = getDateCreate() != null ? getDateCreate().toLocalDate()
				: LocalDate.now(zoneId);
		setValidity(switch (unit) {
			case ChronoUnit.DAYS -> date.plusDays(i);
			case ChronoUnit.WEEKS -> date.plusWeeks(i);
			case ChronoUnit.MONTHS -> date.plusMonths(i);
			case ChronoUnit.YEARS -> date.plusYears(i);
			default ->
				throw new IllegalArgumentException("Unexpected value: " + unit);
		});
		return this;
	}
	
	public boolean validityIsOk() {
		return validityIsOk(ZoneId.systemDefault());
	}
	
	public boolean validityIsOk(ZoneId zoneId) {
		if (getValidity() != null)
			return getValidity().isAfter(LocalDate.now(zoneId));
		return false;
	}
	
}
