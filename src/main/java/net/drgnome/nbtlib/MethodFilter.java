// Bukkit Plugin "NBTLib" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by/3.0/

package net.drgnome.nbtlib;

import java.lang.reflect.Method;

/**
 * <p>When creating a new proxy using {@link ClassProxy}, you may specify a MethodFilter to select which methods should be overridden and which not.</p>
 *
 * @since 0.3
 */
public interface MethodFilter
{
    /**
     * <p>Returns a boolean indicating whether or not to filter a {@link Method}.</p>
     * <p>In use with {@link ClassProxy}, a return value of {@code true} means to override the method in question, a {@code false} means to keep the method of the super class.</p>
     *
     * @param method    The method which needs to be checked.
     *
     * @return {@code true} if the method should be filtered (if it matches), {@code false} otherwise.
     */
    public abstract boolean filterMethod(Method method);
}