package com.bytecodeking.avaj.utils.value;

import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;

public class DefaultLongGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new LongLiteralExpr("0L");
    }
}
