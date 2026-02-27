package studyjakartajpa.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import studyjakartajpa.model.enums.JobFunctions;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder(toBuilder = true, builderMethodName = "maker",
	buildMethodName = "done")
@DiscriminatorValue(value = "E")
public class Employee extends Person {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "register", length = 255, nullable = false)
	private String register;
	
	@Column(name = "jobfunction", nullable = false)
	private Byte jobFunction;
	
	public void setJobFunction(JobFunctions jobFunction) {
		this.jobFunction = jobFunction.getCode();
		
	}
	
	public JobFunctions getJobFunction() {
		return JobFunctions.toEnum(this.jobFunction);
	}
	
	@Builder.Default
	@Column(name = "salary", precision = 18, scale = 2)
	private BigDecimal salary = BigDecimal.ZERO;
	
	public void setSalary(BigDecimal salary) {
		this.salary = salary.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void setSalary(String value) {
		this.salary = new BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN);
	}
	
	public void setSalary(double value) {
		setSalary(String.valueOf(value));
	}
	
	@Column(name = "hiringDate", nullable = false)
	private LocalDate hiringDate;
	
	public Employee(String firstname, Character gender, LocalDate birthdate,
			String register, JobFunctions jobFunction, double salary,
			LocalDate hiringDate) {
		super(firstname, gender, birthdate);
		this.register = register;
		this.jobFunction = jobFunction.getCode();
		this.salary = new BigDecimal(Double.toString(salary));
		this.hiringDate = hiringDate;
	}
	
	@Override
	public String toString() {
		var sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		sb.appendSuper(super.toString());
		sb.append("register", this.getRegister());
		sb.append("jobFunction", this.getJobFunction().getDescription());
		sb.append("salary", this.getSalaryFormatted());
		sb.append("hiringDate", dtf.format(this.getHiringDate()));
		return sb.toString();
	}
	
	public String getSalaryFormatted() {
		return this.getSalaryFormatted(locale);
	}
	
	public String getSalaryFormatted(Locale locale) {
		NumberFormat cf = NumberFormat.getCurrencyInstance(locale);
		cf.setMaximumFractionDigits(2);
		return cf.format(this.getSalary());
	}
	
}
