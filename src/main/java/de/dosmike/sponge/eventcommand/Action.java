package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Action {

    String command;
    Pattern pat = Pattern.compile("(\\$\\{\\p{L}+})");

    private Action(String action) {
        command = action.charAt(0)=='/' ? action.substring(1) : action;
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

    abstract CommandSource from(Event context);
    void run(Event context, Map<String, String> variables) {
        String action = command;
        for (Map.Entry<String, String> e : variables.entrySet()) {
            action = action.replace("${"+e.getKey()+"}", e.getValue());
        }
        Sponge.getCommandManager().process(from(context), action);
    }

    static class PlayerCommand extends Action {
        public PlayerCommand(String action) {
            super(action);
        }
        @Override
        CommandSource from(Event context) {
            return context.getCause().first(Player.class).orElseThrow(()->new IllegalStateException("Could not retrieve player for player exec"));
        }
    }

    static class ServerCommand extends Action {
        public ServerCommand(String action) {
            super(action);
        }
        @Override
        CommandSource from(Event context) {
            return Sponge.getServer().getConsole();
        }
    }

}
