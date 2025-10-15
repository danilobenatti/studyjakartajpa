package studyjakartajpa.util;

import java.math.RoundingMode;
import java.text.NumberFormat;
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
	
	static NumberFormat nf = NumberFormat.getInstance(Locale.ROOT);
	
	private Imc() {
	}
	
	/**
	 * Method that calculates the <b>body mass index</b> (BMI).
	 * 
	 * @param weight Person's weight in kilograms.
	 * @param height Height of the person in meters.
	 * @return value Double BMI value.
	 */
	public static double calcImc(float weight, float height) {
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		nf.setRoundingMode(RoundingMode.HALF_EVEN);
		double value = 0;
		if (weight > 0 && height > 0)
			value = weight / Math.pow(height, 2);
		return Float.valueOf(nf.format(value));
	}
	
	/**
	 * Method that calculates the <b>body mass index</b> (BMI) by the
	 * individual's gender.
	 * 
	 * @param weight Person's weight in kilograms.
	 * @param height Height of the person in meters.
	 * @param sex    Gender of the person, male or female only.
	 * @return info BMI calculation information.
	 * @throws info NullPointException
	 * @since 1.0
	 */
	public static String imcByGender(float weight, float height, char sex) {
		return switch (sex) {
			case 'f', 'F' -> femaleImc(calcImc(weight, height));
			case 'm', 'M' -> maleImc(calcImc(weight, height));
			default -> null;
		};
	}
	
	public static String imcByGender(Person p) {
		return switch (p.getGender()) {
			case 'f', 'F' -> femaleImc(calcImc(p.getWeight(), p.getHeight()));
			case 'm', 'M' -> maleImc(calcImc(p.getWeight(), p.getHeight()));
			default -> null;
		};
	}
	
	/**
	 * Method that returns information about the BMI calculation for females.
	 * 
	 * @param value Calculated BMI.
	 * @return info BMI calculation information for females.
	 */
	public static String femaleImc(double value) {
		String info = null;
		if (value <= 0 || Double.isNaN(value)) {
			info = "unknown";
		} else if (value > 0 && value < 19.1) {
			info = String.format("[%.1f] under weight", value);
		} else if (value > 19.2 && value < 25.8) {
			info = String.format("[%.1f] at ideal weight", value);
		} else if (value > 25.9 && value < 27.3) {
			info = String.format("[%.1f] a little overweight", value);
		} else if (value > 27.4 && value < 32.3) {
			info = String.format("[%.1f] over ideal weight", value);
		} else {
			info = String.format("[%.1f] is obese", value);
		}
		return info;
	}
	
	/**
	 * Method that returns information about the BMI calculation for males.
	 * 
	 * @param value Calculated BMI.
	 * @return info BMI calculation information for males.
	 */
	public static String maleImc(double value) {
		String info = null;
		if (value <= 0 || Double.isNaN(value)) {
			info = "unknown";
		} else if (value > 0 && value < 20.7) {
			info = String.format("[%.1f] under weight", value);
		} else if (value > 20.8 && value < 26.4) {
			info = String.format("[%.1f] at ideal weight", value);
		} else if (value > 26.5 && value < 27.8) {
			info = String.format("[%.1f] a little overweight", value);
		} else if (value > 27.9 && value < 31.1) {
			info = String.format("[%.1f] over ideal weight", value);
		} else {
			info = String.format("[%.1f] is obese", value);
		}
		return info;
	}
	
}
