package studyjakartajpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orderitems", catalog = "jpaforbeginners", schema = "public")
public class OrderItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private OrderItemPk id;
	
	@MapsId("orderId")
	@ManyToOne(optional = false, fetch = FetchType.EAGER,
		cascade = CascadeType.ALL)
	@JoinColumn(name = "order_id", nullable = false,
		referencedColumnName = "id")
	private Order order;
	
	@MapsId("productId")
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id", nullable = false,
		referencedColumnName = "id")
	private Product product;
	
	@Column(name = "quantity", nullable = false)
	private double quantity;
	
	@Setter(AccessLevel.NONE)
	@Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
	private BigDecimal subtotal;
	
	public BigDecimal getSubtotal() {
		return this.subtotal.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void setSubTotal() {
		this.subtotal = calcSubTotal();
	}
	
	public BigDecimal calcSubTotal() {
		return calcSubTotal(this.product, this.quantity);
	}
	
	public BigDecimal calcSubTotal(Product product, double quantity) {
		BigDecimal priceWithDiscount = product.getPriceWithDiscount();
		if (priceWithDiscount.signum() > 0 && quantity > 0) {
			return priceWithDiscount.multiply(BigDecimal.valueOf(quantity))
					.setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}
	
	@PrePersist
	private void whenPersist() {
		setSubTotal();
	}
	
	@PreUpdate
	private void whenUpdate() {
		if (getOrder().isWaitting())
			setSubTotal();
	}
	
	@Builder(setterPrefix = "with")
	public static OrderItem of(Order order, Product product, double quantity) {
		OrderItem item = new OrderItem();
		item.setOrder(order);
		item.setProduct(product);
		item.setQuantity(quantity);
		return item;
	}
	
	//@formatter:off
	public String getItemWithPriceFormatted() {
		NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		NumberFormat pf = NumberFormat.getPercentInstance(Locale.getDefault());
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
		StringBuilder builder = new StringBuilder(cf.format(getSubtotal()));
		if (getProduct().getDiscount() > 0)
			builder.append("(").append(pf.format(getProduct().getDiscount())).append(")");
		builder.append(StringUtils.SPACE);
		builder.append(nf.format(getQuantity()));
		builder.append(getProduct().getUnit().getValue());
		return builder.toString();
	}
	//@formatter:on
	
}
