package de.dosmike.sponge.eventcommand;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(id = "eventcommand", name = "Event Command", version = "1.0")
public class EventCommand {

    private static EventCommand instance;
    static EventCommand getInstance() { return EventCommand.instance; }

    @Inject
    private Logger logger;
    public static void l(String format, Object... args) {
        instance.logger.info(String.format(format, args));
    }
    public static void w(String format, Object... args) {
        instance.logger.warn(String.format(format, args));
    }

    @Listener
    public void onPreLoad(GamePreInitializationEvent event) {
        EventCommand.instance = this;
    }

    @Listener
    public void onLoad(GameInitializationEvent event) {
        load();
        Commands.register();
    }
    @Listener
    public void onReload(GameReloadEvent event) {
        unload();
        load();
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    List<Trigger> triggerList = new ArrayList<>();

    public static void simplePrint(Throwable x) {
        String padding="";
        while (padding.length()<15) {
            w("%s> %s: %s", padding, x.getClass().getSimpleName(), x.getMessage());
            x = x.getCause();
            if (x == null || x.equals(x.getCause())) break;
            padding+=" ";
        }
    }

    void load() {
        try {
            writeExample();
            for (Path path : Files.list(privateConfigDir)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".ec") && Files.isRegularFile(path))
                    .collect(Collectors.toSet()) ) {
                loadFile(path); //not using .forEach to catch errors
            }
            l("Successfully loaded %d triggers", triggerList.size());
        } catch (Exception e) {
            w("Error while loading scripts!");
            simplePrint(e);
        }
    }
    void loadFile(Path path) throws IOException {
        int lineNo=0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader( Files.newInputStream(path) ));
            String line;
            String className = null;
            List<WithChain> variables = new ArrayList<>();
            List<Action> actions = new ArrayList<>();
            // 0: expect event name
            // 1: expect with-chains or (switch) first action
            // 2: expect actions or (switch) event name
            int state=0;

            while ((line=br.readLine())!=null) { lineNo++;
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty()) continue; //comments
                else if (line.startsWith("@")) {
                    if (state == 1) throw new IllegalStateException("Expected 'with'-chains or actions at line "+lineNo);
                    else if (className!=null) {
                        try {
                            triggerList.add(new Trigger(className, variables, actions));
                        } catch (Exception e) {
                            throw new IOException("Unable to create trigger \""+className+"\" before line "+lineNo, e);
                        }
                        variables.clear();
                        actions.clear();
                    }
                    className = line.substring(1).trim();
                    state = 1;
                } else if (line.toLowerCase().startsWith("with")) {
                    if (state == 0) throw new IllegalStateException("Expected event name at line "+lineNo);
                    else if (state == 2) throw new IllegalStateException("Expected action or event name at line "+lineNo);
                    try {
                        variables.add(new WithChain(line));
                    } catch (Exception e) {
                        throw new IOException("Unable to parse 'with'-chain at line "+lineNo, e);
                    }
                } else {
                    if (state == 0) throw new IllegalStateException("Expected event name at line "+lineNo);
                    try {
                        if (line.startsWith("!")) {
                            actions.add(new Action.ServerCommand(line.substring(1).trim()));
                        } else {
                            actions.add(new Action.PlayerCommand(line));
                        }
                    } catch (Exception e) {
                        throw new IOException("Unable to parse commands at line "+lineNo, e);
                    }
                    state = 2;
                }
            }
            if (className != null) {
                if (state < 2) throw new IllegalStateException("Unexpected end of file, actions expected");
                try {
                    triggerList.add(new Trigger(className, variables, actions));
                } catch (Exception e) {
                    throw new IOException("Unable to create trigger \"" + className + "\" before line " + lineNo, e);
                }
            }

        } catch (Exception e) {
            throw new IOException("Unable to load file \""+path.toString()+"\"", e);
        } finally {
            try { br.close(); } catch (Exception ignore) {}
        }
    }
    void unload() {
        Sponge.getEventManager().unregisterPluginListeners(instance);
        triggerList.clear();
    }

    void writeExample() throws IOException {
        if (Files.isDirectory(privateConfigDir)) return;
        Files.createDirectories(privateConfigDir);
        String[] example = {
                "@ org.spongepowered.api.event.advancement.AdvancementEvent$Grant",
                "  with player as getTargetEntity getName",
                "  with advancement as getAdvancement toToastText 1",
                "  -- exclamation point to send message as server",
                "  !say ${player} got advancement ${advancement}",
                "@ org.spongepowered.api.event.entity.TameEntityEvent",
                "  with player as &org.spongepowered.api.entity.living.player.Player getName",
                "  with entity as getTargetEntity getType getName",
                "  /tell @p You tamed this ${entity}"
        };
        Files.write(privateConfigDir.resolve("example.ec"), Arrays.asList(example), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

}
