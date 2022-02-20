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

public abstract class AbstractDefaultDictionary implements Dictionary {
    private final String prefix;
    private int counter;

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
