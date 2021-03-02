package de.dosmike.sponge.eventcommand.statements;

import org.spongepowered.api.Sponge;

public class DependencyProxy {

	public static boolean isLuckPermsClass(Class<?> clz) {
		return (clz.getName().startsWith("net.luckperms.api.event."));
	}
	public static boolean isLuckPermsClass(String classname) {
		return (classname.startsWith("net.luckperms.api.event."));
	}

	private static Boolean loadedCache_LuckPerms = null;
	public static boolean isLuckPermsLoaded() {
		if (loadedCache_LuckPerms == null)
			loadedCache_LuckPerms = Sponge.getPluginManager().getPlugin("luckperms").isPresent();
		return loadedCache_LuckPerms;
	}

}
