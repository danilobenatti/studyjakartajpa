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
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.persistence.annotations.CascadeOnDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import studyjakartajpa.model.enums.ProductUnit;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@EntityListeners(value = { AuditListener.class })
@Table(name = "products", catalog = "jpaforbeginners", schema = "public")
@NamedQuery(name = "Product.willExpire", query = """
	select p from Product p where p.validity <= :date
	""", resultClass = Product.class)
public class Product implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	static Locale locale = Locale.getDefault();
	
	static ZoneId systemDefault = ZoneId.systemDefault();
	
	static final NumberFormat CF = NumberFormat.getCurrencyInstance(locale);
	
	static final NumberFormat PF = NumberFormat.getPercentInstance(locale);
	
	@Id
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "products_seq_generator", sequenceName = "products_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "products_seq_generator")
	private long id;
	
	@lombok.NonNull
	@Column(name = "title", length = 150, nullable = false)
	private String title;
	
	@lombok.NonNull
	@Column(name = "description", length = 255, nullable = false)
	private String description;
	
	@lombok.NonNull
	@NotNull
	@Column(name = "unitPrice", precision = 18, scale = 2, nullable = false)
	private BigDecimal unitPrice = BigDecimal.ZERO;
	
	@Column(name = "unit", nullable = false)
	private byte unit = ProductUnit.UNITY.getCode();
	
	public ProductUnit getUnit() {
		return ProductUnit.toEnum(this.unit);
	}
	
	public void setUnit(ProductUnit unit) {
		if (unit == null)
			throw new IllegalArgumentException("Product unit cannot be null.");
		this.unit = unit.getCode();
		
	}
	
	@Column(name = "discount", nullable = false, precision = 4, scale = 2)
	private BigDecimal discount = BigDecimal.ZERO;
	
	@Column(name = "validity")
	private LocalDate validity;
	
	@CascadeOnDelete
	@ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
	private List<WishList> wishLists = new ArrayList<>();
	
	public List<WishList> getWishLists() {
		return List.copyOf(this.wishLists);
		
	}
	
	@Column(name = "active")
	private boolean active = true;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@Setter(value = AccessLevel.PROTECTED)
	@Column(name = "dateupdate", insertable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
	@Builder(builderMethodName = "maker", buildMethodName = "done")
	public static Product of(String title, String description,
			BigDecimal discount, BigDecimal unitPrice, ProductUnit unit) {
		
		Objects.requireNonNull(title, "Title can't be null");
		title = title.trim();
		if (title.isBlank())
			throw new IllegalArgumentException("Title can't be blank");
		
		Objects.requireNonNull(description, "Description can't be null");
		description = description.trim();
		if (description.isBlank())
			throw new IllegalArgumentException("Description can't be blank");
		
		Objects.requireNonNull(unitPrice, "Price can't be null");
		if (unitPrice.compareTo(BigDecimal.ZERO) < 0)
			throw new IllegalArgumentException("price can't be negative");
		BigDecimal price = unitPrice.setScale(2, RoundingMode.HALF_EVEN);
		
		BigDecimal finalDiscount = discount == null ? BigDecimal.ZERO : discount;
		if (finalDiscount.compareTo(BigDecimal.ZERO) < 0 || finalDiscount.compareTo(BigDecimal.ONE) > 0)
			throw new IllegalArgumentException("Discount must be between 0(0%) and 1(100%)");
		
		Objects.requireNonNull(unit, "Unit can't be null");
		
		Product product = new Product();
		product.setTitle(title);
		product.setDescription(description);
		product.setDiscount(finalDiscount);
		product.setUnitPrice(price);
		product.setUnit(unit);
		return product;
	}
	
	public static Product of(String title, String description,
			BigDecimal discount, double unitPrice, ProductUnit unit) {
		return of(title, description, discount, BigDecimal.valueOf(unitPrice), unit);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("id", this.getId()).append("title", this.getTitle())
				.append("description", this.getDescription())
				.append("price", this.getPriceInfo())
				.append("unit", this.getUnit().getValue())
				.append("validity", this.getValidity()).build();
	}
	
	public String getPriceInfo() {
		String price = CF.format(this.getPriceWithDiscount());
		if (this.getDiscount().compareTo(BigDecimal.ZERO) > 0)
			return "%s(-%s)".formatted(price, PF.format(this.getDiscount()));
		return price;
	}
	
	public String getProductInfo() {
		return "%s-%s=%s".formatted(this.getId(), this.getTitle(),
				Optional.ofNullable(getPriceInfo()).filter(s -> !s.isBlank())
						.orElse(""));
	}
	
	/**
	 * Gets discounted product price if discount is set.
	 * 
	 * @return BigDecimal value
	 */
	public BigDecimal getPriceWithDiscount() {
		if (this.getDiscount() == null || this.getDiscount().compareTo(BigDecimal.ZERO) == 0)
			return this.getUnitPrice().setScale(2, RoundingMode.HALF_EVEN);
		return this.getUnitPrice()
				.multiply(BigDecimal.ONE.subtract(this.getDiscount()))
				.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	/**
	 * Overload accepting a decimal additional discount (0..1).
	 */
	public BigDecimal getPriceWithDiscount(BigDecimal addDiscount) {
		if (addDiscount == null || addDiscount.compareTo(BigDecimal.ZERO) == 0)
			return this.getPriceWithDiscount();
		return this.getPriceWithDiscount()
				.multiply(BigDecimal.ONE.subtract(addDiscount))
				.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	/**
	 * Define expiration datetime (years, months, days, hours, minutes and
	 * seconds) for product. Considers default zone_id.
	 * 
	 * @param i    amount of time
	 * @param unit ChronoUnit (LIMITED to days/weeks/months/years)
	 * @return object Product with the set expiration date
	 */
	public Product setValidity(long i, TemporalUnit unit) {
		return this.setValidity(i, unit, systemDefault);
	}
	
	public Product setValidity(long i, TemporalUnit unit, ZoneId zoneId) {
		LocalDate date = this.getDateCreate() != null
				? this.getDateCreate().toLocalDate()
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
		return isValid(systemDefault);
	}
	
	public boolean isValid(ZoneId zoneId) {
		LocalDate v = this.getValidity();
		return v != null && v.isAfter(LocalDate.now(zoneId));
	}
	
}
