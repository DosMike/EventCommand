package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.statements.luckperms.LuckPermsSourceResolver;
import de.dosmike.sponge.eventcommand.statements.sponge.SpongeSourceResolver;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

import java.util.LinkedList;
import java.util.List;

@FunctionalInterface
public interface CommandSourceResolver {

	CommandSource get(Object event);

	default boolean canResolve(Object object) { return true; }

	class Factory {

		static CommandSourceResolver player = null;
		static CommandSourceResolver server = e -> Sponge.getServer().getConsole();
		static List<CommandSourceResolver> playerResolvers = new LinkedList<>();

		public static CommandSourceResolver Server() {
			return server;
		}

		public static CommandSourceResolver Player() {
			if (player == null) {
				EventCommand.l("Loading Source Resolvers...");
				if (DependencyProxy.isLuckPermsLoaded()) {
					playerResolvers.add(new LuckPermsSourceResolver());
				}
				CommandSourceResolver vanillaPlayer = new SpongeSourceResolver();
				player = e->{
					for (CommandSourceResolver r : playerResolvers) if (r.canResolve(e)) return r.get(e);
					return vanillaPlayer.get(e);
				};
			}
			return player;
		}

	}

}
