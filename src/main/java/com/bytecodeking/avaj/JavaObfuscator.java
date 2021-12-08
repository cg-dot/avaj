package com.bytecodeking.avaj;

import java.nio.file.Path;
import java.util.Arrays;

import com.bytecodeking.avaj.transformers.ClassTransformer;
import com.bytecodeking.avaj.transformers.misc.MethodScrambler;
import com.bytecodeking.avaj.transformers.obfuscation.flow.Flattening;
import com.bytecodeking.avaj.transformers.obfuscation.number.ExtractInteger;
import com.bytecodeking.avaj.transformers.obfuscation.string.StringEncryption;
import com.bytecodeking.avaj.transformers.remover.CommentNodeRemover;
import com.bytecodeking.avaj.transformers.rename.VariableRenamer;

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
