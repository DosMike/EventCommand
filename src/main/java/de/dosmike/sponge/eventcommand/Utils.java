package de.dosmike.sponge.eventcommand;

import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Utils {

    static <T> Optional<Class<? extends T>> tryLoad(String className, Class<T> baseClass) {
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

}
