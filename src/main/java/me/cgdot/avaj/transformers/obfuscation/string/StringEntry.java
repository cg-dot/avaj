package me.cgdot.avaj.transformers.obfuscation.string;

import com.github.javaparser.ast.expr.SimpleName;

import java.util.UUID;

public class StringEntry {
    private final SimpleName varName;
    private String rawString;

    public StringEntry(String rawString) {
        this(new SimpleName(UUID.randomUUID().toString()), rawString);
    }

    public StringEntry(SimpleName varName, String rawString) {
        this.varName = varName;
        this.rawString = rawString;
    }

    public SimpleName getVarName() {
        return varName;
    }

    public String getRawString() {
        return rawString;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }
}
