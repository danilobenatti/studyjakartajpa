package studyjakartajpa.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public final class AuditListener {
	
	protected static LocalDateTime now() { return LocalDateTime.now(ZoneId.systemDefault()); }
	
	@PrePersist
	public void setCreatedOn(Object obj) {
		switch (obj) {
			case Person p -> {
				p.toUpperCaseGender();
				p.setDateCreate(now());
			}
			case Product p -> p.setDateCreate(now());
			case WishList w -> w.setDateCreate(now());
			case Order o -> {
				o.calcPricesOrder();
				o.setDateCreate(now());
			}
			case OrderItem oi when oi.getOrder() != null -> oi.setSubTotal();
			default -> { // no-op
			}
		}
	}
	
	@PreUpdate
	public void setUpdatedOn(Object obj) {
		switch (obj) {
			case Person p -> {
				p.toUpperCaseGender();
				p.setDateUpdate(now());
			}
			case Product p -> p.setDateUpdate(now());
			case WishList w -> w.setDateUpdate(now());
			case Order o -> {
				if (o.isWaiting())
					o.calcPricesOrder();
				o.setDateUpdate(now());
			}
			case OrderItem oi when oi.getOrder() != null && oi.getOrder().isWaiting() -> oi.setSubTotal();
			default -> { // no-op
			}
		}
	}
}
