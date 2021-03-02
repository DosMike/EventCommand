package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.VariableContext;

public interface Action {

	void run(Object context, VariableContext variables);

}
