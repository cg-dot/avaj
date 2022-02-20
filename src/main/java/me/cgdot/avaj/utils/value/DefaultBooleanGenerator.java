package me.cgdot.avaj.utils.value;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;

public class DefaultBooleanGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new BooleanLiteralExpr();
    }
}
