// Bukkit Plugin "NBTLib" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by/3.0/

package net.drgnome.nbtlib;

import java.util.*;
import java.lang.reflect.*;

/**
 * <p>A representation of Minecraft NBT tags.</p>
 */
public final class Tag<T>
{
    private final NBT _type;
    private final T _data;
    
    /**
     * Creates a new byte tag.
     *
     * @param data The byte.
     *
     * @return The byte tag.
     */
    public static Tag<Boolean> newBool(boolean data)
    {
        return new Tag<Boolean>(NBT.BOOL, data);
    }
    
    /**
     * Creates a new byte tag.
     *
     * @param data The byte.
     *
     * @return The byte tag.
     */
    public static Tag<Byte> newByte(byte data)
    {
        return new Tag<Byte>(NBT.BYTE, data);
    }
    
    /**
     * Creates a new short tag.
     *
     * @param data The short.
     *
     * @return The short tag.
     */
    public static Tag<Short> newShort(short data)
    {
        return new Tag<Short>(NBT.SHORT, data);
    }
    
    /**
     * Creates a new int tag.
     *
     * @param data The int.
     *
     * @return The int tag.
     */
    public static Tag<Integer> newInt(int data)
    {
        return new Tag<Integer>(NBT.INT, data);
    }
    
    /**
     * Creates a new long tag.
     *
     * @param data The long.
     *
     * @return The long tag.
     */
    public static Tag<Long> newLong(long data)
    {
        return new Tag<Long>(NBT.LONG, data);
    }
    
    /**
     * Creates a new float tag.
     *
     * @param data The float.
     *
     * @return The float tag.
     */
    public static Tag<Float> newFloat(float data)
    {
        return new Tag<Float>(NBT.FLOAT, data);
    }
    
    /**
     * Creates a new double tag.
     *
     * @param data The double.
     *
     * @return The double tag.
     */
    public static Tag<Double> newDouble(double data)
    {
        return new Tag<Double>(NBT.DOUBLE, data);
    }
    
    /**
     * Creates a new byte array tag.
     *
     * @param data The byte array.
     *
     * @return The byte array tag.
     */
    public static Tag<byte[]> newByteArray(byte[] data)
    {
        return new Tag<byte[]>(NBT.BYTE_ARRAY, data);
    }
    
    /**
     * Creates a new String tag.
     *
     * @param data The String.
     *
     * @return The String tag.
     */
    public static Tag<String> newString(String data)
    {
        return new Tag<String>(NBT.STRING, data);
    }
    
    /**
     * Creates a new List tag.
     *
     * @param data The List.
     *
     * @return The List tag.
     *
     * @throws UnknownTagException If the List contains an element that can't be represented by a {@link Tag} object.
     */
    public static Tag<List<Tag>> newList(List data)
    throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
    {
        List<Tag> list;
        try
        {
            for(Object o : data.toArray())
            {
                Tag.class.cast(o);
            }
            list = (List<Tag>)data;
        }
        catch(ClassCastException e)
        {
            list = new ArrayList<Tag>();
            for(Object o : data.toArray())
            {
                list.add(parse(o));
            }
        }
        return new Tag<List<Tag>>(NBT.LIST, list);
    }
    
    /**
     * Creates a new Map tag.
     *
     * @param data The Map.
     *
     * @return The Map tag.
     *
     * @throws UnknownTagException If the Map contains a value that can't be represented by a {@link Tag} object.
     */
    public static Tag<Map<String, Tag>> newCompound(Map<String, ?> data)
    throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
    {
        Map<String, Tag> map;
        try
        {
            for(Object o : data.values().toArray())
            {
                Tag.class.cast(o);
            }
            map = (Map<String, Tag>)data;
        }
        catch(ClassCastException e)
        {
            map = new HashMap<String, Tag>();
            for(Map.Entry<String, ?> entry : data.entrySet())
            {
                map.put(entry.getKey(), parse(entry.getValue()));
            }
        }
        return new Tag<Map<String, Tag>>(NBT.COMPOUND, map);
    }
    
    /**
     * Creates a new int array tag.
     *
     * @param data The int array.
     *
     * @return The int array tag.
     */
    public static Tag<int[]> newIntArray(int[] data)
    {
        return new Tag<int[]>(NBT.INT_ARRAY, data);
    }
    
    private static Tag parse(Object o)
    throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException, NBTLibDisabledException, UnknownTagException
    {
        if(o instanceof Tag)
        {
            return (Tag)o;
        }
        else if(o instanceof Boolean)
        {
            return newBool((Boolean)o);
        }
        else if(o instanceof Byte)
        {
            return newByte((Byte)o);
        }
        else if(o instanceof Short)
        {
            return newShort((Short)o);
        }
        else if(o instanceof Integer)
        {
            return newInt((Integer)o);
        }
        else if(o instanceof Long)
        {
            return newLong((Long)o);
        }
        else if(o instanceof Float)
        {
            return newFloat((Float)o);
        }
        else if(o instanceof Double)
        {
            return newDouble((Double)o);
        }
        else if(o instanceof byte[])
        {
            return newByteArray((byte[])o);
        }
        else if(o instanceof String)
        {
            return newString((String)o);
        }
        else if(o instanceof List)
        {
            return newList((List)o);
        }
        else if(o instanceof Map)
        {
            return newCompound((Map)o);
        }
        else if(o instanceof int[])
        {
            return newIntArray((int[])o);
        }
        else if(Class.forName(NBTLib.getMinecraftPackage() + "NBTBase").isInstance(o))
        {
            return NBT.NBTToTag(o);
        }
        else
        {
            throw new UnknownTagException();
        }
    }
    
    private Tag(NBT type, T data)
    {
        _type = type;
        _data = data;
    }
    
    /**
     * Returns an {@link NBT} representing the type of this {@link Tag}.
     *
     * @return An enum representing the type of this {@link Tag}.
     */
    public NBT getType()
    {
        return _type;
    }
    
    /**
     * Returns the value of this {@link Tag}.
     *
     * @return The value.
     */
    public T get()
    {
        return _data;
    }
}