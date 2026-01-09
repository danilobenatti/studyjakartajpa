package studyjakartajpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyjakartajpa.model.balance.OrderSale;
import studyjakartajpa.model.enums.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "orders", catalog = "jpaforbeginners", schema = "public")
@NamedNativeQuery(name = "Orders.AvgMonthly2", query = """
	select
		extract(year from o.billingdate) as year,
		extract(month from o.billingdate) as month,
		round(avg(o.total), 2) as average,
		count(o) as count,
		o.status as status
	from orders as o
	where extract(year from o.billingdate) = ?1 and o.status in (?2)
	group by year, month, status
	order by month asc
	""", resultSetMapping = "OrderSaleMapping2")
@SqlResultSetMapping(name = "OrderSaleMapping2",
	classes = @ConstructorResult(targetClass = OrderSale.class,
		columns = { @ColumnResult(name = "year", type = Integer.class),
				@ColumnResult(name = "month", type = Integer.class),
				@ColumnResult(name = "average", type = BigDecimal.class),
				@ColumnResult(name = "count", type = Long.class),
				@ColumnResult(name = "status", type = Byte.class) }))
public class Order implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@EqualsAndHashCode.Include
	@SequenceGenerator(catalog = "jpaforbeginners", schema = "public",
		name = "orders_seq_generator", sequenceName = "orders_id_seq",
		initialValue = 100, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.SEQUENCE,
		generator = "orders_seq_generator")
	private long id;
	
	@Column(name = "billingdate", nullable = false)
	private LocalDate billingDate;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
		orphanRemoval = true)
	private List<OrderItem> orderItems = new ArrayList<>();
	
	public void setOrderItem(OrderItem item) {
		if (item != null) {
			this.orderItems.add(item);
			item.setOrder(this);
		}
	}
	
	public void setOrderItems(List<OrderItem> items) {
		items.forEach(this::setOrderItem);
	}
	
	public void setOrderItems(OrderItem... items) {
		Arrays.asList(items).forEach(this::setOrderItem);
	}
	
	@Column(name = "discount", nullable = false,
		columnDefinition = "NUMERIC(4,2)")
	private float discount = 0;
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "total", nullable = false, precision = 18, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;
	
	public double getTotal() {
		return calcTotal().doubleValue();
	}
	
	public void setTotal() {
		this.total = calcTotal(this.orderItems, this.discount);
	}
	
	public BigDecimal calcTotal() {
		return calcTotal(this.orderItems, this.discount);
	}
	
	public BigDecimal calcTotal(List<OrderItem> orderitems, float discount) {
		if (isNotEmptyOrNull(orderitems)) {
			return orderitems.stream().map(OrderItem::calcSubTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add)
					.multiply(BigDecimal.valueOf(1 - discount))
					.setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}
	
	static boolean isNotEmptyOrNull(Collection<?> collection) {
		return (collection != null) && !collection.isEmpty();
	}
	
	@Column(name = "status", nullable = false)
	private Byte status = OrderStatus.WAITING.getCode();
	
	public OrderStatus getStatus() {
		return OrderStatus.toEnum(this.status);
	}
	
	public void setStatus(OrderStatus newStatus) {
		if (newStatus != null && !this.status.equals(newStatus.getCode())) {
			this.status = newStatus.getCode();
			if (newStatus.equals(OrderStatus.PAID)) {
				getOrderItems().forEach(OrderItem::setSubTotal);
				this.setTotal();
			}
		}
	}
	
	public boolean isPaid() {
		return getStatus().equals(OrderStatus.PAID);
	}
	
	public boolean isWaitting() {
		return getStatus().equals(OrderStatus.WAITING);
	}
	
	public boolean isCanceled() {
		return getStatus().equals(OrderStatus.CANCELED);
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateinsert", updatable = false,
		columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateCreate;
	
	@PrePersist
	protected void whenPersist() {
		this.dateCreate = LocalDateTime.now();
		calcPricesOrder();
	}
	
	@Setter(value = AccessLevel.NONE)
	@Column(name = "dateupdate", columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private LocalDateTime dateUpdate;
	
	@PreUpdate
	protected void whenUpdate() {
		this.dateUpdate = LocalDateTime.now();
		if (isWaitting())
			calcPricesOrder();
	}
	
	private void calcPricesOrder() {
		getOrderItems().forEach(OrderItem::setSubTotal);
		this.setTotal();
	}
	
	@Builder(setterPrefix = "with")
	public static Order of(LocalDate billingDate, Person person, float discount,
			OrderStatus status) {
		Order order = new Order();
		order.setBillingDate(billingDate);
		order.setPerson(person);
		order.setDiscount(discount);
		order.setStatus(status);
		return order;
	}
	
	//@formatter:off
	@Override
	public String toString() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
		NumberFormat pf = NumberFormat.getPercentInstance(Locale.getDefault());
		NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		
		Map<String, String> products = getOrderItems().stream()
				.collect(Collectors.toMap(i -> i.getProduct().getTitle(),
						OrderItem::getItemWithPriceFormatted));
		
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("person", getPerson().getFirstname())
				.append("billingDate", dtf.format(getBillingDate()))
				.append("discount", pf.format(getDiscount()))
				.append("total", cf.format(getTotal()))
				.append("status", getStatus().getValue())
				.append("products", products).build();
	}
	//@formatter:on
	
}
