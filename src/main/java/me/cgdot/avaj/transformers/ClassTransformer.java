package me.cgdot.avaj.transformers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public interface ClassTransformer {
    /**
     * Returns the transformer name.
     */
    String getName();

    /**
     * Runs the transformer on {@code clazz}.
     */
    void transform(ClassOrInterfaceDeclaration clazz);
}
