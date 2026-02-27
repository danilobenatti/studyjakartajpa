package studyjakartajpa.model.enums;

import lombok.Getter;

@Getter
public enum JobFunctions {
	ENG_JR((byte) 1, "Junior Engineer"), ENG_PL((byte) 2, "Full Engineer"),
	ENG_SR((byte) 3, "Senior Engineer");
	
	private Byte code;
	private String description;
	
	JobFunctions(Byte code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public static JobFunctions toEnum(Byte code) {
		if (code == null)
			return null;
		for (JobFunctions jobFunction : JobFunctions.values())
			if (code.equals(jobFunction.getCode()))
				return jobFunction;
		throw new IllegalArgumentException("Invalid code: " + code);
	}
	
}
