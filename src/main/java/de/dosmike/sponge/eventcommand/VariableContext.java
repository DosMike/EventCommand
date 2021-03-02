package de.dosmike.sponge.eventcommand;

import de.dosmike.sponge.eventcommand.exception.NoVariableValueException;
import de.dosmike.sponge.eventcommand.exception.VariableTypeException;

import java.util.HashMap;

public class VariableContext extends HashMap<String,Object> {

	public Object require(String variable) {
		Object t = get(variable);
		if (t == null) throw new NoVariableValueException("The Variable \""+variable+"\" did not contain a value");
		return t;
	}

	// resolve vars into a string using ${} placeholders
	String resolveVariables(String entry) {
	    for (Entry<String, Object> e : this.entrySet()) {
	        String key = "${" + e.getKey() + "}";
	        if (entry.contains(key)) {
	            entry = entry.replace(key, e.getValue().toString());
	        }
	    }
	    return entry;
	}

	String resolveVariablesNumeric(String entry) {
	    for (Entry<String, Object> e : this.entrySet()) {
	        String key = "${" + e.getKey() + "}";
	        if (entry.contains(key)) {
	        	try {
			        entry = entry.replace(key, Utils.toDouble(e.getValue()).toString());
		        } catch (IllegalArgumentException exception) {
	        		throw new VariableTypeException(exception);
		        }
	        }
	    }
	    return entry;
	}
}
