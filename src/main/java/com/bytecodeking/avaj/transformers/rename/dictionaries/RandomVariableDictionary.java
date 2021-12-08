package com.bytecodeking.avaj.transformers.rename.dictionaries;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomVariableDictionary implements Dictionary {
    private final List<String> usedName = new ArrayList<>();

    @Override
    public String next() {
        String randName;
        do {
            randName = RandomStringUtils.randomAlphabetic(5, 25);
        } while (usedName.contains(randName));
        usedName.add(randName);
        return randName;
    }

    @Override
    public void reset() {
        usedName.clear();
    }

}
