package com.bytecodeking.avaj.transformers.obfuscation.string.operators;

import com.bytecodeking.avaj.transformers.obfuscation.string.Constant;
import com.bytecodeking.avaj.transformers.obfuscation.string.Round;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class XorIndexOperator extends XorOperator {
    @Override
    public double getStrength() {
        return 0.1D;
    }

    @Override
    public Round makeRound(SimpleName variable, Constant... constants) {
        return new Round(this, generateRound(variable, new NameExpr(constants[0].getVarName())), null);
    }
}
