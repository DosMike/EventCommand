package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;

public class Utils {

    public static <T> Optional<Class<? extends T>> tryLoad(String className, Class<T> baseClass) {
        Class<?> result;
        try {
            result = Utils.class.getClassLoader().loadClass(className);
            if (baseClass.isAssignableFrom(result)) return Optional.of( (Class<? extends T>) result );
        } catch (ClassNotFoundException e) {
            //
        }
        return Optional.empty();
    }
    static Optional<Class<?>> tryLoad(String className) {
        Class<?> result;
        try {
            return Optional.of(Utils.class.getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            //
        }
        return Optional.empty();
    }

    // unpacks optional<optional<?>> and packs ? to optional<?>
    static Optional<?> makeOptional(Object input) {
        if (input == null) return Optional.empty();
        if (!(input instanceof Optional)) return Optional.of(input);
        Optional<?> optional = (Optional<?>) input;
        if (!optional.isPresent()) return optional;
        if (optional.get() instanceof Optional) return (Optional<?>)optional.get();
        return optional;
    }

    // handle some exceptions differently to .toString()
    static String toString(Object object) {
        if (object instanceof Text) {
            return ((Text) object).toPlain();
        }
        if (object instanceof String) return (String) object;
        return object.toString();
    }

    public static UUID toPlayerUUID(Object object) {
        if (object == null) return null;
        if (object instanceof User) {
            return ((User) object).getUniqueId();
        } else if (object instanceof Subject) {
            CommandSource cs = ((Subject) object).getCommandSource().orElse(null);
            if (cs != null && cs instanceof User) return ((User) cs).getUniqueId();
            else return null;
        } else if (object instanceof UUID) {
            return Sponge.getServer().getPlayer((UUID) object).isPresent() ? (UUID) object : null;
        } else if (object instanceof String) {
            try {
                UUID uuid = UUID.fromString((String) object);
                return Sponge.getServer().getPlayer(uuid).isPresent() ? uuid : null;
            } catch (IllegalArgumentException nouuid) {
                return Sponge.getServer().getPlayer((String) object).map(Identifiable::getUniqueId).orElse(null);
            }
        } else return null; // don't know how to resolve this into a uuid
    }
    public static Double toDouble(Object object) {
        Class<?> clz = object.getClass();
        if (clz == Boolean.class) return ((Boolean)object) ? 1.0 : 0.0;
        else if (clz == Boolean.TYPE) return ((boolean)object) ? 1.0 : 0.0;
        else if (clz == Byte.class) return (double)((Byte)object);
        else if (clz == Byte.TYPE) return (double)((byte)object);
        else if (clz == Short.class) return (double)((Short)object);
        else if (clz == Short.TYPE) return (double)((short)object);
        else if (clz == Character.class) return (double)((Character)object);
        else if (clz == Character.TYPE) return (double)((char)object);
        else if (clz == Integer.class) return (double)((Integer)object);
        else if (clz == Integer.TYPE) return (double)((int)object);
        else if (clz == Long.class) return (double)((Long)object);
        else if (clz == Long.TYPE) return (double)((long)object);
        else if (clz == Float.class) return (double)((Float)object);
        else if (clz == Float.TYPE) return (double)((float)object);
        else if (clz == Double.class) return (double)((Double)object);
        else if (clz == Double.TYPE) return (double)(object);
        else if (clz == String.class) return Double.parseDouble((String) object);
        else throw new IllegalArgumentException("Cannot convert object to double numeric");
    }

    public static int firstIndexOf(String haystack, String needles, int offset) {
        char[] nx = needles.toCharArray();
        int min = Integer.MAX_VALUE;
        boolean found = false;
        for (char c : nx) {
            int at = haystack.indexOf(c, offset);
            if (at>=0) {
                found = true;
                if (at < min) min = at;
            }
        }
        return found ? min : -1;
    }

}
