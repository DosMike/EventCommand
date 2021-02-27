package de.dosmike.sponge.eventcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Trigger<EventType> {

    protected Class<? extends EventType> eventClass;
    protected Map<String, WithChain> variables = new HashMap<>();
    protected List<Action> actions = new ArrayList<>();

    protected Trigger(Class<? extends EventType> clazz, List<WithChain> variables, List<Action> actions) {
        this.eventClass = clazz;
        register(eventClass);
        for (WithChain with : variables) {
            this.variables.put(with.name.toLowerCase(), with);
        }
        this.actions.addAll(actions);
    }

    protected abstract <T extends EventType> void register(Class<T> eventClass);

    protected void unregister() {
    }

    protected void run(EventType event) {
        int hc = hashCode();
        try {
            EventCommand.l("(%d) Triggered Event %s", hc, event.getClass().getName());
            Map<String, Object> values = new HashMap<>();
            for (Map.Entry<String, WithChain> e : variables.entrySet()) {
                String k = e.getKey().toLowerCase();
                Object v = e.getValue().resolve(event);
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
