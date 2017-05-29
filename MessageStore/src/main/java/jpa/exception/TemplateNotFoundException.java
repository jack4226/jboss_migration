package jpa.exception;

public class TemplateNotFoundException extends Exception {
	
	private static final long serialVersionUID = 2494389451029831480L;

	public TemplateNotFoundException() {
		super();
	}
	
	public TemplateNotFoundException(String message) {
		super(message);
	}
	
    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
