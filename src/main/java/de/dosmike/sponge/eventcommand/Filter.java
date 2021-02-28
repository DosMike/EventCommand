package de.dosmike.sponge.eventcommand;

import java.util.Map;

public interface Filter {

	boolean test(Map<String,Object> variables, Filtered ruleSet);

}
