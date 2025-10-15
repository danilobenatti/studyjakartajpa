package studyjakartajpa.model.enums;

import lombok.Getter;

@Getter
public enum ProductUnit {
	
	UNITY((byte) 1, "pc"), GRAM((byte) 2, "g"), KILOGRAM((byte) 3, "kg"),
	CENTIMETER((byte) 4, "cm"), METER((byte) 5, "m");
	
	private Byte code;
	private String value;
	
	ProductUnit(Byte code, String value) {
		this.code = code;
		this.value = value;
	}
	
	public static ProductUnit toEnum(Byte code) {
		if (code == null)
			return null;
		for (ProductUnit unit : ProductUnit.values())
			if (code.equals(unit.getCode()))
				return unit;
		throw new IllegalArgumentException("Enum code invalid: " + code);
	}
	
}
