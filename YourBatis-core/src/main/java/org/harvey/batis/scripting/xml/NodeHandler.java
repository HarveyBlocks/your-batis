package org.harvey.batis.scripting.xml;

import org.harvey.batis.parsing.XNode;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-08-11 17:59
 */
public interface NodeHandler {
    /**
     * 解析一个节点(标签)
     *
     * @param nodeToHandle   节点本身
     * @param targetContents 前面的节点, <br>
     *                       也有一些不是节点, 是单纯的文本, 但看作节点<br>
     *                       和当前待解析的节点是同一层的<br>
     *                       递归解析过程中的上下文
     */
    void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
}
