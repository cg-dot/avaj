package me.cgdot.avaj.utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.Statement;

public class StatementUtils {
    public static Statement findParentBlock(Node node) {
        if (!node.getParentNode().isPresent() || node.getParentNode().get() instanceof NodeWithStatements) {
            return (Statement) node;
        }
        return findParentBlock(node.getParentNode().get());
    }
}
