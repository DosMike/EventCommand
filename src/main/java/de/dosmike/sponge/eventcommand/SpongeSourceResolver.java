package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

public class SpongeSourceResolver implements CommandSourceResolver {

	@Override
	public CommandSource get(Object event) {
		if (event instanceof Event) {
			return ((Event) event).getCause().first(Player.class)
					.orElseThrow(() -> new IllegalStateException("Could not retrieve player for player exec"));
		} else {
			throw new IllegalStateException("The supplied event \"" + event.getClass().getSimpleName() + "\" is not recognized");
		}
	}
}
