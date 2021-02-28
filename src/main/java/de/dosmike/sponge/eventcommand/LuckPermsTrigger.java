package de.dosmike.sponge.eventcommand;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;

import java.util.List;

public class LuckPermsTrigger extends Trigger<LuckPermsEvent> {

    EventSubscription<? extends LuckPermsEvent> subscription;

    public LuckPermsTrigger(String classname, List<WithChain> variables, ActionGroup actions) {
        super(Utils.tryLoad(classname, LuckPermsEvent.class)
                        .orElseThrow(() -> new RuntimeException("Could not find event (include package): " + classname))
                , variables, actions);
    }

    @Override
    protected <T extends LuckPermsEvent> void register(Class<T> eventClass) {
        subscription = LuckPermsProvider.get().getEventBus().subscribe(eventClass, this::run);
    }

    @Override
    protected void unregister() {
        subscription.close();
    }

}
