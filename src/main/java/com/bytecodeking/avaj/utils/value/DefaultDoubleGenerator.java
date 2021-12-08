package com.bytecodeking.avaj.utils.value;

import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;

public class DefaultDoubleGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new DoubleLiteralExpr(0.0D);
    }
}
