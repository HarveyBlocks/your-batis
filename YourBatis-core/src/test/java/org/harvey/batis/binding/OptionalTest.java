package org.harvey.batis.binding;

import junit.framework.TestCase;
import lombok.Data;
import org.junit.Assert;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Optional, 减少if-else
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-04 13:46
 */
public class OptionalTest extends TestCase {
    @Data
    public static class School {

        private List<Student> students;

        @Data
        public static class Student {
            private Score score;

            @Data
            public static class Score {
                private Math math;

                @Data
                public static class Math {
                    private int value;
                }
            }
        }
    }

    public void test() {
        School originValue = new School();
        testOrElse(Optional.ofNullable(originValue), originValue);
        testOrElse(Optional.ofNullable(null), null);
    }

    private static void testOrElse(Optional<School> schoolOpt, School originValue) {
        School defaultValue = new School();
        School school = schoolOpt.orElse(defaultValue);
        Assert.assertTrue(originValue == null ? school == defaultValue : school == originValue);
    }

    private static void testOrElseGet(Optional<School> schoolOpt, School originValue) {
        School defaultValue = new School();
        Supplier<School> supplier = () -> defaultValue;
        School school = schoolOpt.orElseGet(supplier);
        Assert.assertTrue(originValue == null ? school == defaultValue : school == originValue);
    }
}
