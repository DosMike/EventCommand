# Event Command
Triggers commands based on events.

## Scripts

Put all scripts into the `config/eventcommand` folder. Scripts have to end with the extension `.ec` (Save as any file \*.\* if your system is hiding file extensions).

You can have any amount of files in that folder.

Lines that start with `--` are ignored.
Indentation throughout the file has to be consistently either tabs or spaces.
The whole script (with exception to event names and mappers) is case-insensitive, although it is recommended to use consistent case for readability.

There are 4 types of statements: Triggers, 'with'-chains, mathematics and filters/actions.
'With'-chains are optional, as are mathematics. If you want to do any mathematics, they have to be after 'with'-chains.

### Trigger

Listen to an event with `@ Event`. The event has to be the fully qualified name.
For example `org.spongepowered.api.event.network.ClientConnectionEvent$Join`
You can look up all sponge events on https://jd.spongepowered.org/7.2.0/index.html but note
that dots in class names (nested classes) have to be replaced with dollar signs 
(`ClientConnectionEvent.Join` -> `ClientConnectionEvent$Join`).

### 'with'-chains

Following the listener declaration you can extract information from the event using 'with'-chains.
The syntax is `with NAME as MAPPERS` where NAME is any label (without spaces). MAPPERS is a chain of one or more elements from this list:
 * `&classname` searches the cause stack for the first occurance of this type. Requires a fully qualified name, like event class names.
 * `#key` tries to get a `Key` value from the ValueContainer. Supports field names of [Keys](https://jd.spongepowered.org/7.3.0/org/spongepowered/api/data/key/Keys.html). For other `Key`s you have to use the `Key`'s id.
 * numbers to index into arrays, `List`s or `Iterable`s
 * method names to get values. Methods may not be static or void return.

The following variables are available by default but can be overwritten on event basis:
 * `GAME` contains `Sponge.getGame()`
 * `SERVER` contains `Sponge.getServer()`

### Mathematics

All calculations are done as double. The syntax for it is `let NAME be MATH EXPRESSION`.
If you don't like to use the keyword `be` you can also use a simple `=` or `:=`.
The `MATH EXPRESSION` supports variables in the `${NAME}` format.
Multiplication, division, addition, subtraction, powers(`^`), modulo(`%`) and grouping(`()`) are supported.

### Filters

If you don't want an action to run every time, you can filter them as well. The syntax is:
```
for CONDITION
  actions/nested filters
otherwise
  actions/nested filters
```
The otherwise block is optional, but cannot be the only block.
Actions blocks are grouped by indentation, this means that every case (you can have more than one for block)
has to be written on the same indentation level, and all actions have to be placed deeper.
Every condition can specify an arbitrary amount of `CONDITIONS`, but the `otherwise` block has to go without.

#### Conditions

On **Strings**: String Literals are enclosed in double-quotes(`"`).
Supported escape sequences are: `\n`, `\t`, `\\` and `\"` 
* `global every DURATION`   
  `${PLAYER} every DURATION`   
  This filter restricts execution on a time base. `DURATION` can be things like 10m, 1h, 30s or in the format mm:ss or hh:mm:ss.
  Cooldowns per player require a variable that represents a Player object, player name or player uuid.
* `${PLAYER} hasPermission "PERMISSION"`
  For some actions you might want to check player permissions. This condition requires `PERMISSION` to be quoted.
  The player variable again has to represent a Player object, player name or player uuid. 
* Comparisons   
  You can compare two values, where at lease one has to be a variable. The comparing type has to be numeric or plain text.   
  For numbers the comparators `<`, `>`, `<=`, `>=`, `==` (or `=`), `!=` (or `<>`) are available.   
  For strings the comparators `<`, `>`, `<=`, `>=`, `===`, `!=` (or `<>`) make case-sensitive lexicographic comparisons.
  Case-insensitive comparison can be done with `=` and `==`.
  The special comparator `matches` compiles a right hand string literal into a regex and matches the left hand against it.
* `not CONDITION`   
  Any condition can be inverted once by prefixing it with the word `not`

### Actions

Lastly you can specify actions your trigger should take.
Actions are command that are either executed as the player triggering the event, or as console if you prefix the
command with an exclamation point (`!`) instead of a slash (`/`). Leading slashes for commands are optional.
You can insert the values from 'with'-chains with `${NAME}`.
Please also keep in mind that not every event has a player as cause, so you might use something like
`!execute as ${PLAYER} run ...`.

After running a action the following variables will be updated or removed automatically, based on the command:
 * `successCount` the commands' success count, if applicable. Usually > 0 on success, 0 otherwise.
 * `queryResult` the commands' result count, if applicable.
 * `affectedBlocks` the number of affected blocks, if applicable.
 * `affectedEntities` the number of affected entities, if applicable.
 * `affectedItems` the number of affected items, if applicable.

####Cancelling Events

You can also cancel cancellable events with the special action `cancel`.

**This is not a command!** Do not prefix this action with `/` or `!`.

## Plugin Support

Event Command also supports [LuckPerms](https://luckperms.net/) events.
For a list of LP Events, see the [LuckPerms SourceCode](https://github.com/lucko/LuckPerms/tree/master/api/src/main/java/net/luckperms/api/event)
(make sure the classes extend LuckPermsEvent).

## Examples

```
-- Sponge event
@ org.spongepowered.api.event.entity.TameEntityEvent
  -- player should be this most of the time:
  with player as &org.spongepowered.api.entity.living.player.Player getName
  with entity as getTargetEntity getType getName
  !tell ${player} You tamed this ${entity}

@ org.spongepowered.api.event.network.ClientConnectionEvent$Join
  with player as getTargetEntity getName
  with world as getTargetEntity getLocation getExtent getName
  for ${world} != "overworld"
    -- teleport to spawn if no /spawn command is available
    !execute in "overworld" run tp ${player} 0 60 0 

-- Event for other Plugins
@ de.dosmike.sponge.minesweeper.MinesweeperGameEvent$Victory
  with player as getTargetEntity getName
  for ${player} every 5m
    !effect ${player} luck 30 1
  otherwise
    !tellraw ${player} {"text":"Sorry, but you've already received the luck effect recently","color":"red"}

@ de.dosmike.sponge.minesweeper.MinesweeperGameEvent$Defeat
  with player as getTargetEntity getName
  for ${player} every 5min
    !effect ${player} unluck 30 1
```

### Need Help?
#### [Join my Discord](https://discord.gg/E592Gdu)