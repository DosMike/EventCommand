package de.dosmike.sponge.eventcommand.statements.sponge;

import de.dosmike.sponge.eventcommand.*;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;
import de.dosmike.sponge.eventcommand.statements.ActionGroup;
import de.dosmike.sponge.eventcommand.statements.Computing;
import de.dosmike.sponge.eventcommand.statements.Trigger;
import de.dosmike.sponge.eventcommand.statements.WithChain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

import java.util.List;

public class SpongeTrigger extends Trigger<Event> {

    public SpongeTrigger(String classname, List<WithChain> variables, List<Computing> maths, ActionGroup actions) {
        super(Utils.tryLoad(classname, Event.class)
                        .orElseThrow(() -> new StatementParseException("Could not find event (include package): " + classname))
                , variables, maths, actions);
    }

    @Override
    protected <T extends Event> void register(Class<T> eventClass) {
        Sponge.getEventManager().registerListener(EventCommand.getInstance(), eventClass, this::run);
    }

}
