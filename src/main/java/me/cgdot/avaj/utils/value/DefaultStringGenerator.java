package me.cgdot.avaj.utils.value;

import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class DefaultStringGenerator implements ValueGenerator {
    @Override
    public LiteralExpr generate() {
        return new StringLiteralExpr("");
    }
}
