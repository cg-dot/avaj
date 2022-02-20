package me.cgdot.avaj.transformers.obfuscation.string.operators;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

import java.util.concurrent.ThreadLocalRandom;

public class SubOperator extends AbstractOperator {
    @Override
    protected BinaryExpr.Operator getSimpleOperator() {
        return BinaryExpr.Operator.PLUS;
    }

    @Override
    public double getStrength() {
        return 1.0D;
    }

    @Override
    public int doRound(int value, int... constants) {
        return value - constants[0];
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        Expression left;
        Expression right;
        if (ThreadLocalRandom.current().nextBoolean()) {
            left = new NameExpr(variable);
            right = constants[0];
        } else {
            left = constants[0];
            right = new NameExpr(variable);
        }
        return new BinaryExpr(left, right, BinaryExpr.Operator.PLUS);
    }
}