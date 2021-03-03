package de.dosmike.sponge.eventcommand.statements.luckperms;

import de.dosmike.sponge.eventcommand.*;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;
import de.dosmike.sponge.eventcommand.statements.ActionGroup;
import de.dosmike.sponge.eventcommand.statements.Computing;
import de.dosmike.sponge.eventcommand.statements.Trigger;
import de.dosmike.sponge.eventcommand.statements.WithChain;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;

import java.util.List;

public class LuckPermsTrigger extends Trigger<LuckPermsEvent> {

    EventSubscription<? extends LuckPermsEvent> subscription;

    public LuckPermsTrigger(String classname, List<WithChain> variables, List<Computing> maths, ActionGroup actions) {
        super(Utils.tryLoad(classname, LuckPermsEvent.class)
                        .orElseThrow(() -> new StatementParseException("Could not find event (include package): " + classname))
                , variables, maths, actions);
    }

    @Override
    protected <T extends LuckPermsEvent> void register(Class<T> eventClass) {
        subscription = LuckPermsProvider.get().getEventBus().subscribe(eventClass, this::run);
    }

    @Override
    public void unregister() {
        subscription.close();
    }

}
