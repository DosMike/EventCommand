package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.Mapper;
import de.dosmike.sponge.eventcommand.Patterns;
import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WithChain {

    List<Mapper> mappers = new ArrayList<>();
    String name;

	public WithChain(String lineDef) {
        String[] tokens = lineDef.split(" ");
	    if (tokens.length >= 4 && tokens[0].equalsIgnoreCase("with") && tokens[2].equalsIgnoreCase("as")) {
		    name = tokens[1].toLowerCase();
		    if (!Patterns.allVariableCheck.test(name)) throw new StatementParseException("Variables names may only contain letters");
		    for (int i = 3; i < tokens.length; i++) {
			    mappers.add(Mapper.fromString(tokens[i]));
		    }
	    } else {
		    throw new StatementParseException("'with'-chain definition is invalid!");
	    }
    }

    public boolean expectVariablesInput() {
	    return (mappers.size() > 0 && (mappers.get(0) instanceof Mapper.Variable));
    }

	/**
	 * @return String or primitive that can be converted to string by java
	 */
	Object resolve(Object input) {
		Optional<?> value = Optional.of(input);
		for (Mapper m : mappers) value = m.map(value);
		return value.map(v -> {
			if (v instanceof Number)
				return v;
			else if (v instanceof Boolean)
				return ((Boolean)v)?1:0;
			return v.toString();
		}).orElseThrow(() -> new ScriptExecutionException("Could not resolve 'with'-chain"));
	}

}
