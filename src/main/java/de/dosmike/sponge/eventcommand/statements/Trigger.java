package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.*;
import de.dosmike.sponge.eventcommand.exception.MissingDependencyException;
import de.dosmike.sponge.eventcommand.statements.luckperms.LuckPermsTrigger;
import de.dosmike.sponge.eventcommand.statements.sponge.SpongeTrigger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void unregister() {
    }

    protected void run(EventType event) {
        int hc = hashCode();
        try {
            if (EventCommand.isVerboseLogging())
                EventCommand.l("(%d) Triggered Event %s", hc, event.getClass().getName());
            VariableContext values = new VariableContext();
            for (Map.Entry<String, WithChain> e : variables.entrySet()) {
                String k = e.getKey().toLowerCase();
                Object v = e.getValue().resolve(event);
                values.put(k, v);
                if (EventCommand.isVerboseLogging())
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

    public static <E> Trigger<E> create(String classname, List<WithChain> variables, List<Computing> maths, ActionGroup actions) {
        if (DependencyProxy.isLuckPermsClass(classname)) {
            if (!DependencyProxy.isLuckPermsLoaded())
                throw new MissingDependencyException("Detected LuckPerms hooks, but LuckPerms is missing on the Server!");
            return (Trigger<E>) new LuckPermsTrigger(classname, variables, maths, actions);
        } else { //sponge events do not have to be inside the sponge package
            return (Trigger<E>) new SpongeTrigger(classname, variables, maths, actions);
        }
    }

}
