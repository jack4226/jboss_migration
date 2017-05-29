package jpa.service.msgout;

/**
 * define SmtpException
 */
public class SmtpException extends Exception {
	private static final long serialVersionUID = -554228998731399437L;

	/**
	 * create a SmtpException instance
	 */
	public SmtpException() {
		super();
	}

	/**
	 * create a SmtpException instance with message text
	 * 
	 * @param s -
	 *            message text
	 */
	public SmtpException(String s) {
		super(s);
	}
}