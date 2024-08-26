package org.harvey.batis;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

/**
 * 狗娘养的
 * 喂养
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-03 00:17
 */
public class DogBitchFeedingThisSon {

    public static final MethodHandles.Lookup DOG_LOOKUP = MethodHandles.lookup();

    static class GrandGrandFather {
        private void thinking() {
            System.out.println(GrandGrandFather.class);
        }
    }

    static class GrandFather extends GrandGrandFather {

        public static final MethodHandles.Lookup GRAND_FATHER_LOOKUP = MethodHandles.lookup();

        private void thinking() {
            System.out.println(GrandFather.class);
        }
    }

    static class Father extends GrandFather {
        public static final MethodHandles.Lookup FATHER_LOOKUP = MethodHandles.lookup();


        private void thinking() {
            System.out.println(Father.class);
        }
    }

    private static final List<MethodHandles.Lookup> LOOKUP_LIST = List.of(GrandFather.GRAND_FATHER_LOOKUP, Father.FATHER_LOOKUP, Son.SON_LOOKUP, DOG_LOOKUP);
    private static final List<Class<? extends GrandFather>> PEOPLE_CLASSES = List.of(GrandFather.class, Father.class, Son.class);
    private static final List<Class<?>> THINKABLE_CLASSES = List.of(GrandFather.class, Father.class, Son.class, DogBitchFeedingThisSon.class);
    private static final List<? extends GrandFather> PEOPLE_OBJECTS = List.of(new GrandFather(), new Father(), new Son());

    static class Son extends Father {

        public static final MethodHandles.Lookup SON_LOOKUP = MethodHandles.lookup();


        public void thinking() {
            System.out.println(Son.class);
        }

        protected void thinking0() {
            for (int i = 0; i < LOOKUP_LIST.size(); i++) {
                MethodHandles.Lookup lookup = LOOKUP_LIST.get(i);
                for (int j = 0; j < PEOPLE_CLASSES.size(); j++) {
                    Class<? extends GrandFather> reflectionFrom = PEOPLE_CLASSES.get(j);
                    for (int k = 0; k < PEOPLE_CLASSES.size(); k++) {
                        Class<? extends GrandFather> specialCaller = PEOPLE_CLASSES.get(k);

                        MethodHandle mh = executeSuccessfully(lookup, reflectionFrom, specialCaller);
                        assertMethodHandler(i, k, mh, j);
                    }
                }
            }
        }

        private static void assertMethodHandler(int i, int k, MethodHandle mh, int j) {
            // 第一条
            if (i != k) {
                assert mh == null;
                return;
            }

            // 第二条
            if (j == k) {
                assert mh != null;
                return;
            }
            if (mh != null) {
                try {
                    mh.invoke(new Son());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                // (j < k): j是k的父类
                System.out.print((j < k) + " ");
                System.out.println("lookup=" + map(i) + ",ref=" + map(j) + ",special=" + map(k));
            } else {
                System.err.println("lookup=" + map(i) + ",ref=" + map(j) + ",special=" + map(k));
            }
        }
    }

    public static String map(int num) {
        switch (num) {
            case 0:
                return "Grand";
            case 1:
                return "Father";
            case 2:
                return "Son";
            case 3:
                return "Dog";
            default:
                throw new IllegalArgumentException();
        }
    }

    private static MethodHandle executeSuccessfully(MethodHandles.Lookup lookup, Class<? extends GrandFather> reflectFrom, Class<? extends GrandFather> specialCaller) {
        try {
            MethodType mt = MethodType.methodType(void.class);
            MethodHandle mh = lookup.findSpecial(reflectFrom, "thinking", mt, specialCaller);
            return mh;
        } catch (IllegalAccessException e) {
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    protected void thinking() {
        System.out.println(DogBitchFeedingThisSon.class);
    }

    public static void main(String[] args) {
        // new DogBitchFeedingThisSon.Son().thinking0();
    }


}
