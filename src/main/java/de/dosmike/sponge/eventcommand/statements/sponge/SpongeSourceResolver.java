package de.dosmike.sponge.eventcommand.statements.sponge;

import de.dosmike.sponge.eventcommand.statements.CommandSourceResolver;
import de.dosmike.sponge.eventcommand.exception.MissingEventSourceException;
import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

public class SpongeSourceResolver implements CommandSourceResolver {

	@Override
	public CommandSource get(Object event) {
		if (event instanceof Event) {
			return ((Event) event).getCause().first(Player.class)
					.orElseThrow(() -> new MissingEventSourceException("Could not retrieve player for player exec"));
		} else {
			throw new ScriptExecutionException("The supplied event \"" + event.getClass().getSimpleName() + "\" is not recognized");
		}
	}
}
