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
