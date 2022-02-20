package me.cgdot.avaj.transformers.rename;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.Visitable;
import me.cgdot.avaj.transformers.ClassTransformer;
import me.cgdot.avaj.transformers.rename.dictionaries.RandomVariableDictionary;
import me.cgdot.avaj.visitors.ModifierVisitor;
import org.tinylog.Logger;

import java.util.List;

public class VariableRenamer extends ModifierVisitor<Renamer> implements ClassTransformer {
    @Override
    public String getName() {
        return "VariableRenamer";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            Logger.debug("Renaming variables in method: {}", method.getDeclarationAsString(false, false, false));
            Renamer renamer = new Renamer(new RandomVariableDictionary());

            // Reserve field name (Keep the variable/parameter if the name conflicts with the field name)
            clazz.findAll(FieldDeclaration.class).forEach(f -> f.getVariables().forEach(var -> {
                String name = var.getNameAsString();
                renamer.addReservedName(name);
            }));

            method.accept(this, renamer);
        }

        for (ConstructorDeclaration constructor : clazz.findAll(ConstructorDeclaration.class)) {
            Logger.debug("Renaming variables in method: {}", constructor.getDeclarationAsString(false, false, false));
            Renamer renamer = new Renamer(new RandomVariableDictionary());

            // Reserve field name (Keep the variable/parameter if the name conflicts with the field name)
            clazz.findAll(FieldDeclaration.class).forEach(f -> f.getVariables().forEach(var -> {
                String name = var.getNameAsString();
                renamer.addReservedName(name);
            }));

            constructor.accept(this, renamer);
        }

        for (InitializerDeclaration initializer : clazz.findAll(InitializerDeclaration.class)) {
            Logger.debug("Renaming variables in static block in {}", clazz.getNameAsString());
            Renamer renamer = new Renamer(new RandomVariableDictionary());

            // Reserve field name (Keep the variable/parameter if the name conflicts with the field name)
            clazz.findAll(FieldDeclaration.class).forEach(f -> f.getVariables().forEach(var -> {
                String name = var.getNameAsString();
                renamer.addReservedName(name);
            }));

            initializer.accept(this, renamer);
        }
    }

    @Override
    public Visitable visit(Parameter param, Renamer renamer) {
        SimpleName name = param.getName();
        if (renamer.isReservedName(name.asString())) {
            return super.visit(param, renamer);
        }

        renamer.rename(name);
        return super.visit(param, renamer);
    }

    @Override
    public Visitable visit(VariableDeclarator var, Renamer renamer) {
        SimpleName name = var.getName();
        if (renamer.isReservedName(name.asString())) {
            return super.visit(var, renamer);
        }

        renamer.rename(name);
        return super.visit(var, renamer);
    }

    @Override
    public Visitable visit(NameExpr nameExpr, Renamer renamer) {
        SimpleName name = nameExpr.getName();
        if (!renamer.isRenamed(name)) {
            return super.visit(nameExpr, renamer);
        }

        renamer.rename(name);
        return super.visit(nameExpr, renamer);
    }
}
