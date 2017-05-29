package jpa.exception;

/**
 * @author Jack Wang
 */
public class TemplateException extends Exception {
	private static final long serialVersionUID = 5328822016227740458L;

	public TemplateException(String message) {
		super(message);
	}
	
	public TemplateException(String message, Throwable cause) {
		super(message, cause);
	}
}
