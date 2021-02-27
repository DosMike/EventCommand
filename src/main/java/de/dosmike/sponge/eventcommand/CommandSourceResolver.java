package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

@FunctionalInterface
public interface CommandSourceResolver {

	CommandSource get(Object event);

	class Factory {
		static CommandSourceResolver player = null;
		static CommandSourceResolver server = e -> Sponge.getServer().getConsole();

		public static CommandSourceResolver Server() {
			return server;
		}

		public static CommandSourceResolver Player() {
			if (player == null) {
				if (TriggerFactory.get().isLuckPermsLoaded()) {
					player = new LPSourceResolver();
				} else {
					player = new SpongeSourceResolver();
				}
			}
			return player;
		}

	}

}
