package org.harvey.batis.demo.utils;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-26 11:43
 */
public class Common {
    private Common(){}
    private static final int HALF_SPACE = 0x20;
    private static final int FULL_SPACE = 0x3000;
    private static final int HALF_TOTAL = 0x7F;
    private static final int HALF_TO_FULL = 0xFEE0;
    public static final int NAME_FORMAT_LEN = 15;

    public static String fullName(String name) {
        char[] fullChar = name.toCharArray();
        for (int i = 0; i < fullChar.length; i++) {
            if (fullChar[i] == HALF_SPACE) {
                fullChar[i] = (char) FULL_SPACE;
                continue;
            }
            if (fullChar[i] < HALF_TOTAL) {
                fullChar[i] = (char) (fullChar[i] + HALF_TO_FULL);
            }
        }
        char space = (char) FULL_SPACE;
        String spaceStr = String.valueOf(space).repeat(Math.max(0, NAME_FORMAT_LEN - name.length()));
        return String.valueOf(fullChar) + spaceStr;
    }
}
