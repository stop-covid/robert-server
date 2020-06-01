package fr.gouv.stopc.robert.cockpit.exception;

public class UnknownKpiFormatException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -129263496315576456L;

	public UnknownKpiFormatException() {
		super();
	}

	public UnknownKpiFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnknownKpiFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownKpiFormatException(String message) {
		super(message);
	}

	public UnknownKpiFormatException(Throwable cause) {
		super(cause);
	}

}
