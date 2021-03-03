package de.dosmike.sponge.eventcommand;

import de.dosmike.sponge.eventcommand.exception.NoVariableValueException;
import de.dosmike.sponge.eventcommand.exception.VariableTypeException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class VariableContext extends HashMap<String,Object> {

	public static final String VAR_GAME = "GAME";
	public static final String VAR_SERVER = "SERVER";
	public static final String VAR_SUCCESSCOUNT = "successCount";
	public static final String VAR_QUERYRESULT = "queryResult";
	public static final String VAR_AFFECTEDBLOCKS = "affectedBlocks";
	public static final String VAR_AFFECTEDENTITIES = "affectedEntities";
	public static final String VAR_AFFECTEDITEMS = "affectedItems";

	public VariableContext() {
		put(VAR_GAME, Sponge.getGame());
		put(VAR_SERVER, Sponge.getServer());
	}

	public Object require(String variable) {
		Object t = get(variable);
		if (t == null) throw new NoVariableValueException("The Variable \""+variable+"\" did not contain a value");
		return t;
	}

	// resolve vars into a string using ${} placeholders
	public String resolveVariables(String entry) {
	    for (Entry<String, Object> e : this.entrySet()) {
	        String key = "${" + e.getKey() + "}";
	        if (entry.contains(key)) {
	            entry = entry.replace(key, e.getValue().toString());
	        }
	    }
	    return entry;
	}

	public String resolveVariablesNumeric(String entry) {
	    for (Entry<String, Object> e : this.entrySet()) {
	        String key = "${" + e.getKey() + "}";
	        if (entry.contains(key)) {
	        	try {
			        entry = entry.replace(key, Utils.toDouble(e.getValue()).toString());
		        } catch (IllegalArgumentException exception) {
	        		throw new VariableTypeException(exception);
		        }
	        }
	    }
	    return entry;
	}

	public void read(CommandResult process) {
		getOr(process.getSuccessCount(), (c)->put(VAR_SUCCESSCOUNT, c), ()->remove(VAR_SUCCESSCOUNT));
		getOr(process.getQueryResult(), (c)->put(VAR_QUERYRESULT, c), ()->remove(VAR_QUERYRESULT));
		getOr(process.getAffectedBlocks(), (c)->put(VAR_AFFECTEDBLOCKS, c), ()->remove(VAR_AFFECTEDBLOCKS));
		getOr(process.getAffectedEntities(), (c)->put(VAR_AFFECTEDENTITIES, c), ()->remove(VAR_AFFECTEDENTITIES));
		getOr(process.getAffectedItems(), (c)->put(VAR_AFFECTEDITEMS, c), ()->remove(VAR_AFFECTEDITEMS));
	}

	private static <T> void getOr(Optional<T> optional, Consumer<T> present, Runnable absent) {
		if (optional.isPresent()) {
			present.accept(optional.get());
		} else {
			absent.run();
		}
	}

	private static Object key2lower(Object key) {
		return key instanceof String ? ((String)key).toLowerCase(Locale.ROOT) : key;
	}

	@Override
	public Object get(Object key) {
		return super.get(key2lower(key));
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key2lower(key));
	}

	@Override
	public Object put(String key, Object value) {
		return super.put(key.toLowerCase(Locale.ROOT), value);
	}

	@Override
	public Object remove(Object key) {
		return super.remove(key2lower(key));
	}

	@Override
	public Object getOrDefault(Object key, Object defaultValue) {
		return super.getOrDefault(key2lower(key), defaultValue);
	}

	@Override
	public Object putIfAbsent(String key, Object value) {
		return super.putIfAbsent(key.toLowerCase(Locale.ROOT), value);
	}

	@Override
	public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
		return super.computeIfAbsent(key.toLowerCase(Locale.ROOT), mappingFunction);
	}

	@Override
	public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
		return super.computeIfPresent(key.toLowerCase(Locale.ROOT), remappingFunction);
	}
}
