package com.bytecodeking.avaj.transformers.obfuscation.string.operators;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class AddOperator extends AbstractOperator {
    @Override
    protected BinaryExpr.Operator getSimpleOperator() {
        return BinaryExpr.Operator.MINUS;
    }

    @Override
    public double getStrength() {
        return 1.0D;
    }

    @Override
    public int doRound(int value, int... constants) {
        return value + constants[0];
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        return new BinaryExpr(new NameExpr(variable), constants[0], BinaryExpr.Operator.MINUS);
    }
}
