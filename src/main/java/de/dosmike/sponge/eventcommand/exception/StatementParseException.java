package de.dosmike.sponge.eventcommand.exception;

public class StatementParseException extends IllegalArgumentException implements ECException {

	public StatementParseException() {
	}

	public StatementParseException(String s) {
		super(s);
	}

	public StatementParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public StatementParseException(Throwable cause) {
		super(cause);
	}
}
