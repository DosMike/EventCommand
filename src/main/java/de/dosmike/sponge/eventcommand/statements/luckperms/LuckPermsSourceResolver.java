package de.dosmike.sponge.eventcommand.statements.luckperms;

import de.dosmike.sponge.eventcommand.statements.sponge.SpongeSourceResolver;
import de.dosmike.sponge.eventcommand.exception.MissingEventSourceException;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.source.EntitySource;
import net.luckperms.api.event.type.Sourced;
import net.luckperms.api.platform.PlatformEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

import java.util.Objects;

public class LuckPermsSourceResolver extends SpongeSourceResolver {

	@Override
	public CommandSource get(Object event) {
		if (event instanceof Sourced) {
			if (((Sourced) event).getSource() instanceof EntitySource) {
				PlatformEntity pent = ((EntitySource) ((Sourced) event).getSource()).getEntity();
				if (pent.getType() == PlatformEntity.Type.CONSOLE) {
					return Sponge.getServer().getConsole();
				} else {
					return Sponge.getServer().getPlayer(Objects.requireNonNull(pent.getUniqueId())).orElseThrow(() -> new MissingEventSourceException("Could not fetch source of event \"" + event.getClass().getSimpleName() + "\""));
				}
			} else {
				throw new MissingEventSourceException("Could not get source of sourced event \"" + event.getClass().getSimpleName() + "\"");
			}
		} else {
			throw new MissingEventSourceException("The event \"" + event.getClass().getSimpleName() + "\" is not sourced (Actions should probably be run as console)");
		}
	}

	@Override
	public boolean canResolve(Object object) {
		return object instanceof LuckPermsEvent;
	}
}
