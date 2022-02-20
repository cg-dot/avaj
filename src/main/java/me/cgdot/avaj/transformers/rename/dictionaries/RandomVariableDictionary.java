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

package me.cgdot.avaj.transformers.rename.dictionaries;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

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
