package com.bytecodeking.avaj.utils.value;

import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;

public class DefaultObjectGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new NullLiteralExpr();
    }
}
