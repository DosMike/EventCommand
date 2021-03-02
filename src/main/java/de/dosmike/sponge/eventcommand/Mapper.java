package de.dosmike.sponge.eventcommand;

import de.dosmike.sponge.eventcommand.exception.ScriptExecutionException;
import de.dosmike.sponge.eventcommand.exception.StatementParseException;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.event.Event;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class Mapper {

    String key;
    abstract Optional<?> map(Optional<?> in);

	static class Getter extends Mapper {
		public Getter(String method) {
			key = method;
		}

		private boolean hasMethod(Class<?> clz, String name) {
			try {
				clz.getMethod(name);
				return true;
			} catch (NoSuchMethodException e) {
				return false;
			}
		}

		private boolean hasField(Class<?> clz, String name) {
			try {
				clz.getField(name);
				return true;
			} catch (NoSuchFieldException e) {
				return false;
			}
		}

		Optional<?> map(Optional<?> in) {
			try {
				if (hasMethod(in.get().getClass(), key)) {
					Method m = in.get().getClass().getMethod(key);
					m.setAccessible(true);
					if ((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
						throw new IllegalAccessException("Static methods are not supported in the chain");
					if (m.getReturnType().equals(Void.TYPE))
						throw new IllegalAccessException("Methods in the chain need to return values");
					return Utils.makeOptional(m.invoke(in.get()));
				} else if (hasField(in.get().getClass(), key)) {
					Field f = in.get().getClass().getField(key);
					f.setAccessible(true);
					if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
						throw new IllegalAccessException("Static fields are not supported in the chain");
					return Utils.makeOptional(f.get(in.get()));
				} else
					throw new NoSuchElementException();
			} catch (Exception e) {
				throw new ScriptExecutionException("Error in 'with'-chain at \"" + toString() + "\": Invalid member or input", e);
            }
        }

        @Override
        public String toString() {
            return key;
        }
    }

    static class Keys extends Mapper {
        String id;
        public Keys(String keyName) {
            key = keyName;
            try {
                //try to resolve the key name against the Keys catalog
                Object keyObject = Arrays.asList(org.spongepowered.api.data.key.Keys.class.getFields()).stream().filter(k->k.getName().equalsIgnoreCase(keyName)).findFirst().get().get(null);
                id = ((CatalogType)keyObject).getId();
            } catch (Exception e) {
                //otherwise assume the key was referenced by it's id
                Sponge.getRegistry().getType(Key.class, key).orElseThrow(()->new StatementParseException("There is no such Key: \""+keyName+"\""));
                id = key;
            }
        }
        <E> Optional<E> get(ValueContainer<?> dh, Object key) {
            //small required type hack due to erased types
            // -> input from .map call is Key<capture of ?>
            //    and that's pretty useless
            // optional of require allows to print into console if a key does not exist since it throws
            return Optional.of(dh.require((Key<? extends BaseValue<E>>)key));
        }
        Optional<?> map(Optional<?> in) {
            if (in.orElse(null) instanceof ValueContainer<?>) {
                ValueContainer<?> dh = (ValueContainer<?>) in.get();
                return Utils.makeOptional( dh.getKeys().stream()
                        .filter(k -> k.getId().equalsIgnoreCase(id))
                        .findFirst()
                        .map(k-> get(dh, k)) );
            } else {
                throw new ScriptExecutionException("Error in 'with'-chain at \""+toString()+"\": Key not supported by input");
            }
        }

        @Override
        public String toString() {
            return "#"+key;
        }
    }

    static class Cause extends Mapper {
        Class<?> clz;
        public Cause(String keyName) {
            key = keyName;
            clz = Utils.tryLoad(keyName).orElseThrow(()->new StatementParseException("No class found for \""+key+"\""));
        }
        Optional<?> map(Optional<?> in) {
            if (in.orElse(null) instanceof Event) {
                Event e = (Event) in.get();
                return Utils.makeOptional( e.getCause().all().stream().filter(cause->
                    clz.isAssignableFrom(cause.getClass())
                ).findFirst() );
            } else {
                throw new ScriptExecutionException("Error in 'with'-chain at \""+toString()+"\": Not available in cause or input not event");
            }
        }

        @Override
        public String toString() {
            return "&"+key;
        }
    }

    static class Index extends Mapper {
        int i;
        public Index(String index) {
            key = index;
            i = Integer.parseInt(key)-1;
            if (i<0) throw new StatementParseException("Index has to be positive (non-null)");
        }
        Optional<?> map(Optional<?> in) {
            if (in.get() instanceof Array) {
                if (i < Array.getLength(in.get())) return Utils.makeOptional(Array.get(in.get(),i));
                else throw new ScriptExecutionException("Error in 'with'-chain at \""+key+"\": Iterable has not enough elements");
            } else if (in.get() instanceof List) {
                List<?> list = (List<?>) in.get();
                if (i < list.size()) return Utils.makeOptional(list.get(i));
                else throw new ScriptExecutionException("Error in 'with'-chain at \""+key+"\": Iterable has not enough elements");
            } else if (in.get() instanceof Iterable) {
                Iterator<?> it = ((Iterable<?>) in.get()).iterator();
                for (int n=0; n<i && it.hasNext(); n++) it.next();
                if (it.hasNext()) return Utils.makeOptional(it.next());
                else throw new ScriptExecutionException("Error in 'with'-chain at \""+key+"\": Iterable has not enough elements");
            } else {
                throw new ScriptExecutionException("Error in 'with'-chain at \""+key+"\": Iterable element expected");
            }
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public static Mapper fromString(String def) {
        if (def.isEmpty() || def.equals("#") || def.equals("&")) throw new StatementParseException("Definition required");
        if (def.charAt(0)=='#') {
            return new Keys(def.substring(1).toUpperCase());
        } else if (def.charAt(0)=='&') {
            return new Cause(def.substring(1));
        } else if (def.matches("^[0-9]+$")) {
            return new Index(def);
        } else {
            return new Getter(def);
        }
    }

}
