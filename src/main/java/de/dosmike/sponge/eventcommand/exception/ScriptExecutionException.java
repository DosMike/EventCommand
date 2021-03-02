package de.dosmike.sponge.eventcommand.exception;

public class ScriptExecutionException extends RuntimeException implements ECException {

	public ScriptExecutionException() {
	}

	public ScriptExecutionException(String s) {
		super(s);
	}

	public ScriptExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptExecutionException(Throwable cause) {
		super(cause);
	}
}
