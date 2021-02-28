package de.dosmike.sponge.eventcommand;

import java.util.*;

public abstract class Trigger<EventType> {

    protected Class<? extends EventType> eventClass;
    protected Map<String, WithChain> variables = new HashMap<>();
    protected List<Computing> maths;
    protected ActionGroup actions;

    protected Trigger(Class<? extends EventType> clazz, List<WithChain> variables, List<Computing> maths, ActionGroup actions) {
        this.eventClass = clazz;
        register(eventClass);
        for (WithChain with : variables) {
            this.variables.put(with.name.toLowerCase(), with);
        }
        this.maths = maths;
        this.actions = actions;
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
            for (Computing m : maths) {
                m.mutateState(values);
            }
            actions.run(event, values);
        } catch (Exception e) {
            EventCommand.w("(%d) Error during event trigger:", hc);
            EventCommand.simplePrint(e);
        }
    }

}
