/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.arh.gds.message.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author llacz
 */
public class ExceptionHelper {

    private ExceptionHelper() {

    }

    public static <T> T requireNonNullValue(T obj, String className, String valueName) throws NullPointerException {
        return Objects.requireNonNull(obj, String.format("Parameter %1$s of class %2$s cannot be null.",
                valueName != null ? valueName : "<Unknown argument>",
                className != null ? className : "<Unknown class>")
        );
    }

    public static <T> T requireNullValue(T obj, String className, String valueName) throws NullPointerException {
        if(obj != null) {
            throw new IllegalArgumentException(String.format("Parameter %1$s of class %2$s must be null.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
        return obj;
    }

    public static <T> Collection<T> requireNonEmptyCollection(Collection<T> obj, String className, String valueName)
            throws NullPointerException {

        if (obj == null) {
            return obj;
        } else if (!obj.isEmpty()) {
            return obj;
        } else {
            throw new IllegalStateException(String.format("Parameter %1$s of class %2$s cannot be empty.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
    }

    public static <T, E> Map<T, E> requireNonEmptyMap(Map<T, E> obj, String className, String valueName)
            throws NullPointerException {

        if (obj == null) {
            return obj;
        } else if (!obj.isEmpty()) {
            return obj;
        } else {
            throw new IllegalStateException(String.format("Parameter %1$s of class %2$s cannot be empty.",
                    valueName != null ? valueName : "<Unknown argument>",
                    className != null ? className : "<Unknown class>"));
        }
    }

    /*public static String formatSubItemCannotBeNull(String className, String subItemName) {
        return String.format("Subitem %1$s of class %2$s cannot be null.",
                subItemName != null ? subItemName : "<Unknown argument>",
                className != null ? className : "<Unknown class>");
    }

    public static <T> T requireNonNullCtorArgument(T obj, String className, String argumentName) throws
            NullPointerException {
        return Objects.requireNonNull(obj, formatCtorArgumentCannotBeNull(className, argumentName));
    }

    public static String formatCtorArgumentCannotBeNull(String className, String argumentName) {
        return String.format("%1$s constructor failed. Argument %2$s cannot be null.",
                className != null ? className : "<Unknown class>",
                argumentName != null ? argumentName : "<Unknown argument>");
    }

    public static String formatCtorArgumentSubItemCannotBeNull(String className, String argumentName) {
        return String.format("%1$s constructor failed. Argument subitem %2$s cannot be null.",
                className != null ? className : "<Unknown class>",
                argumentName != null ? argumentName : "<Unknown argument>");
    }


    public static <T> T requireNonNullUnpackValue(T obj, String className, String argumentName) throws
            NullPointerException {
        return Objects.requireNonNull(obj, formatUnpackValueCannotBeNull(className, argumentName));
    }

    public static String formatUnpackValueCannotBeNull(String className, String argumentName) {
        return String.format("Failed to unpack %1$s. Argument %2$s cannot be null.",
                className != null ? className : "<Unknown class>",
                argumentName != null ? argumentName : "<Unknown argument>");
    }*/
}
