package de.dosmike.sponge.eventcommand;

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
            for (int i = 3; i < tokens.length; i++) {
                mappers.add(Mapper.fromString(tokens[i]));
            }
        } else {
            throw new IllegalArgumentException("'with'-chain definition is invalid!");
        }
    }

    String resolve(Object input) {
        Optional<?> value = Optional.of(input);
        for (Mapper m : mappers) value = m.map(value);
        return value.map(Utils::toString).orElseThrow(()->new RuntimeException("Could not resolve 'with'-chain"));
    }

}
