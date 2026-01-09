package studyjakartajpa.model.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyjakartajpa.model.enums.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSale {
	
	private int year;
	
	private int month;
	
	private BigDecimal average;
	
	private long count;
	
	private byte status;
	
	public OrderSale(int year, int month, double avg, long count, byte status) {
		this.year = year;
		this.month = month;
		this.average = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_EVEN);
		this.count = count;
		this.status = status;
	}
	
	public OrderSale(Tuple tuple) {
		this.year = tuple.get("year", Integer.class);
		this.month = tuple.get("month", Integer.class);
		this.average = tuple.get("average", BigDecimal.class);
		this.count = tuple.get("counter", Long.class);
		this.status = tuple.get("status", Byte.class);
	}
	
	@Builder(setterPrefix = "with")
	public static OrderSale of(int year, int month, BigDecimal average,
			long count, byte status) {
		return new OrderSale(year, month, average, count, status);
	}
	
	//@formatter:off
	@Override
	public String toString() {
		return new StringBuilder("OrderSales [").append("year=").append(this.year)
				.append(", month=").append(this.month).append("(")
				.append(Month.of(this.month).getDisplayName(TextStyle.FULL, Locale.getDefault()))
				.append(")").append(", average=")
				.append(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this.average))
				.append(", count=").append(this.count)
				.append(", status=").append(OrderStatus.toEnum(this.status).getValue())
				.append("]").toString();
	}
	//@formatter:on
	
}
