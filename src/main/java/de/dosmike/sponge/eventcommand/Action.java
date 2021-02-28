package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Action {

    String command;
    Pattern pat = Pattern.compile("(\\$\\{\\p{L}+})");
    CommandSourceResolver resolver;

    public Action(String action, CommandSourceResolver resolver) {
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
    protected Action() {
    }

    void run(Object context, Map<String, Object> variables) {
        Sponge.getCommandManager().process(resolver.get(context), Utils.resolveVariables(command, variables));
    }

}
