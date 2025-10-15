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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tbl_products", catalog = "jpaforbeginners", schema = "public")
public class Product implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(generator = "products_seq_generator",
		strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "products_seq_generator", sequenceName = "products_id_seq",
		initialValue = 150, allocationSize = 50)
	private long id;
	
	@NonNull
	@Column(name = "col_title", length = 150, nullable = false)
	private String title;
	
	@NonNull
	@Column(name = "col_description", length = 255, nullable = false)
	private String description;
	
	@NonNull
	@Column(name = "col_unitPrice", precision = 11, scale = 2, nullable = false)
	private BigDecimal unitPrice = BigDecimal.ZERO;
	
	@Column(name = "col_unit", nullable = false)
	private byte unit = ProductUnit.UNITY.getCode();
	
	public ProductUnit getUnit() {
		return ProductUnit.toEnum(this.unit);
	}
	
	public void setUnit(ProductUnit unit) {
		this.unit = unit.getCode();
		
	}
	
	@Column(name = "col_discount", nullable = false,
		columnDefinition = "NUMERIC(3,2)")
	private float discount = 0;
	
	@Column(name = "col_validity")
	private LocalDate validity;
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateinsert", updatable = false,
		columnDefinition = "timestamp with time zone")
	private LocalDateTime dateCreate;
	
	@jakarta.persistence.PrePersist
	protected void prePersist() {
		this.dateCreate = LocalDateTime.now();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "col_dateupdate", insertable = false,
		columnDefinition = "timestamp with time zone")
	private LocalDateTime dateUpdate;
	
	@jakarta.persistence.PreUpdate
	protected void preUpdate() {
		this.dateUpdate = LocalDateTime.now();
	}
	
	@Builder(setterPrefix = "with")
	public static Product of(String title, String description, float discount,
			double unitPrice, ProductUnit unit) {
		Product product = new Product();
		product.setTitle(title);
		product.setDescription(description);
		product.setDiscount(discount);
		product.setUnitPrice(BigDecimal.valueOf(unitPrice).setScale(2,
				RoundingMode.HALF_EVEN));
		product.setUnit(unit);
		return product;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("title", getTitle())
				.append("description", getDescription())
				.append("end_price", formatedPrice())
				.append("unit", getUnit().getValue())
				.append("validity", getValidity()).build();
	}
	
	public String formatedPrice() {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		return nf.format(getPriceWithDiscount().doubleValue());
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
	 * Gets the product price with an additional discount applied to
	 * getPriceWithDiscount.
	 * 
	 * @param discount
	 * @return BigDecimal value
	 */
	public BigDecimal getPriceWithDiscount(float discount) {
		return getPriceWithDiscount()
				.multiply(BigDecimal.valueOf(1 - discount));
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
