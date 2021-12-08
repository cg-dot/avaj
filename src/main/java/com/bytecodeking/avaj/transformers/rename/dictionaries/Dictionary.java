package com.bytecodeking.avaj.transformers.rename.dictionaries;

/**
 * This class represents the generator for symbol.
 */
public interface Dictionary {
    /**
     * Returns a new symbol.
     */
    String next();

    /**
     * Resets the dictionary to generate new symbol from the initial state.
     */
    void reset();
}
