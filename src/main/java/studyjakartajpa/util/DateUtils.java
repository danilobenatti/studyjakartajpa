package studyjakartajpa.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public class DateUtils {
	
	private DateUtils() {
		throw new AssertionError("Do not instantiate this utility class");
	}
	
	public static Locale getLocale() {
		return Locale.getDefault();
	}
	
	public static ZoneId getZoneId() {
		return ZoneId.systemDefault();
	}
	
	public static LocalDate today() {
		return LocalDate.now(getZoneId());
	}
	
	public static LocalDateTime now(ZoneId zoneId) {
		return LocalDateTime.now(zoneId);
	}
	
}
