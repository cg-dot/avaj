package me.cgdot.avaj.transformers.obfuscation.string;

import com.github.javaparser.ast.expr.SimpleName;

public class Constant {
    private final SimpleName varName;

    public Constant(SimpleName varName) {
        this.varName = varName;
    }

    public SimpleName getVarName() {
        return varName;
    }
}
