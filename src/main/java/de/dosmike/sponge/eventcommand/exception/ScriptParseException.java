package de.dosmike.sponge.eventcommand.exception;

import java.io.IOException;

public class ScriptParseException extends IOException implements ECException {

	int line;
	public ScriptParseException(int line) {
		this.line = line;
	}

	public ScriptParseException(int line, String message) {
		super(message);
		this.line = line;
	}

	public ScriptParseException(int line, String message, Throwable cause) {
		super(message, cause);
		this.line = line;
	}

	public ScriptParseException(int line, Throwable cause) {
		super(cause);
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	@Override
	public String getMessage() {
		String message = super.getMessage();
		if (message == null) return "Unable to parse Line "+line;
		else return message + " (on line "+line+')';
	}
}
