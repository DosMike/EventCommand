package de.dosmike.sponge.eventcommand.exception;

public class MissingEventSourceException extends ScriptExecutionException {

	public MissingEventSourceException() {
	}

	public MissingEventSourceException(String s) {
		super(s);
	}

	public MissingEventSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingEventSourceException(Throwable cause) {
		super(cause);
	}

}
