package com.bytecodeking.avaj.transformers.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bytecodeking.avaj.transformers.ClassTransformer;
import com.github.javaparser.ast.body.*;

public class MethodScrambler implements ClassTransformer {
    @Override
    public String getName() {
        return "MethodScrambler";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        List<BodyDeclaration<?>> members = new ArrayList<>();

        List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);
        methods.forEach(clazz::remove);
        members.addAll(methods);

        List<ConstructorDeclaration> constructors = clazz.findAll(ConstructorDeclaration.class);
        constructors.forEach(clazz::remove);
        members.addAll(constructors);

        List<InitializerDeclaration> initializers = clazz.findAll(InitializerDeclaration.class);
        initializers.forEach(clazz::remove);
        members.addAll(initializers);

        Collections.shuffle(members);
        members.forEach(clazz::addMember);
    }
}
