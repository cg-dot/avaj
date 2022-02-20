package me.cgdot.avaj.transformers.obfuscation.string.operators;

import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import me.cgdot.avaj.transformers.obfuscation.string.Constant;
import me.cgdot.avaj.transformers.obfuscation.string.Round;

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
