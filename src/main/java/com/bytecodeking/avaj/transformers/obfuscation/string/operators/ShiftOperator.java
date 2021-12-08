package com.bytecodeking.avaj.transformers.obfuscation.string.operators;

import java.util.concurrent.ThreadLocalRandom;

import com.github.javaparser.ast.expr.*;

public class ShiftOperator extends AbstractOperator {
    @Override
    public double getStrength() {
        return 1.0D;
    }

    @Override
    protected int[] getRandomValues() {
        int bits1 = ThreadLocalRandom.current().nextInt(31) + 1;
        int bits2 = 32 - bits1;
        return new int[]{bits1, bits2};
    }

    @Override
    public int doRound(int value, int... constants) {
        return value >>> constants[0] | value << constants[1];
    }

    @Override
    protected Expression generateExpr(SimpleName variable, Expression... constants) {
        EnclosedExpr leftShift = new EnclosedExpr(new BinaryExpr(new NameExpr(variable),
                                                                 constants[0],
                                                                 BinaryExpr.Operator.LEFT_SHIFT));
        EnclosedExpr rightShift = new EnclosedExpr(new BinaryExpr(new NameExpr(variable),
                                                                  constants[1],
                                                                  BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT));

        if (ThreadLocalRandom.current().nextBoolean()) {
            return new EnclosedExpr(new BinaryExpr(leftShift, rightShift, BinaryExpr.Operator.BINARY_OR));
        } else {
            return new EnclosedExpr(new BinaryExpr(rightShift, leftShift, BinaryExpr.Operator.BINARY_OR));
        }
    }
}
