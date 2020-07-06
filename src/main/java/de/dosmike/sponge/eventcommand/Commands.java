package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;

public class Commands {

    private static CommandSpec subcmdReload() {
        return CommandSpec.builder()
                .permission("eventcommand.reload")
                .arguments(GenericArguments.none())
                .executor((src,args)->{
                    EventCommand.getInstance().onReload(null);
                    return CommandResult.success();
                })
                .build();
    }

    static void register() {
        Sponge.getCommandManager().register(EventCommand.getInstance(), CommandSpec.builder()
                .child(subcmdReload(), "reload")
                .build(), "eventcommand", "ec");
    }

}
