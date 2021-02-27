package de.dosmike.sponge.eventcommand;

import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.source.EntitySource;
import net.luckperms.api.event.type.Sourced;
import net.luckperms.api.platform.PlatformEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;

import java.util.Objects;

public class LPSourceResolver extends SpongeSourceResolver {

	@Override
	public CommandSource get(Object event) {
		if (event instanceof Sourced) {
			if (((Sourced) event).getSource() instanceof EntitySource) {
				PlatformEntity pent = ((EntitySource) ((Sourced) event).getSource()).getEntity();
				if (pent.getType() == PlatformEntity.Type.CONSOLE) {
					return Sponge.getServer().getConsole();
				} else {
					return Sponge.getServer().getPlayer(Objects.requireNonNull(pent.getUniqueId())).orElseThrow(() -> new IllegalStateException("Could not fetch source of event \"" + event.getClass().getSimpleName() + "\""));
				}
			} else {
				throw new IllegalStateException("Could not get source of sourced event \"" + event.getClass().getSimpleName() + "\"");
			}
		} else if (event instanceof LuckPermsEvent) {
			throw new IllegalStateException("The event \"" + event.getClass().getSimpleName() + "\" is not sourced (Actions should probably be run as console)");
		} else {
			return super.get(event);
		}
	}
}
