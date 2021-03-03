package de.dosmike.sponge.eventcommand.exception;

public class NoVariableValueException extends ScriptExecutionException {

	public NoVariableValueException() {
	}

	public NoVariableValueException(String s) {
		super(s);
	}

	public NoVariableValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoVariableValueException(Throwable cause) {
		super(cause);
	}

}
