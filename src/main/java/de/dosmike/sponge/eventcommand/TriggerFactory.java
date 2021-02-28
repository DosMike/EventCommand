package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;

import java.util.List;

public class TriggerFactory {

	private static TriggerFactory instance;

	public static TriggerFactory get() {
		if (instance == null) instance = new TriggerFactory();
		return instance;
	}

	private boolean isLuckPermsLoaded;

	public boolean isLuckPermsLoaded() {
		return isLuckPermsLoaded;
	}

	private TriggerFactory() {
		isLuckPermsLoaded = Sponge.getPluginManager().getPlugin("luckperms").isPresent();
	}

	public <E> Trigger<E> create(String classname, List<WithChain> variables, List<Computing> maths, ActionGroup actions) {
		if (classname.startsWith("net.luckperms.api.event.")) {
			if (!isLuckPermsLoaded)
				throw new IllegalStateException("Detected LuckPerms hooks, but LuckPerms is missing on the Server!");
			return (Trigger<E>) new LuckPermsTrigger(classname, variables, maths, actions);
		} else { //sponge events do not have to be inside the sponge package
			return (Trigger<E>) new SpongeTrigger(classname, variables, maths, actions);
		}
	}

}
