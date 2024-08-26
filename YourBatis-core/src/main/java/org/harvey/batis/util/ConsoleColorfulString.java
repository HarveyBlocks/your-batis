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

    public enum Color {
        BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, SKY, GRAY, DEFAULT
    }

    private static String colorPre(Color color, Color background) {
        StringBuilder result = new StringBuilder(COLOR_PRE);
        if (background != Color.DEFAULT) {
            result.append(background.ordinal() + 40).append(";");
        }
        if (color == Color.DEFAULT) {
            color = Color.BLACK;
        }
        return result.append(color.ordinal() + 30).append("m").toString();
    }


    /**
     * 着色
     */
    public static String painting(String str, Color color) {
        return painting(str, color, Color.DEFAULT);
    }

    /**
     * 着色
     */
    public static String painting(String str, Color color, Color background) {
        return colorPre(color, background) + str + COLOR_POST;
    }

}
