package studyjakartajpa.model.balance;

import static java.math.RoundingMode.HALF_EVEN;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
		this.average = BigDecimal.valueOf(avg).setScale(2, HALF_EVEN);
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
		var sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		sb.append("year", getYear());
		sb.append("month", Month.of(getMonth()).getDisplayName(TextStyle.FULL, Locale.getDefault()).concat("(" + getMonth() + ")"));
		sb.append("average", NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this.average));
		sb.append("count", getCount());
		sb.append("status", OrderStatus.toEnum(getStatus()).getValue());
		return sb.build();
	}
	//@formatter:on
	
}
