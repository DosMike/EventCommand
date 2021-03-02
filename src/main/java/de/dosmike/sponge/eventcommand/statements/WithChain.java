package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.Mapper;
import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class WithChain {

    List<Mapper> mappers = new ArrayList<>();
    String name;
	private static final Predicate<String> onlyLetters = Pattern.compile("^\\p{L}+$").asPredicate();

    public WithChain(String lineDef) {
        String[] tokens = lineDef.split(" ");
	    if (tokens.length >= 4 && tokens[0].equalsIgnoreCase("with") && tokens[2].equalsIgnoreCase("as")) {
		    name = tokens[1].toLowerCase();
		    if (!onlyLetters.test(name)) throw new StatementParseException("Variables names may only contain letters");
		    for (int i = 3; i < tokens.length; i++) {
			    mappers.add(Mapper.fromString(tokens[i]));
		    }
	    } else {
		    throw new StatementParseException("'with'-chain definition is invalid!");
	    }
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
			return v.toString();
		}).orElseThrow(() -> new ScriptExecutionException("Could not resolve 'with'-chain"));
	}

}
