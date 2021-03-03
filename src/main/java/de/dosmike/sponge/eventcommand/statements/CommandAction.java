package de.dosmike.sponge.eventcommand.statements;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.VariableContext;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandAction implements Action {

    String command;
    Pattern pat = Pattern.compile("(\\$\\{\\p{L}+})");
    CommandSourceResolver resolver;

    public CommandAction(String action, CommandSourceResolver resolver) {
        this.resolver = resolver;

        command = action.charAt(0) == '/' ? action.substring(1) : action;
        Matcher matcher = pat.matcher(command);
        StringBuffer sb = new StringBuffer();
        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(command, lastEnd, matcher.start());
            sb.append(matcher.group().toLowerCase());
            lastEnd = matcher.end();
        }
        if (lastEnd < command.length())
            sb.append(command.substring(lastEnd));
        command = sb.toString();
    }
    protected CommandAction() {
    }

    @Override
    public void run(Object context, VariableContext variables) {
        if (EventCommand.isVerboseLogging())
            EventCommand.l("  Executing %s", command);
        CommandSource executor = resolver.get(context); //where is this executed (probably console)
        String resolvedCommand = variables.resolveVariables(command); //plug in all variables
        //we might be async here, maybe sync manually instead of having sponge complain?
        CommandResult result = Sponge.getCommandManager().process(executor, resolvedCommand); //execute command
        variables.read(result); //read result values back into variable context
    }

}
