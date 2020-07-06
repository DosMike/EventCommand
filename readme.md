# Event Command
Triggers Commands based on events

## Scripts

Put all scripts into the `config/eventcommand` folder. Scripts have to end with the extension `.ec` (Save as any file \*.\* if your system is hiding file extensions).

You can have any amount of files in that folder.

Lines that start with `--` are ignored.

Listen to an event with `@ Event`. The event has to be the fully qualified name.
For example `org.spongepowered.api.event.network.ClientConnectionEvent$Join`
You can look up all sponge events on https://jd.spongepowered.org/7.2.0/index.html but note
that dots in class names (nested classes) have to be replaced with dollar signs 
(`ClientConnectionEvent.Join` -> `ClientConnectionEvent$Join`).

Following the listener declaration you can extract information from the event using 'with'-chains.
The syntax is `with NAME as MAPPERS` where NAME is any label (without spaces). MAPPERS is a chain of one or more elements from this list:
 * `&classname` searches the cause stack for the first occurance of this type. Requires a fully qualified name, like event class names.
 * `#key` tries to get a `Key` value from the ValueContainer. Supports field names of [Keys](https://jd.spongepowered.org/7.2.0/org/spongepowered/api/data/key/Keys.html). For other `Key`s you have to use the `Key`'s id.
 * numbers to index into arrays, `List`s or `Iterable`s
 * method names to get values. Methods may not be static or void return.

Lastly you can specify a list of actions your listener should take. Actions are command that are either executed as the player triggering the event, or as console if you prefix the command with an exclamation point. Leading slashes for commands are optional. You can insert the values from 'with'-chains with `${NAME}`.

```
-- Sponge event
@ org.spongepowered.api.event.entity.TameEntityEvent
  -- player should be this most of the time:
  with player as &org.spongepowered.api.entity.living.player.Player getName
  with entity as getTargetEntity getType getName
  !tell ${player} You tamed this ${entity}

-- Event for other Plugins
@ de.dosmike.sponge.minesweeper.MinesweeperGameEvent$Victory
  with player as getTargetEntity getName
  !effect ${player} luck 30 1

@ de.dosmike.sponge.minesweeper.MinesweeperGameEvent$Defeat
  with player as getTargetEntity getName
  !effect ${player} unluck 30 1
```