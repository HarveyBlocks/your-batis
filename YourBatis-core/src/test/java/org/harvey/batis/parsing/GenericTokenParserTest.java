package org.harvey.batis.parsing;

import junit.framework.TestCase;

import java.util.Locale;

public class GenericTokenParserTest extends TestCase {

    public void testParse() {
        GenericTokenParser parser = new GenericTokenParser("${", "}",
                content -> {
                    System.out.println(content);
                    return content.toLowerCase(Locale.ENGLISH);
                });
        String text = "012,${3456}, ${AB\\C}, \\${DE\\F\\}, ${GHI\\},${JK${LM\\${NO}PQR},${STUVWX\\}YZ";
        System.out.println(text);
        System.out.println(parser.parse(text));
    }
}