// Bukkit Plugin "NBTLib" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by/3.0/

package net.drgnome.nbtlib;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * <p>A util which allows you to create class proxies.</p>
 * <p>This means if you specify a class A, then a class B which extends A will be created and, depending on the given {@link MethodFilter}, some or all non-static non-final methods which are public or protected will be overridden and dispatched to a provided {@link InvocationHandler}.</p>
 * <b>Examples:</b>
 * 
 * @since 0.3
 */
public class ClassProxy
{
    private static final HashMap<ProxyDescriptor, Class> _map = new HashMap<ProxyDescriptor, Class>();
    private static final HashMap<Class, Method[]> _methodTables = new HashMap<Class, Method[]>();
    private static final Map<Class, Method> _toObjectMap;
    private static final Map<Class, Method> _fromObjectMap;
    private static final String _namePrefix = ClassProxy.class.getPackage().getName() + ".Proxy";
    private static final String _magicName = "*";
    private static int _count = 0;
    
    static
    {
        HashMap<Class, Method> toMap = new HashMap<Class, Method>();
        HashMap<Class, Method> fromMap = new HashMap<Class, Method>();
        try
        {
            toMap.put(Boolean.TYPE, Boolean.class.getDeclaredMethod("valueOf", boolean.class));
            toMap.put(Byte.TYPE, Byte.class.getDeclaredMethod("valueOf", byte.class));
            toMap.put(Character.TYPE, Character.class.getDeclaredMethod("valueOf", char.class));
            toMap.put(Short.TYPE, Short.class.getDeclaredMethod("valueOf", short.class));
            toMap.put(Integer.TYPE, Integer.class.getDeclaredMethod("valueOf", int.class));
            toMap.put(Long.TYPE, Long.class.getDeclaredMethod("valueOf", long.class));
            toMap.put(Float.TYPE, Float.class.getDeclaredMethod("valueOf", float.class));
            toMap.put(Double.TYPE, Double.class.getDeclaredMethod("valueOf", double.class));
            fromMap.put(Boolean.TYPE, Boolean.class.getDeclaredMethod("booleanValue"));
            fromMap.put(Byte.TYPE, Byte.class.getDeclaredMethod("byteValue"));
            fromMap.put(Character.TYPE, Character.class.getDeclaredMethod("charValue"));
            fromMap.put(Short.TYPE, Short.class.getDeclaredMethod("shortValue"));
            fromMap.put(Integer.TYPE, Integer.class.getDeclaredMethod("intValue"));
            fromMap.put(Long.TYPE, Long.class.getDeclaredMethod("longValue"));
            fromMap.put(Float.TYPE, Float.class.getDeclaredMethod("floatValue"));
            fromMap.put(Double.TYPE, Double.class.getDeclaredMethod("doubleValue"));
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        _toObjectMap = Collections.unmodifiableMap(toMap);
        _fromObjectMap = Collections.unmodifiableMap(fromMap);
    }
    
    private ClassProxy()
    {
    }
    
    /**
     * <p>Calls the method that has been overridden by a proxy method.</p>
     *
     * @param proxy         The proxy instance.
     * @param method        The method you want to call.
     * @param args          The argument to pass to the method.
     *
     * @return Whatever the super method returns.
     *
     * @throws NoSuchMethodException    If the super method cannot be found.
     * @throws SecurityException        If the super method cannot be accessed.
     * @throws Throwable                If the super method throws anything.
     */
    public static Object callSuper(Object proxy, Method method, Object... args) throws Throwable
    {
        try
        {
            Method m = proxy.getClass().getDeclaredMethod(_magicName, int.class, Object[].class);
            m.setAccessible(true);
            return m.invoke(proxy, numMethod(proxy.getClass(), method), args);
        }
        catch(InvocationTargetException e)
        {
            Throwable t = e.getCause();
            throw (t == null) ? e : t;
        }
    }
    
    /**
     * <p>Creates a proxy class for a given super class and instantiates it.</p>
     * <p>This basically just calls {@code newInstance(superClass, null, handler, paramTypes, params)}.</p>
     *
     * @param superClass    The class on which you wish to create a proxy.
     * @param handler       The {@link InvocationHandler} to which public and protected method calls will be redirected.
     * @param paramTypes    An array of {@link Class Classes} to identify the constructor of the super class you wish to invoke.
     * @param params        An array or arguments to pass to the constructor of the super class.
     *
     * @return An new proxy instance.
     *
     * @throws IllegalArgumentException If the proxy class has not yet been created: See {@link #getClass(Class, boolean) getClass()}.
     */
    public static <T> T newInstance(Class<T> superClass, InvocationHandler handler, Class[] paramTypes, Object... params) throws IllegalArgumentException
    {
        return newInstance(superClass, null, handler, paramTypes, params);
    }
    
    /**
     * <p>Creates a proxy class for a given super class and instantiates it.</p>
     *
     * @param superClass    The class on which you wish to create a proxy.
     * @param filter        The {@link MethodFilter} to use on {@code superClass}.
     * @param handler       The {@link InvocationHandler} to which public and protected method calls will be redirected.
     * @param paramTypes    An array of {@link Class Classes} to identify the constructor of the super class you wish to invoke.
     * @param params        An array or arguments to pass to the constructor of the super class.
     *
     * @return An new proxy instance.
     *
     * @throws IllegalArgumentException If the proxy class has not yet been created: See {@link #getClass(Class, boolean) getClass()}.
     */
    public static <T> T newInstance(Class<T> superClass, MethodFilter filter, InvocationHandler handler, Class[] paramTypes, Object... params) throws IllegalArgumentException
    {
        try
        {
            Class[] types0 = new Class[paramTypes.length + 1];
            types0[0] = InvocationHandler.class;
            for(int i = 0; i < paramTypes.length; i++)
            {
                types0[i + 1] = paramTypes[i];
            }
            Object[] params0 = new Object[params.length + 1];
            params0[0] = handler;
            for(int i = 0; i < params.length; i++)
            {
                params0[i + 1] = params[i];
            }
            Constructor c = getClass(superClass, filter, true).getDeclaredConstructor(types0);
            c.setAccessible(true);
            return (T)c.newInstance(params0);
        }
        catch(IllegalArgumentException e)
        {
            throw e;
        }
        catch(Throwable t)
        {
            throw new IllegalArgumentException(t);
        }
    }
    
    /**
     * <p>Creates a proxy class for a given super class.</p>
     * <p>Creates a new {@link Class} which extends a given super class which redirects all calls to public and protected methods which are non-final and non-static to a given {@link InvocationHandler}.<br />
     * Abstract methods will be defined and redirected to the InvocationHandler as well.<br />
     * For every public and protected constructor the super class has, this class will have the same constructor with an InvocationHandler as additional first parameter.</p>
     *
     * @param superClass        The class on which you wish to create a proxy. Has to be public and not final, primitive, an array or an interface.
     * @param createIfNotExists Whether or not to create a proxy class if none exists.
     *
     * @return The new class.
     *
     * @throws IllegalArgumentException If {@code createIfNotExists} is {@code true}, the class does not exist yet and no valid bytecode can be generated.
     */
    public static Class getClass(Class superClass, boolean createIfNotExists) throws IllegalArgumentException
    {
        return getClass(superClass, null, createIfNotExists);
    }
    
    /**
     * <p>Creates a proxy class for a given super class.</p>
     * <p>Creates a new {@link Class} which extends a given super class which redirects all calls to public and protected methods which are non-final and non-static, on which {@code filter.filterMethod()} returns {@code true}, to a given {@link InvocationHandler}.<br />
     * Abstract methods will be defined and redirected to the InvocationHandler as well.<br />
     * For every public and protected constructor the super class has, this class will have the same constructor with an InvocationHandler as additional first parameter.</p>
     *
     * @param superClass        The class on which you wish to create a proxy. Has to be public and not final, primitive, an array or an interface.
     * @param filter            The {@link MethodFilter} to use on {@code superClass}.
     * @param createIfNotExists Whether or not to create a proxy class if none exists.
     *
     * @return The new class.
     *
     * @throws IllegalArgumentException If {@code createIfNotExists} is {@code true}, the class does not exist yet and no valid bytecode can be generated.
     */
    public static synchronized Class getClass(Class superClass, MethodFilter filter, boolean createIfNotExists) throws IllegalArgumentException
    {
        if(superClass.isInterface() || superClass.isAnnotation())
        {
            throw new IllegalArgumentException("No interfaces allowed!");
        }
        if(superClass.isArray())
        {
            throw new IllegalArgumentException("No arrays allowed!");
        }
        if(superClass.isPrimitive())
        {
            throw new IllegalArgumentException("No primitive types allowed!");
        }
        if((superClass.getModifiers() & Modifier.PUBLIC) != Modifier.PUBLIC)
        {
            throw new IllegalArgumentException("superClass not public!");
        }
        Method[] mthd = getMethods(superClass, filter);
        ProxyDescriptor desc = new ProxyDescriptor(superClass, mthd);
        if(_map.containsKey(desc))
        {
            return _map.get(desc);
        }
        if(!createIfNotExists)
        {
            return null;
        }
        try
        {
            Class clazz = defineClass(superClass, mthd);
            _map.put(desc, clazz);
            _methodTables.put(clazz, mthd);
            return clazz;
        }
        catch(Throwable t)
        {
            throw new IllegalArgumentException(t);
        }
    }
    
    private static int numMethod(Class clazz, Method m) throws IllegalArgumentException
    {
        try
        {
            Method[] ms = _methodTables.get(clazz);
            for(int i = 0; i < ms.length; i++)
            {
                if(ms[i] == m)
                {
                    return i;
                }
            }
        }
        catch(Throwable t)
        {
            IllegalArgumentException e = new IllegalArgumentException("Method " + m.getName() + " not found!");
            e.initCause(t);
            throw e;
        }
        throw new IllegalArgumentException("Method " + m.getName() + " not found!");
    }
    
    private static synchronized Class defineClass(Class superClass, Method[] mthd) throws ClassFormatError, IllegalAccessException, NoSuchMethodException, VerifyError
    {
        byte[] b = compileClass(superClass, mthd);
        /*try
        {
            new FileOutputStream(new File("Proxy1.class")).write(b);
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }*/
        Method m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
        m.setAccessible(true);
        try
        {
            return (Class)m.invoke(ClassProxy.class.getClassLoader(), _namePrefix + _count, b, 0, b.length);
        }
        catch(InvocationTargetException e)
        {
            Throwable t = e.getCause();
            if(t instanceof ClassFormatError)
            {
                throw (ClassFormatError)t;
            }
            if(t instanceof VerifyError)
            {
                throw (VerifyError)t;
            }
            ClassFormatError c = new ClassFormatError();
            c.initCause(t);
            throw c;
        }
    }
    
    private static synchronized byte[] compileClass(Class superClass, Method[] mthd) throws ClassFormatError
    {
        _count++;
        BBAOS stream = new BBAOS();
        BBAOS method = new BBAOS();
        BBAOS tmp = new BBAOS();
        BBAOS magicCode = new BBAOS();
        BBAOS magicPart = new BBAOS();
        int magicStack = 2;
        stream.write(0xca, 0xfe, 0xba, 0xbe, 0, 0, 0, 0x32);
        PoolList pool = new PoolList();
        ArrayList<byte[]> methods = new ArrayList<byte[]>();
        pool.add(new byte[0]); // To adjust the pool index
        pool.add(b(7, 0, 3)); // 1: this class
        pool.add(b(7, 0, 4)); // 2: super class
        pool._classMap.put(superClass, 2);
        pool.addStringFix(getName(_namePrefix + _count)); // 3
        pool.addStringFix(getName(superClass)); // 4
        pool.addStringFix("Synthetic"); // 5
        pool.addStringFix("Code"); // 6
        pool.add(b(9, 0, 1, 0, 9)); // 7: Field
        pool.add(b(9, 0, 1, 0, 10)); // 8: Field
        pool.add(b(12, 0, 11, 0, 13)); // 9: NameAndType
        pool.add(b(12, 0, 12, 0, 14)); // 10: NameAndType
        pool.addStringFix("_table"); // 11
        pool.addStringFix("_handler"); // 12
        pool.addStringFix(getType(Method[].class)); // 13
        pool.addStringFix(getType(InvocationHandler.class)); // 14
        pool.addStringFix("Exceptions"); // 15
        // static {}
        method.write(writeInt(Modifier.STATIC, 2));
        method.write(pool.getStringIndex("<clinit>", 2));
        method.write(pool.getStringIndex("()V", 2));
        //          | meta     | length     | something | code_len   | getClassName, invokestatic
        method.write(0, 2, 0, 6, 0, 0, 0, 22, 0, 1, 0, 0, 0, 0, 0, 10, 0x13, 0, 1, 0xb8);
        // Method to invoke
        try
        {
            method.write(pool.getMethodIndex(ClassProxy.class.getDeclaredMethod("getMethodTable", Class.class), 2));
        }
        catch(NoSuchMethodException e)
        {
            AssertionError error = new AssertionError("ClassProxy does not have the correct method \"getMethodTable\"!");
            error.initCause(e);
            throw error;
        }
        //          | putstatic, ret | ...       | synthetic
        method.write(0xb3, 0, 7, 0xb1, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0);
        methods.add(method.toByteArray());
        for(Constructor cstr : getConstructors(superClass))
        {
            tmp.write(12); // N&T
            tmp.write(pool.getStringIndex("<init>", 2));
            tmp.write(pool.getStringIndex(getConstructorSignature(cstr), 2));
            pool.add(tmp.toByteArray());
            tmp.write(10, 0, 2); // Mthd
            tmp.write(pool.lastIndex(2));
            pool.add(tmp.toByteArray());
            Class[] param = cstr.getParameterTypes();
            tmp.write(0x2a);
            for(int i = 0; i < param.length; i++)
            {
                int pos = i + 2;
                int operator;
                if(pos <= 3)
                {
                    if(param[i].equals(Boolean.TYPE) || param[i].equals(Byte.TYPE) || param[i].equals(Character.TYPE) || param[i].equals(Short.TYPE) || param[i].equals(Integer.TYPE))
                    {
                        operator = 0x1a;
                    }
                    else if(param[i].equals(Long.TYPE))
                    {
                        operator = 0x1e;
                    }
                    else if(param[i].equals(Float.TYPE))
                    {
                        operator = 0x22;
                    }
                    else if(param[i].equals(Double.TYPE))
                    {
                        operator = 0x26;
                    }
                    else
                    {
                        operator = 0x2a;
                    }
                    tmp.write(operator + pos);
                }
                else
                {
                    if(param[i].equals(Boolean.TYPE) || param[i].equals(Byte.TYPE) || param[i].equals(Character.TYPE) || param[i].equals(Short.TYPE) || param[i].equals(Integer.TYPE))
                    {
                        operator = 0x15;
                    }
                    else if(param[i].equals(Long.TYPE))
                    {
                        operator = 0x16;
                    }
                    else if(param[i].equals(Float.TYPE))
                    {
                        operator = 0x17;
                    }
                    else if(param[i].equals(Double.TYPE))
                    {
                        operator = 0x18;
                    }
                    else
                    {
                        operator = 0x19;
                    }
                    tmp.write(operator, pos);
                }
                if(param[i].equals(Long.TYPE) || param[i].equals(Double.TYPE))
                {
                    i++;
                }
            }
            tmp.write(0xb7);
            tmp.write(pool.lastIndex(2));
            // put handler
            tmp.write(0x2a, 0x2b, 0xb5, 0, 8, 0xb1);
            byte[] actualCode = tmp.toByteArray();
            // Now put it together
            method.write(writeInt(cstr.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.SYNCHRONIZED | Modifier.STRICT), 2));
            method.write(pool.getStringIndex("<init>", 2));
            method.write(pool.getStringIndex(getNewConstructorSignature(cstr), 2));
            int len = 1; // + 1st arg
            for(Class par : param)
            {
                len += (par.equals(Long.TYPE) || par.equals(Double.TYPE)) ? 2 : 1;
            }
            //          |num | name
            method.write(0, 2, 0, 6);
            method.write(writeInt(actualCode.length + 12, 4));
            method.write(writeInt(len, 2));
            method.write(writeInt(len + 1, 2)); // + this
            method.write(writeInt(actualCode.length, 4));
            method.write(actualCode);
            //                     | synthetic
            method.write(0, 0, 0, 0, 0, 5, 0, 0, 0, 0);
            methods.add(method.toByteArray());
        }
        byte[] methodRedirect;
        try
        {
            methodRedirect = pool.getMethodIndex(ClassProxy.class.getDeclaredMethod("redirect", Object.class, InvocationHandler.class, Method.class, Object[].class), 2);
        }
        catch(NoSuchMethodException e)
        {
            AssertionError error = new AssertionError("ClassProxy does not have the correct method \"redirect\"!");
            error.initCause(e);
            throw error;
        }
        for(int methodNum = 0; methodNum < mthd.length; methodNum++)
        {
            boolean doMagic = ((mthd[methodNum].getModifiers() & Modifier.ABSTRACT) == 0);
            if(doMagic)
            {
                magicPart.write(0x2a); // aload_0
            }
            Class[] param = mthd[methodNum].getParameterTypes();
            tmp.write(0x2a, 0x2a, 0xb4, 0, 8, 0xb2, 0, 7);
            tmp.write(smartInt(methodNum));
            tmp.write(0x32);
            tmp.write(smartInt(param.length));
            tmp.write(0xbd);
            tmp.write(pool.getClassIndex(Object.class, 2));
            int pos = 1;
            for(int i = 0; i < param.length; i++)
            {
                tmp.write(0x59); // Dup
                tmp.write(smartInt(i)); // Index
                int operator;
                if(pos <= 3)
                {
                    if(param[i].equals(Boolean.TYPE) || param[i].equals(Byte.TYPE) || param[i].equals(Character.TYPE) || param[i].equals(Short.TYPE) || param[i].equals(Integer.TYPE))
                    {
                        operator = 0x1a;
                    }
                    else if(param[i].equals(Long.TYPE))
                    {
                        operator = 0x1e;
                    }
                    else if(param[i].equals(Float.TYPE))
                    {
                        operator = 0x22;
                    }
                    else if(param[i].equals(Double.TYPE))
                    {
                        operator = 0x26;
                    }
                    else
                    {
                        operator = 0x2a;
                    }
                    tmp.write(operator + pos);
                }
                else
                {
                    if(param[i].equals(Boolean.TYPE) || param[i].equals(Byte.TYPE) || param[i].equals(Character.TYPE) || param[i].equals(Short.TYPE) || param[i].equals(Integer.TYPE))
                    {
                        operator = 0x15;
                    }
                    else if(param[i].equals(Long.TYPE))
                    {
                        operator = 0x16;
                    }
                    else if(param[i].equals(Float.TYPE))
                    {
                        operator = 0x17;
                    }
                    else if(param[i].equals(Double.TYPE))
                    {
                        operator = 0x18;
                    }
                    else
                    {
                        operator = 0x19;
                    }
                    tmp.write(operator, pos);
                }
                convertToObject(pool, param[i], tmp);
                tmp.write(0x53); // aastore
                pos += (param[i].equals(Long.TYPE) || param[i].equals(Double.TYPE)) ? 2 : 1;
                if(doMagic)
                {
                    // Prepare magic
                    magicPart.write(0x2c); // aload_2
                    magicPart.write(smartInt(i));
                    magicPart.write(0x32); // aaload
                    convertFromObject(pool, param[i], magicPart);
                }
            }
            tmp.write(0xb8);
            tmp.write(methodRedirect);
            Class rType = mthd[methodNum].getReturnType();
            convertFromObject(pool, rType, tmp);
            tmp.write(getReturn(rType));
            byte[] actualCode = tmp.toByteArray();
            method.write(writeInt(mthd[methodNum].getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.SYNCHRONIZED | Modifier.STRICT), 2));
            method.write(pool.getStringIndex(mthd[methodNum].getName(), 2));
            method.write(pool.getStringIndex(getMethodSignature(mthd[methodNum]), 2));
            method.write(0, 2, 0, 6);
            method.write(writeInt(actualCode.length + 12, 4));
            boolean hasLong = false;
            int len = 1; // + "this"
            for(Class par : param)
            {
                if((par.equals(Long.TYPE) || par.equals(Double.TYPE)))
                {
                    len += 2;
                    hasLong = true;
                }
                else
                {
                    len++;
                }
            }
            method.write(writeInt((param.length > 0) ? (hasLong ? 8 : 7) : 4, 2));
            method.write(writeInt(len, 2));
            method.write(writeInt(actualCode.length, 4));
            method.write(actualCode);
            method.write(0, 0, 0, 0, 0, 5, 0, 0, 0, 0);
            methods.add(method.toByteArray());
            if(doMagic)
            {
                // And now a little magic
                if(len >= magicStack)
                {
                    magicStack = len + 1;
                }
                magicPart.write(0xb7); // invokespecial
                magicPart.write(pool.getMethodIndex(mthd[methodNum], 2));
                convertToObject(pool, rType, magicPart);
                magicPart.write(0xb0); // areturn
                // Next
                magicCode.write(0x1b); // iload_1
                if(methodNum == 0)
                {
                    magicCode.write(0x9a);
                }
                else
                {
                    magicCode.write(smartInt(methodNum));
                    magicCode.write(0xa0);
                }
                magicCode.write(writeInt(magicPart.size() + 3, 2));
                magicCode.write(magicPart.toByteArray());
            }
        }
        // Maaagic :D
        magicCode.write(0xbb); // new
        magicCode.write(pool.getClassIndex(NoSuchMethodException.class, 2));
        magicCode.write(0x59, 0xb7); // dup, invokespecial
        tmp.write(12); // N&T
        tmp.write(pool.getStringIndex("<init>", 2));
        tmp.write(pool.getStringIndex("()V", 2));
        pool.add(tmp.toByteArray());
        tmp.write(10); // Method
        tmp.write(pool.getClassIndex(NoSuchMethodException.class, 2));
        tmp.write(pool.lastIndex(2));
        pool.add(tmp.toByteArray());
        magicCode.write(pool.lastIndex(2));
        magicCode.write(0xbf); // athrow
        byte[] magic = magicCode.toByteArray();
        method.write(writeInt(Modifier.PRIVATE, 2));
        method.write(pool.getStringIndex("*", 2));
        method.write(pool.getStringIndex("(I[Ljava/lang/Object;)Ljava/lang/Object;", 2));
        method.write(0, 3, 0, 6); // Code
        method.write(writeInt(magic.length + 12, 4));
        method.write(writeInt(magicStack, 2));
        method.write(writeInt(3, 2)); // numargs
        method.write(writeInt(magic.length, 4));
        method.write(magic);
        method.write(0, 0, 0, 0); // Something we have nothing of
        //          | Synthetic      | throws Throwable
        method.write(0, 5, 0, 0, 0, 0, 0, 15, 0, 0, 0, 4, 0, 1);
        method.write(pool.getClassIndex(Throwable.class, 2));
        methods.add(method.toByteArray());
        // End magic
        stream.write(writeInt(pool.size(), 2));
        for(byte[] tag : pool)
        {
            stream.write(tag);
        }
        stream.write(writeInt(Modifier.PUBLIC | Modifier.FINAL | 32, 2)); // 32 = ACC_SUPER
        stream.write(writeInt(1, 2)); // This class pos
        stream.write(writeInt(2, 2)); // Super class pos
        stream.write(writeInt(0, 2)); // No interfaces
        stream.write(writeInt(2, 2)); // Always 2 fields
        stream.write(0, 0x1a, 0, 11, 0, 13, 0, 1, 0, 5, 0, 0, 0, 0); // private static final java.lang.reflect.Method[] _table;
        stream.write(0, 0x12, 0, 12, 0, 14, 0, 1, 0, 5, 0, 0, 0, 0); // private final java.lang.reflect.InvocationHandler _handler;
        stream.write(writeInt(methods.size(), 2));
        for(byte[] tag : methods)
        {
            stream.write(tag);
        }
        stream.write(0, 1, 0, 5, 0, 0, 0, 0); // Synthetic
        return stream.toByteArray();
    }
    
    private static void convertToObject(PoolList pool, Class type, BBAOS stream)
    {
        if(type.equals(Void.TYPE))
        {
            stream.write(0x1); // aconst_null
        }
        else if(_toObjectMap.containsKey(type))
        {
            stream.write(0xb8); // Invokestatic
            stream.write(pool.getMethodIndex(_toObjectMap.get(type), 2));
        }
    }
    
    private static void convertFromObject(PoolList pool, Class type, BBAOS stream)
    {
        if(type.equals(Void.TYPE))
        {
            stream.write(0x57); // pop
        }
        else if(_fromObjectMap.containsKey(type))
        {
            Method method = _fromObjectMap.get(type);
            stream.write(0xc0);
            stream.write(pool.getClassIndex(method.getDeclaringClass(), 2));
            stream.write(0xb6); // Invokevirtual
            stream.write(pool.getMethodIndex(method, 2));
        }
        else if(!type.equals(Object.class))
        {
            stream.write(0xc0);
            stream.write(pool.getClassIndex(type, 2));
        }
    }
    
    private static int getReturn(Class type)
    {
        if(type.equals(Void.TYPE)) return 0xb1;
        if(type.equals(Boolean.TYPE) || type.equals(Byte.TYPE) || type.equals(Character.TYPE) || type.equals(Short.TYPE) || type.equals(Integer.TYPE)) return 0xac;
        if(type.equals(Long.TYPE)) return 0xad;
        if(type.equals(Float.TYPE)) return 0xae;
        if(type.equals(Double.TYPE)) return 0xaf;
        return 0xb0;
    }
    
    private static byte[] b(int... array)
    {
        byte[] b = new byte[array.length];
        for(int i = 0; i < array.length; i++)
        {
            b[i] = (byte)array[i];
        }
        return b;
    }
    
    private static byte[] writeInt(int i, int d)
    {
        byte[] b = new byte[d];
        for(int c = 0; c < d; c++)
        {
            b[c] = (byte)(i >> ((d - c - 1) * 8));
        }
        return b;
    }
    
    private static byte[] smartInt(int i)
    {
        if(i <= 5)
        {
            return new byte[]{(byte)(i + 3)};
        }
        return (i < 256) ? new byte[]{0x10, (byte)i} : new byte[]{0x11, (byte)((i << 8) & 0xff), (byte)(i & 0xff)};
    }
    
    private static byte[] unicode(String string)
    {
        BBAOS stream = new BBAOS();
        for(char c : string.toCharArray())
        {
            if((c >= '\u0001') && (c <= '\u007f'))
            {
                stream.write(c & 0x7f);
            }
            else if((c == '\u0000') || ((c >= '\u0080') && (c <= '\u07ff')))
            {
                stream.write(((c & 0x7c0) >> 6) | 0xc0, (c & 0x3f) | 0x80);
            }
            else if((c >= '\u0800') && (c <= '\uffff'))
            {
                stream.write(((c & 0xf000) >> 12) | 0xe0, ((c & 0xfc0) >> 6) | 0x80, (c & 0x3f) | 0x80);
            }
        }
        byte[] str = stream.toByteArray();
        stream.write(1);
        stream.write(writeInt(str.length, 2));
        stream.write(str);
        return stream.toByteArray();
    }
    
    private static String getName(Class clazz)
    {
        return getName(clazz.getName());
    }
    
    private static String getName(String clazz)
    {
        return clazz.replace(".", "/");
    }
    
    private static String getType(Class clazz)
    {
        String name = "";
        while(clazz.isArray())
        {
            name += "[";
            clazz = clazz.getComponentType();
        }
        if(clazz.equals(Boolean.TYPE))
        {
            name += "Z";
        }
        else if(clazz.equals(Byte.TYPE))
        {
            name += "B";
        }
        else if(clazz.equals(Character.TYPE))
        {
            name += "C";
        }
        else if(clazz.equals(Short.TYPE))
        {
            name += "S";
        }
        else if(clazz.equals(Integer.TYPE))
        {
            name += "I";
        }
        else if(clazz.equals(Long.TYPE))
        {
            name += "J";
        }
        else if(clazz.equals(Float.TYPE))
        {
            name += "F";
        }
        else if(clazz.equals(Double.TYPE))
        {
            name += "D";
        }
        else if(clazz.equals(Void.TYPE))
        {
            name += "V";
        }
        else
        {
            name += "L" + getName(clazz) + ";";
        }
        return name;
    }
    
    private static Constructor[] getConstructors(Class clazz)
    {
        ArrayList<Constructor> list = new ArrayList<Constructor>();
        for(Constructor c : clazz.getDeclaredConstructors())
        {
            int mf = c.getModifiers();
            if(((mf & Modifier.PUBLIC) == Modifier.PUBLIC) || ((mf & Modifier.PROTECTED) == Modifier.PROTECTED))
            {
                list.add(c);
            }
        }
        return list.toArray(new Constructor[0]);
    }
    
    private static Method[] getMethods(Class original, MethodFilter filter)
    {
        Class clazz = original;
        ArrayList<Method> list = new ArrayList<Method>();
        while(clazz != null)
        {
            i_haz_a_loop:
            for(Method m : clazz.getDeclaredMethods())
            {
                int mf = m.getModifiers();
                if(((mf & Modifier.FINAL) == Modifier.FINAL) || ((mf & Modifier.STATIC) == Modifier.STATIC) || (((mf & Modifier.PUBLIC) == 0) && ((mf & Modifier.PROTECTED) == 0)))
                {
                    continue;
                }
                for(Method check : list)
                {
                    if(m.getName().equals(check.getName()) && m.getReturnType().equals(check.getReturnType()) && Arrays.equals(m.getParameterTypes(), check.getParameterTypes()))
                    {
                        continue i_haz_a_loop;
                    }
                }
                if((filter == null) || filter.filterMethod(m) || ((mf & Modifier.ABSTRACT) == Modifier.ABSTRACT))
                {
                    list.add(m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        Method[] array = list.toArray(new Method[0]);
        return array;
    }
    
    private static String getMethodSignature(Method m)
    {
        String sig = "(";
        for(Class clazz : m.getParameterTypes())
        {
            sig += getType(clazz);
        }
        return sig + ")" + getType(m.getReturnType());
    }
    
    private static String getNewConstructorSignature(Constructor c)
    {
        String sig = "(" + getType(InvocationHandler.class);
        for(Class clazz : c.getParameterTypes())
        {
            sig += getType(clazz);
        }
        return sig + ")V";
    }
    
    private static String getConstructorSignature(Constructor c)
    {
        String sig = "(";
        for(Class clazz : c.getParameterTypes())
        {
            sig += getType(clazz);
        }
        return sig + ")V";
    }
    
    static Method[] getMethodTable(Class proxy)
    {
        return _methodTables.containsKey(proxy) ? _methodTables.get(proxy) : new Method[0];
    }
    
    static Object redirect(Object proxy, InvocationHandler handler, Method m, Object... args) throws Throwable
    {
        try
        {
            return handler.invoke(proxy, m, args);
        }
        catch(Throwable t)
        {
            if(t == null)
            {
                throw new Exception("An exception was here, but apparently it decided to go away.");
            }
            if((t instanceof RuntimeException) || (t instanceof Error))
            {
                throw t;
            }
            for(Class c : m.getExceptionTypes())
            {
                if(c.isInstance(t))
                {
                    throw t;
                }
            }
            throw new UndeclaredThrowableException(t);
        }
    }
    
    // BetterByteArrayOutputStream
    private static class BBAOS extends ByteArrayOutputStream
    {
        public void write(int... array)
        {
            for(int i : array)
            {
                write(i);
            }
        }
        
        // Go away with your exception! o.0
        public void write(byte[] b)
        {
            write(b, 0, b.length);
        }
        
        // Yes, I'm lazy. Officially.
        public byte[] toByteArray()
        {
            byte[] data = super.toByteArray();
            reset();
            return data;
        }
    }
    
    private static class PoolList extends ArrayList<byte[]>
    {
        private final HashMap<String, Integer> _map = new HashMap<String, Integer>();
        final HashMap<Class, Integer> _classMap = new HashMap<Class, Integer>();
        private final HashMap<Method, Integer> _methodMap = new HashMap<Method, Integer>();
        
        public void addStringFix(String str)
        {
            if(!_map.containsKey(str))
            {
                _map.put(str, size());
            }
            add(ClassProxy.unicode(str));
        }
        
        public void addString(String str)
        {
            if(!_map.containsKey(str))
            {
                _map.put(str, size());
                add(ClassProxy.unicode(str));
            }
        }
        
        public byte[] getStringIndex(String str, int bytes)
        {
            addString(str);
            return ClassProxy.writeInt(_map.get(str), bytes);
        }
        
        public byte[] getClassIndex(Class clazz, int bytes)
        {
            if(!_classMap.containsKey(clazz))
            {
                byte[] cl = new byte[3];
                cl[0] = (byte)7;
                byte[] name = getStringIndex(ClassProxy.getName(clazz), 2);
                cl[1] = name[0];
                cl[2] = name[1];
                _classMap.put(clazz, size());
                add(cl);
            }
            return ClassProxy.writeInt(_classMap.get(clazz), bytes);
        }
        
        public byte[] getMethodIndex(Method m, int bytes)
        {
            if(!_methodMap.containsKey(m))
            {
                BBAOS tmp = new BBAOS();
                tmp.write(12); // NameAndType
                tmp.write(getStringIndex(m.getName(), 2));
                tmp.write(getStringIndex(ClassProxy.getMethodSignature(m), 2));
                add(tmp.toByteArray());
                byte[] last = lastIndex(2);
                tmp.write(10); // Method
                tmp.write(getClassIndex(m.getDeclaringClass(), 2));
                tmp.write(last);
                _methodMap.put(m, size());
                add(tmp.toByteArray());
            }
            return ClassProxy.writeInt(_methodMap.get(m), bytes);
        }
        
        public byte[] lastIndex(int bytes)
        {
            return ClassProxy.writeInt(size() - 1, bytes);
        }
    }
    
    private static class ProxyDescriptor
    {
        private final int _hash;
        
        public ProxyDescriptor(Class clazz, Method[] methods)
        {
            int hash = clazz.hashCode();
            for(Method m : methods)
            {
                hash ^= m.hashCode();
            }
            _hash = hash;
        }
        
        public int hashCode()
        {
            return _hash;
        }
        
        public boolean equals(Object o)
        {
            return (o instanceof ProxyDescriptor) && (o.hashCode() == hashCode());
        }
    }
}