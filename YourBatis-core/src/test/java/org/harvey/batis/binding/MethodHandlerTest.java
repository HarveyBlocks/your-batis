package org.harvey.batis.binding;

import junit.framework.TestCase;
import org.harvey.batis.TestTargetObject;
import org.junit.Assert;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodHandlerTest extends TestCase {
    public static class A extends TestTargetObject {
        /*        @Override
                protected String testProtected(int ignore) {
                    return super.testProtected(ignore);
                }
        */
        @Override
        public String testPublic(int ignore) {
            return super.testPublic(ignore);
        }
    }

    public static class C extends A {
        /*        @Override
                protected String testProtected(int ignore) {
                    return super.testProtected(ignore);
                }
        */
        @Override
        public String testPublic(int ignore) {
            return super.testPublic(ignore);
        }
    }

    private static MethodHandle getMethodHandleJava9(Method method, Class<?> specialCaller)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        // null.privateLookupIn(declaringClass,lookup)
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return lookup.findSpecial(
                declaringClass,
                method.getName(),
                methodType,
                specialCaller);
    }

    public void testComputeIfAbsent() {
        Map<Object, String> map = new HashMap<>();
        map.put(new Object(), "A");
        map.put(new Object(), "B");
        map.put(new Object(), "C");
        map.put(new Object(), "D");
        map.put(new Object(), "E");
        Object key = new Object();
        ;
        String value = new String("你好");
        String s = map.computeIfAbsent(key, k -> {
            Assert.assertSame(key, k);
            return value;
        });
        Assert.assertSame(s, value);
        String s2 = map.computeIfAbsent(key, k -> {
            System.out.println("RUN");
            return value;
        });
    }

    public void testGetMethodHandleJava9() {
        TestTargetObject targetObject = new TestTargetObject();
        Class<? extends TestTargetObject> targetClass = targetObject.getClass();
        String methodName = "testPublic";
        Class[] paramTypes = {int.class};
        try {
            Method method = targetClass.getDeclaredMethod(methodName, paramTypes);
            MethodHandle methodHandle = MethodHandlerTest.getMethodHandleJava9(method, A.class);
            System.out.println(methodHandle);
            // System.out.println(methodHandle.bindTo(targetObject).invoke(12));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void testLookup() {
        // 凡是调用类支持的字节码操作，lookup都支持。
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        TestTargetObject targetObject = new TestTargetObject();
        Class<? extends TestTargetObject> targetClass = targetObject.getClass();
        String methodName = "testPublic";
        Class[] paramTypes = {int.class};
        Class<String> retrunType = String.class;
        MethodType objectMethodType = MethodType.methodType(retrunType, paramTypes);
        try {
            MethodHandle protectedMethod = lookup.findVirtual(targetClass, methodName, objectMethodType);
            System.out.println("By findVirtual : " + protectedMethod);
            try {
                System.out.println(protectedMethod.bindTo(new A()).invoke(12));
                ;
            } catch (Throwable e) {
                e.printStackTrace(System.err);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            System.err.println(e.getMessage());
            try {
                Method method = targetClass.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                // 使用本方法
                MethodHandle protectedMethod = lookup.unreflect(method);
                System.out.println("By unreflect : " + protectedMethod);
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void testClassicReflect() {
        Class<TestTargetObject> targetClass = TestTargetObject.class;
        try {
            Constructor<TestTargetObject> constructor = targetClass.getDeclaredConstructor(String.class, int.class);
            System.out.println(constructor);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void testPublicLookup() {
        // 只能访问public成员
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
        try {
            Class<?> aClass = publicLookup.findClass("java.lang.Class");
            System.out.println(aClass);
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void testMethodType() {
        Class<?> returnType = String.class;
        Class<?> paramType = int.class;
        // someMethod(int)->String
        MethodType methodType = MethodType.methodType(returnType, paramType);
    }
}

