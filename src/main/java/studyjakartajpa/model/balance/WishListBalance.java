package studyjakartajpa.model.balance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import jakarta.persistence.Tuple;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WishListBalance {
	
	private String firstname;
	
	private String title;
	
	private BigDecimal total;
	
	private BigDecimal average;
	
	private long count;
	
	public WishListBalance(String firstname, String title, double total,
			double avg, int count) {
		this.firstname = firstname;
		this.title = title;
		this.total = roundValue(total);
		this.average = roundValue(avg);
		this.count = count;
	}
	
	private BigDecimal roundValue(double total) {
		return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public WishListBalance(Tuple tuple) {
		this.firstname = tuple.get("firstname", String.class);
		this.title = tuple.get("title", String.class);
		this.total = tuple.get("total", BigDecimal.class);
		this.average = tuple.get("average", BigDecimal.class);
		this.count = tuple.get("counter", Long.class);
	}
	
	//@formatter:off
	@Override
	public String toString() {
		NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		return new StringBuilder("WishListBalance [")
				.append("firstname=").append(firstname)
				.append(", title=").append(title)
				.append(", total=").append(cf.format(this.total))
				.append(", average=").append(cf.format(this.average))
				.append(", count=").append(this.count)
				.append("]").toString();
	}
	//@formatter:on
	
}
