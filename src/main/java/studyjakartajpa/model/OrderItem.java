package studyjakartajpa.model;

import static java.text.NumberFormat.getNumberInstance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@EntityListeners(value = { AuditListener.class })
@Table(name = "orderitems", catalog = "jpaforbeginners", schema = "public")
public class OrderItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static final NumberFormat NF = getNumberInstance(Locale.getDefault());
	
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
	
	public void setSubTotal() {
		this.subtotal = calcSubTotal();
	}
	
	public BigDecimal calcSubTotal() {
		return calcSubTotal(this.getProduct(),
				new BigDecimal(Double.toString(this.getQuantity())));
	}
	
	public BigDecimal calcSubTotal(Product product, BigDecimal quantity) {
		BigDecimal price = product.getPriceWithDiscount();
		if (price.signum() > 0 && quantity.signum() > 0)
			return price.multiply(quantity).setScale(2, RoundingMode.HALF_EVEN);
		return BigDecimal.ZERO;
	}
	
	@Builder(setterPrefix = "with")
	public static OrderItem of(Order order, Product product, double quantity) {
		OrderItem item = new OrderItem();
		item.setOrder(order);
		item.setProduct(product);
		item.setQuantity(quantity);
		return item;
	}
	
	public String getOrderItemInfo() {
		var sb = new StringBuilder(this.getProduct().getProductInfo());
		
		if (this.getQuantity() > 0 && this.getProduct().getUnit() != null)
			sb.append(" ").append(NF.format(this.getQuantity()))
					.append(this.getProduct().getUnit().getValue());
		
		return sb.toString();
	}
	
}
