package de.dosmike.sponge.eventcommand.exception;

public class VariableTypeException extends ScriptExecutionException {

	public VariableTypeException() {
	}

	public VariableTypeException(String s) {
		super(s);
	}

	public VariableTypeException(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableTypeException(Throwable cause) {
		super(cause);
	}
}
