package de.dosmike.sponge.eventcommand.statements;

import de.dosmike.sponge.eventcommand.EventCommand;
import de.dosmike.sponge.eventcommand.VariableContext;
import org.spongepowered.api.Sponge;

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
        Sponge.getCommandManager().process(resolver.get(context), variables.resolveVariables(command));
    }

}
