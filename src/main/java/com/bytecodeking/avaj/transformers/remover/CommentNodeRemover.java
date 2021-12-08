package com.bytecodeking.avaj.transformers.remover;

import com.bytecodeking.avaj.transformers.ClassTransformer;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class CommentNodeRemover extends ModifierVisitor<Void> implements ClassTransformer {
    @Override
    public String getName() {
        return "CommentRemover";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        clazz.accept(this, null);
    }

    @Override
    public Visitable visit(BlockComment n, Void arg) {
        return null;
    }

    @Override
    public Visitable visit(LineComment n, Void arg) {
        return null;
    }

    @Override
    public Visitable visit(JavadocComment n, Void arg) {
        return null;
    }
}
