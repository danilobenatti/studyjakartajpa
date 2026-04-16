package studyjakartajpa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import studyjakartajpa.model.Person;

/**
 * Class for calculating the Body Mass Index (BMI). Based on the individual's
 * weight, height and gender.
 * 
 * @author danil
 *
 */
public class Imc {
	
	private Imc() {
	}
	
	private static final Locale LOCALE = Locale.getDefault();
	
	private static final double[] FEMALE_THRESHOLDS = { 19.1, 25.8, 27.3, 32.3 };
	private static final double[] MALE_THRESHOLDS = { 20.7, 26.4, 27.8, 31.1 };
	
	private static final String[] LABELS = { "under weight", "at ideal weight",
			"a little overweight", "over ideal weight", "is obese" };
	
	private static final String UNKNOWN = "unknown";
	
	/**
	 * Method that calculates the <b>body mass index</b> (BMI).
	 * 
	 * @param weight Person's weight in kilograms.
	 * @param height Height of the person in meters.
	 * @return value Double BMI value.
	 */
	public static double calcImc(float weight, float height) {
		BigDecimal wt = new BigDecimal(Float.toString(weight));
		BigDecimal ht = new BigDecimal(Float.toString(height));
		
		double vl = 0.0;
		
		if (weight > 0 && height > 0)
			vl = wt.divide(ht.pow(2), 1, RoundingMode.HALF_EVEN).doubleValue();
		
		return vl;
	}
	
	/**
	 * Method that calculates the <b>body mass index</b> (BMI) by the
	 * individual's gender.
	 * 
	 * @param weight Person's weight in kilograms.
	 * @param height Height of the person in meters.
	 * @param gender Gender of the person, male or female only.
	 * @return info BMI calculation information.
	 * @throws info NullPointException
	 * @since 1.0
	 */
	public static String imcByGender(float weight, float height, char gender) {
		return switch (Character.toLowerCase(gender)) {
			case 'f' -> femaleImc(calcImc(weight, height));
			case 'm' -> maleImc(calcImc(weight, height));
			default -> UNKNOWN;
		};
	}
	
	public static String imcByGender(Person p) {
		if (p == null)
			return UNKNOWN;
		return imcByGender(p.getWeight(), p.getHeight(), p.getGender());
	}
	
	/**
	 * Method that returns information about the BMI calculation for females.
	 * 
	 * @param value Calculated BMI.
	 * @return info BMI calculation information for females.
	 */
	public static String femaleImc(double value) {
		return classify(value, FEMALE_THRESHOLDS, LABELS);
	}
	
	/**
	 * Method that returns information about the BMI calculation for males.
	 * 
	 * @param value Calculated BMI.
	 * @return info BMI calculation information for males.
	 */
	public static String maleImc(double value) {
		return classify(value, MALE_THRESHOLDS, LABELS);
	}
	
	//@formatter:off
	private static String classify(double value, double[] thresholds, String[] labels) {
		
		if (value <= 0 || Double.isNaN(value))
			return UNKNOWN;
		
		for (int i = 0; i < thresholds.length; i++)
			if (value < thresholds[i])
				return String.format(LOCALE, "[%.1f] %s", value, labels[i]);
		
		return String.format(LOCALE, "[%.1f] %s", value, labels[labels.length - 1]);
	}
	//@formatter:on
	
}
