package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.VariableContext;

public interface Filter {

	boolean test(VariableContext variables, Filtered ruleSet);

	static Filter negate(Filter f) {
		return (variables, ruleSet) -> !f.test(variables, ruleSet);
	}

}
