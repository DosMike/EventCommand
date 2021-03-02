package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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

    private static CommandSpec subcmdDebug() {
        return CommandSpec.builder()
                .permission("eventcommand.verbose")
                .arguments(GenericArguments.none())
                .executor((src,args)->{
                    if (EventCommand.verbosity >= 2) EventCommand.verbosity = 0;
                    else EventCommand.verbosity ++;

                    String mode = "Off";
                    if (EventCommand.verbosity == 1) mode = "On";
                    else if (EventCommand.verbosity > 1) mode = "Throwing";

                    src.sendMessage(Text.of(TextColors.YELLOW, "[EC] Debug Mode is now ", TextColors.RESET, mode));
                    return CommandResult.success();
                })
                .build();
    }

    static void register() {
        Sponge.getCommandManager().register(EventCommand.getInstance(), CommandSpec.builder()
                .child(subcmdReload(), "reload")
                .child(subcmdDebug(), "debug")
                .build(), "eventcommand", "ec");
    }

}
