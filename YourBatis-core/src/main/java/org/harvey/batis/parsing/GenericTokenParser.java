package org.harvey.batis.parsing;

/**
 * 通用令牌解析器<br>
 * 将被{@link #openToken}和{@link #closeToken}包围的字符串的称为content<br>
 * 将content交由{@link #handler}处理之后再度拼接
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-05 16:20
 */
public class GenericTokenParser {

    /**
     * 开放令牌, 表示之后的部分是content<br>
     * 在content之前, 和{@link #closeToken}夹起来的部分是content
     */
    private final String openToken;
    /**
     * 闭合令牌, 表示content的结束<br>
     * 在content之后, 和{@link #openToken}夹起来的部分是content
     */
    private final String closeToken;
    /**
     * 拿到content去处理
     */
    private final TokenHandler handler;

    /**
     * @param openToken  {@link #openToken}
     * @param closeToken {@link #closeToken}
     * @param handler    处理器 {@link #handler}
     */
    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    /**
     * <p>
     * 在text中找出字符串content, 然后用{@link #handler}处理content<br>
     * content处理之后会再次拼回text<br>
     * text中依靠{@link #openToken}和{@link #closeToken}来找content, 贪心<br>
     * </p>
     * <p>
     * 如果text中存在部分字符串和{@link #openToken}或{@link #closeToken}一样, <br>
     * 但不希望它们被识别为{@link #openToken}和{@link #closeToken}, <br>
     * 可以用`{@code \}`做开放口令在{@link #openToken}和{@link #closeToken}之前使其不被视作有效的口令<br>
     * </p>
     * <p>
     * 把`{@code \}`做开放口令和闭合口令的转义时, 会被删除; 其他用处就不会被删除(例如转义其他的字符).<br>
     * 1. 在扫描text并寻找闭合口令时, 如果遇到了被转义的闭合口令, 会删除`\`<br>
     * 2. 在扫描text并寻找开放口令时, 如果遇到了被转义的开放口令, 会删除`\`<br>
     * 3. 在扫描text并寻找闭合口令时, 如果遇到了被转义的开放口令, 不会删除`\`<br>
     * 3. 在扫描text并寻找开放口令时, 如果遇到了被转义的闭合口令, 不会删除`\`<br>
     * 4. 在扫描text并寻找闭合口令时, 如果最终都没有找到闭合口令, 那么不会删除中途转义的闭合口令前的`\`<br>
     * 5. content中的`\`, 和result是一样的, result中要求被删除的`\`, 提供的content中也会被删除, 否则不会被删除
     * </p>
     * <p>
     * 例如:
     * <pre>{@code
     * openToken="${" ;
     * closeToken="}" ;
     * text="012,${3456}, ${AB\C}, \${DE\F\}, ${GHI\},${JK${LM\${NO}PQR},${STU\VWX\}YZ";
     * // 这里的`\`是字符串里实质的`\`, 而不是代码层面的`\`, 要特别注意
     * content = {"3456","AB\C","GHI},${JK${LM\${NO"};
     * handler = String::toLowerCase;
     * return "012,3456, ab\c, ${DE\F\}, ghi},${jk${lm\${noPQR},${STU\VWX\}YZ";
     * // }}}}
     * }
     * </pre>
     *
     * </p>
     *
     * @param text 匹配里面的{@link #openToken}和{@link #closeToken}<br>
     *             对于{@link #openToken}和{@link #closeToken}, 可以在前面加{@code \}表示该符号不启用
     * @return 被handler处理过, 再拼接的字符串
     */
    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // start始终指向text/src中开放令牌的第一个字符
        int start = text.indexOf(openToken);
        if (start == -1) {
            // 没找到开始的开放口令
            return text;
        }
        char[] src = text.toCharArray();
        // content是被开放令牌和闭合令牌围起来的部分
        StringBuilder contentBuilder = new StringBuilder();
        // 最终的结果, 三个任务:
        // 1. 去出text中所有的`\`
        // 2. 将content交给handler处理, 处理后拼入result
        // 3. 有效的openToken和resultToken需要被去除(无效的保留)
        final StringBuilder resultBuilder = new StringBuilder();
        int offset = 0; // 指向src/text内容的指针, offset前面都是已经检查过并加入result的, 后面是还没检查过并加入result的
        do {
            if (start > 0 && src[start - 1] == '\\') {
                // 此开放令牌(开放口令)被转义。删除反斜杠
                int len = start - offset - 1;
                resultBuilder.append(src, offset, len)
                        // 当前的Token是无效的, 只是普通字符串, 加回去
                        .append(openToken);
                // offset移到开放口令最后一个字符的后面
                offset = start + openToken.length();
            } else {
                // 找到了开放令牌, 没有被转义, 是有效的"开放口令"
                // 把开放令牌前面的部分加入result
                int len = start - offset;
                resultBuilder.append(src, offset, len);

                // 此时开始找闭合口令
                // 如果在找到闭合口令之前有开放口令, 无视

                // 清空contentBuilder, 或创建新的空contentBuilder
                contentBuilder.setLength(0);


                offset = start + openToken.length();
                // end始终指向text/src中闭合令牌的第一个字符
                int end = text.indexOf(closeToken, offset);
                // 划分content
                while (end > -1) {
                    // 只要有闭合令牌
                    if (end <= offset || src[end - 1] != '\\') {
                        // 找到有效的闭合令牌, 将闭合令牌前的内容加入content
                        contentBuilder.append(src, offset, end - offset);
                        break;
                    }
                    // 此闭合令牌(闭合口令)被转义, 是无效的闭合令牌, 只被看作简单的字符串
                    // 删除反斜杠
                    // 反斜杠之前的内容加入content
                    contentBuilder.append(src, offset, end - offset - 1)
                            // 将闭合令牌加入到content
                            .append(closeToken);
                    // offset移动到闭合令牌之后
                    offset = end + closeToken.length();
                    // 继续寻找有效的闭合令牌
                    end = text.indexOf(closeToken, offset);
                }
                if (end == -1) {
                    // 有开放令牌, 但是, 知道text最后也没有找到对应的闭合令牌
                    // 这个开放令牌之后的内容不被视作content
                    // 忽略这个开放令牌, 将后面的内容加入result
                    resultBuilder.append(src, start, src.length - start);
                    // 能加的全加到result里去了
                    offset = src.length;

                } else {
                    // 有开放令牌也有闭合令牌, 此时content就已经圆满
                    // 就将content交给handler处理, 处理之后加入result
                    resultBuilder.append(handler.handleToken(contentBuilder.toString()));
                    // offset指向找到的end的后面
                    offset = end + closeToken.length();
                }
            }
            // start移动到下一个开放令牌上
            start = text.indexOf(openToken, offset);
        } while (start > -1/* 每当还能找到下一个开放令牌, 循环*/);
        // 没有开放令牌了
        if (offset < src.length) {
            // 但还有内容没有加入result
            resultBuilder.append(src, offset, src.length - offset);
        }
        return resultBuilder.toString();
    }
}
