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

package me.cgdot.avaj;

import me.cgdot.avaj.transformers.ClassTransformer;
import me.cgdot.avaj.transformers.misc.MethodScrambler;
import me.cgdot.avaj.transformers.obfuscation.flow.Flattening;
import me.cgdot.avaj.transformers.obfuscation.number.ExtractInteger;
import me.cgdot.avaj.transformers.obfuscation.string.StringEncryption;
import me.cgdot.avaj.transformers.remover.CommentNodeRemover;
import me.cgdot.avaj.transformers.rename.VariableRenamer;

import java.nio.file.Path;
import java.util.Arrays;

public class JavaObfuscator {
    private static final ClassTransformer[] DEFAULT_TRANSFORMERS = new ClassTransformer[]{
            new CommentNodeRemover(),
            new Flattening(),
            new StringEncryption(),
            new ExtractInteger(),
            new VariableRenamer(),
            new MethodScrambler()
    };

    public static String obfuscateWithDefaultOption(Path src) {
        return obfuscate(src, DEFAULT_TRANSFORMERS);
    }

    public static String obfuscate(Path src, ClassTransformer... transformers) {
        return new Obfuscator(src, Arrays.asList(transformers)).run();
    }
}
