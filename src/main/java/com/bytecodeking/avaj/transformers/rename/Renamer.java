package com.bytecodeking.avaj.transformers.rename;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bytecodeking.avaj.transformers.rename.dictionaries.Dictionary;
import com.github.javaparser.ast.expr.SimpleName;
import org.tinylog.Logger;

public class Renamer {
    private Dictionary          dict;
    private Set<String>         reservedNames;
    private Map<String, String> mapping;

    public Renamer(Dictionary dict) {
        this.dict = dict;
        this.reservedNames = new HashSet<>();
        this.mapping = new HashMap<>();
    }

    public void rename(SimpleName name) {
        String nameAsString = name.asString();
        String newName;
        if (mapping.containsKey(nameAsString)) {
            newName = mapping.get(nameAsString);
        } else {
            newName = mapping.computeIfAbsent(nameAsString, k -> getNewName());
            Logger.trace("Rename {} -> {}", nameAsString, newName);
        }
        name.setIdentifier(newName);
    }

    public boolean isRenamed(SimpleName name) {
        return mapping.containsKey(name.asString());
    }

    public void addReservedName(String name) {
        reservedNames.add(name);
    }

    public void removeReservedName(String name) {
        reservedNames.remove(name);
    }

    public boolean isReservedName(String name) {
        return reservedNames.contains(name);
    }

    public String getNewName() {
        while (true) {
            String newName = dict.next();
            // Ensure the new name is unique
            if (!reservedNames.contains(newName) && !mapping.containsValue(newName)) {
                return newName;
            }
        }
    }
}
