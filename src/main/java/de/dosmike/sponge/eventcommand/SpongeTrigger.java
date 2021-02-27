package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

import java.util.List;

public class SpongeTrigger extends Trigger<Event> {

    public SpongeTrigger(String classname, List<WithChain> variables, List<Action> actions) {
        super(Utils.tryLoad(classname, Event.class)
                        .orElseThrow(() -> new RuntimeException("Could not find event (include package): " + classname))
                , variables, actions);
    }

    @Override
    protected <T extends Event> void register(Class<T> eventClass) {
        Sponge.getEventManager().registerListener(EventCommand.getInstance(), eventClass, this::run);
    }

}
