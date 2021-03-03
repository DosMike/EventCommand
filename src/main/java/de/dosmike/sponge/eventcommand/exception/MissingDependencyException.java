package de.dosmike.sponge.eventcommand.exception;

public class MissingDependencyException extends RuntimeException implements ECException {

	public MissingDependencyException() {
	}

	public MissingDependencyException(String s) {
		super(s);
	}

	public MissingDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingDependencyException(Throwable cause) {
		super(cause);
	}
}
