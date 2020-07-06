package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trigger {

    Class<? extends Event> eventClass;
    Map<String, WithChain> variables = new HashMap<>();
    List<Action> actions = new ArrayList<>();

    public Trigger(String classname, List<WithChain> variables, List<Action> actions) {
        this.eventClass = Utils.tryLoad(classname, Event.class).orElseThrow(()-> new RuntimeException("Could not find event (include package): "+classname) );
        register(eventClass);
        for (WithChain with : variables) {
            this.variables.put(with.name.toLowerCase(), with);
        }
        this.actions.addAll(actions);
    }
    private <T extends Event> void register(Class<T> eventClass) {
        Sponge.getEventManager().registerListener(EventCommand.getInstance(), eventClass, this::run);
    }
    private void run(Event event) {
        int hc = hashCode();
        try {
            EventCommand.l("(%d) Triggered Event %s", hc, event.getClass().getName());
            Map<String, String> values = new HashMap<>();
            for (Map.Entry<String, WithChain> e : variables.entrySet()) {
                String k = e.getKey().toLowerCase(), v = e.getValue().resolve(event);
                values.put(k, v);
                EventCommand.l("(%d) Resolved %s to %s", hc, k, v);
            }
            for (Action a : actions) {
                EventCommand.l("(%d) Executing %s", hc, a.command);
                a.run(event, values);
            }
        } catch (Exception e) {
            EventCommand.w("(%d) Error during event trigger:", hc);
            EventCommand.simplePrint(e);
        }
    }

}
