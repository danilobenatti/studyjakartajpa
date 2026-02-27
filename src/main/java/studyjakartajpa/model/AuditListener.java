package studyjakartajpa.model;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuditListener {
	
	private static final ZoneId ZONE_ID = ZoneId.systemDefault();
	
	@PrePersist
	public void setCreatedOn(Object obj) {
		switch (obj) {
			case Person p -> {
				p.toUpperCaseGender();
				p.setDateCreate(LocalDateTime.now(ZONE_ID));
			}
			case Product p -> p.setDateCreate(LocalDateTime.now(ZONE_ID));
			case WishList w -> w.setDateCreate(LocalDateTime.now(ZONE_ID));
			case Order o -> {
				o.calcPricesOrder();
				o.setDateCreate(LocalDateTime.now(ZONE_ID));
			}
			case OrderItem oi -> oi.setSubTotal();
			default -> obj.toString();
		}
	}
	
	@PreUpdate
	public void setUpdatedOn(Object obj) {
		switch (obj) {
			case Person p -> {
				p.toUpperCaseGender();
				p.setDateUpdate(LocalDateTime.now(ZONE_ID));
			}
			case Product p -> p.setDateUpdate(LocalDateTime.now(ZONE_ID));
			case WishList w -> w.setDateCreate(LocalDateTime.now(ZONE_ID));
			case Order o -> {
				if (o.isWaitting())
					o.calcPricesOrder();
				o.setDateUpdate(LocalDateTime.now(ZONE_ID));
			}
			case OrderItem oi when oi.getOrder().isWaitting() ->
				oi.setSubTotal();
			default -> obj.toString();
		}
	}
}
