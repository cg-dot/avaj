package me.cgdot.avaj.transformers.obfuscation.string.operators;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import me.cgdot.avaj.transformers.obfuscation.string.Constant;
import me.cgdot.avaj.transformers.obfuscation.string.Round;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractOperator {
    protected int[] getRandomValues() {
        return new int[]{ThreadLocalRandom.current().nextInt(0xFFFF - 1) + 1};
    }

    protected BinaryExpr.Operator getSimpleOperator() {
        return null;
    }

    public abstract double getStrength();

    protected abstract Expression generateExpr(SimpleName variable, Expression... constants);

    public abstract int doRound(int value, int... constants);

    public Round makeRound(SimpleName variable, Constant... constants) {
        int[] csts = getRandomValues();
        Expression[] cstExprs = toExpressions(csts);
        return new Round(this, generateRound(variable, cstExprs), csts);
    }

    protected Statement generateRound(SimpleName variable, Expression... constants) {
        // Expr: var [op]= value;
        if (ThreadLocalRandom.current().nextBoolean() && constants.length == 1 && getSimpleOperator() != null) {
            AssignExpr.Operator op;
            switch (getSimpleOperator()) {
                case PLUS:
                    op = AssignExpr.Operator.PLUS;
                    break;
                case MINUS:
                    op = AssignExpr.Operator.MINUS;
                    break;
                case XOR:
                    op = AssignExpr.Operator.XOR;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            return new ExpressionStmt(new AssignExpr(new NameExpr(variable), constants[0], op));
        } else { // Expr: var = [var[op]value|[op]var];
            Expression expr = generateExpr(variable, constants);
            return new ExpressionStmt(new AssignExpr(new NameExpr(variable), expr, AssignExpr.Operator.ASSIGN));
        }
    }

    protected Expression[] toExpressions(int... values) {
        Expression[] exprs = new Expression[values.length];
        for (int i = 0; i < exprs.length; i++) {
            exprs[i] = new IntegerLiteralExpr(String.valueOf(values[i]));
        }
        return exprs;
    }
}
