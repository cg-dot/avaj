/*
 * Avaj
 * Copyright (C) 2022 Cg <cg@bytecodeking.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.cgdot.avaj.transformers.rename;

import com.github.javaparser.ast.expr.SimpleName;
import me.cgdot.avaj.transformers.rename.dictionaries.Dictionary;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Renamer {
    private Dictionary dict;
    private Set<String> reservedNames;
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
