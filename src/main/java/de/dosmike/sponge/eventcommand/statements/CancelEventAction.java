package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.statements.luckperms.CancelLuckPermsEventAction;
import de.dosmike.sponge.eventcommand.statements.sponge.CancelSpongeEventAction;

public abstract class CancelEventAction implements Action {

    public static CancelEventAction create(String forTrigger) {
        if (DependencyProxy.isLuckPermsClass(forTrigger)) {
            return new CancelLuckPermsEventAction();
        } else {
            return new CancelSpongeEventAction();
        }
    }

}
