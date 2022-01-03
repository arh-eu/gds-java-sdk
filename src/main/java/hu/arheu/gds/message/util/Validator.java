
package hu.arheu.gds.message.util;

import hu.arheu.gds.message.errors.ValidationException;

import java.util.Collection;
import java.util.Map;

/**
 * @author llacz
 */
public class Validator {

    private Validator() {

    }

    public static <T> T requireNonNullValue(T obj, String className, String valueName) throws ValidationException {
        if (obj == null) {
            throw new ValidationException(String.format("Parameter %1$s of class %2$s cannot be null.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>")
            );
        }
        return obj;
    }

    public static <T> T requireNullValue(T obj, String className, String valueName) throws ValidationException {
        if (obj != null) {
            throw new ValidationException(String.format("Parameter %1$s of class %2$s must be null!",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
        return null;
    }

    public static <T> Collection<T> requireNonEmptyCollection(Collection<T> obj, String className, String valueName)
            throws ValidationException {

        if (obj == null) {
            return null;
        } else if (!obj.isEmpty()) {
            return obj;
        } else {
            throw new ValidationException(String.format("Parameter %1$s of class %2$s cannot be empty.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
    }

    public static <T, E> Map<T, E> requireNonEmptyMap(Map<T, E> obj, String className, String valueName)
            throws ValidationException {

        if (obj == null || !obj.isEmpty()) {
            return obj;
        } else {
            throw new ValidationException(String.format("Parameter %1$s of class %2$s cannot be empty.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
    }
}
