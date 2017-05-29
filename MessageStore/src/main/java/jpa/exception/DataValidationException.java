package jpa.exception;

public class DataValidationException extends IllegalArgumentException {
	
	private static final long serialVersionUID = -1180425695282794441L;

	public DataValidationException() {
		super();
	}
	
	public DataValidationException(String message) {
		super(message);
	}
	
    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
