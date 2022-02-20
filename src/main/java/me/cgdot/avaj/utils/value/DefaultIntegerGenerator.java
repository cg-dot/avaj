package me.cgdot.avaj.utils.value;

import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;

public class DefaultIntegerGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new IntegerLiteralExpr("0");
    }
}
