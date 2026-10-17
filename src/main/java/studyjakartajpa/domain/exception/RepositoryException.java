package studyjakartajpa.domain.exception;

public class RepositoryException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor that accepts only an explanatory error message.
	 * 
	 * @param message
	 */
	public RepositoryException(String message) {
		super(message);
	}
	
	/**
	 * A constructor that accepts the original cause of the error (exception
	 * chaining). Essential for preserving the original stack trace of exceptions
	 * such as NoSuchMethodException.
	 * 
	 * @param message Details of the error that occurred.
	 * @param cause   The original exception caught.
	 */
	public RepositoryException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
