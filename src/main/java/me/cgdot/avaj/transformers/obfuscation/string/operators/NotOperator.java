package me.cgdot.avaj.transformers.obfuscation.string.operators;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.UnaryExpr;

public class NotOperator extends AbstractOperator {
    @Override
    public double getStrength() {
        return 0.3D;
    }

    @Override
    public int doRound(int value, int... constants) {
        return ~value;
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        return new UnaryExpr(new NameExpr(variable), UnaryExpr.Operator.BITWISE_COMPLEMENT);
    }
}
