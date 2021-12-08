package com.bytecodeking.avaj.transformers.rename.dictionaries;

public abstract class AbstractDefaultDictionary implements Dictionary {
    private final String prefix;
    private       int    counter;

    public AbstractDefaultDictionary(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String next() {
        return prefix + counter++;
    }

    @Override
    public void reset() {
        counter = 0;
    }

}
