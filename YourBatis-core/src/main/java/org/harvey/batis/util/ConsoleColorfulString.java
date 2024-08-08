package org.harvey.batis.util;


/**
 * 控制台, 通过给字上前后缀以改变颜色
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-06 14:27
 */
public class ConsoleColorfulString {
    private static final String COLOR_POST = "\033[0m";

    private static final String COLOR_PRE = "\033[";

    public enum ConsoleColor {
        BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, SKY, GRAY, DEFAULT
    }

    private static String colorPre(ConsoleColor color, ConsoleColor background) {
        StringBuilder result = new StringBuilder(COLOR_PRE);
        if (background != ConsoleColor.BLACK) {
            result.append(background.ordinal() + 40).append(";");
        }
        if (color == ConsoleColor.DEFAULT) {
            color = ConsoleColor.BLACK;
        }
        return result.append(color.ordinal() + 30).append("m").toString();
    }


    /**
     * 着色
     */
    public static String painting(String str, ConsoleColor color) {
        return painting(str, color, ConsoleColor.DEFAULT);
    }

    /**
     * 着色
     */
    public static String painting(String str, ConsoleColor color, ConsoleColor background) {
        return colorPre(color, background) + str + COLOR_POST;
    }

}
