package me.cgdot.avaj.utils.value;

import com.github.javaparser.ast.expr.LiteralExpr;

public interface ValueGenerator {
    LiteralExpr generate();
}
