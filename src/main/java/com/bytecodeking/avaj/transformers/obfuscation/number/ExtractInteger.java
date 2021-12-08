package com.bytecodeking.avaj.transformers.obfuscation.number;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.bytecodeking.avaj.transformers.ClassTransformer;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.tinylog.Logger;

public class ExtractInteger implements ClassTransformer {
    @Override
    public String getName() {
        return "ExtractInteger";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            if (!method.getBody().isPresent()) {
                return;
            }
            Logger.debug("Extract integers in: {}", method.getDeclarationAsString(false, false, false));
            run(method.getBody().get());
        }
        for (ConstructorDeclaration constructor : clazz.findAll(ConstructorDeclaration.class)) {
            Logger.debug("Extract integers in: {}", constructor.getDeclarationAsString(false, false, false));
            run(constructor.getBody());
        }
        for (InitializerDeclaration initializer : clazz.findAll(InitializerDeclaration.class)) {
            Logger.debug("Extract integers in: static block in {}", clazz.getNameAsString());
            run(initializer.getBody());
        }
    }

    private void run(BlockStmt body) {
        // Array variable
        SimpleName arrayVar = new SimpleName(UUID.randomUUID().toString());

        // Extract all the integers (except 0 ~ 65535, int and char are not compatible)
        List<IntegerLiteralExpr> intPool = new ArrayList<>(extract(body));
        // Add random elements to the pool
        for (int i = 0; i < ThreadLocalRandom.current().nextInt(20) + 8; i++) {
            intPool.add(new IntegerLiteralExpr(String.valueOf(ThreadLocalRandom.current().nextInt())));
        }
        Collections.shuffle(intPool);

        // Create int array
        body.getStatements().add(0, new ExpressionStmt(new VariableDeclarationExpr(
                new VariableDeclarator(StaticJavaParser.parseType("int[]"),
                                       arrayVar,
                                       toInitializerExpr(intPool)))));

        // Replace int constants to array access
        replace(body, intPool, arrayVar);
    }

    private Set<IntegerLiteralExpr> extract(BlockStmt block) {
        Set<IntegerLiteralExpr> result = new HashSet<>();
        GenericVisitorAdapter<Void, Void> visitor = new GenericVisitorAdapter<Void, Void>() {
            @Override
            public Void visit(IntegerLiteralExpr n, Void arg) {
                if (!shouldExtract(n)) {
                    return null;
                }
                result.add(n);
                return null;
            }

            @Override
            public Void visit(SwitchEntry n, Void arg) {
                return n.getStatements().accept(this, arg);
            }

            @Override
            public Void visit(ArrayCreationExpr n, Void arg) {
                return null;
            }

            @Override
            public Void visit(ArrayInitializerExpr n, Void arg) {
                return null;
            }

            @Override
            public Void visit(ArrayAccessExpr n, Void arg) {
                if (n.getIndex().isIntegerLiteralExpr()) {
                    result.add(n.getIndex().asIntegerLiteralExpr());
                }
                return null;
            }
        };
        block.accept(visitor, null);
        return result;
    }

    private void replace(BlockStmt block, List<IntegerLiteralExpr> ints, SimpleName arrayVar) {
        ModifierVisitor<Void> visitor = new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(IntegerLiteralExpr n, Void arg) {
                if (shouldExtract(n) && ints.contains(n)) {
                    return new ArrayAccessExpr(new NameExpr(arrayVar),
                                               new IntegerLiteralExpr(String.valueOf(ints.indexOf(n))));
                }
                return n;
            }

            @Override
            public Visitable visit(SwitchEntry n, Void arg) {
                NodeList<Statement> statements = modifyList(n.getStatements(), arg);
                n.setStatements(statements);
                return n;
            }

            @Override
            public Visitable visit(ArrayCreationExpr n, Void arg) {
                return n;
            }

            @Override
            public Visitable visit(ArrayInitializerExpr n, Void arg) {
                return n;
            }

            @Override
            public Visitable visit(ArrayAccessExpr n, Void arg) {
                if (n.getIndex().isIntegerLiteralExpr()) {
                    IntegerLiteralExpr index = n.getIndex().asIntegerLiteralExpr();
                    if (ints.contains(index)) {
                        n.setIndex(new ArrayAccessExpr(new NameExpr(arrayVar),
                                                       new IntegerLiteralExpr(String.valueOf(ints.indexOf(index)))));
                    }
                }
                return n;
            }

            private <N extends Node> NodeList<N> modifyList(NodeList<N> list, Void arg) {
                return (NodeList<N>) list.accept(this, arg);
            }
        };
        block.accept(visitor, null);
    }

    private ArrayInitializerExpr toInitializerExpr(List<IntegerLiteralExpr> ints) {
        ArrayInitializerExpr expr = new ArrayInitializerExpr();
        for (IntegerLiteralExpr intExpr : ints) {
            expr.getValues().add(intExpr);
        }
        return expr;
    }

    private boolean shouldExtract(IntegerLiteralExpr intExpr) {
        int intVal = (int) intExpr.asNumber();
        if (intVal >= 0 && intVal <= 65535) {
            return false;
        }
        return true;
    }
}
