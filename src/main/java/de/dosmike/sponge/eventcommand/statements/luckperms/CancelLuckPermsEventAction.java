package de.dosmike.sponge.eventcommand.statements.luckperms;

import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.VariableContext;
import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import de.dosmike.sponge.eventcommand.statements.CancelEventAction;
import net.luckperms.api.event.type.Cancellable;

public class CancelLuckPermsEventAction extends CancelEventAction {
	@Override
	public void run(Object context, VariableContext variables) {
		if (context instanceof Cancellable) {
			if (EventCommand.isVerboseLogging())
				EventCommand.l("  Cancelling Event!");
			((Cancellable) context).setCancelled(true);
		} else {
			throw new ScriptExecutionException("The event of type \""+context.getClass().getName()+"\" cannot be cancelled!");
		}
	}
}
