package me.cgdot.avaj.transformers.obfuscation.flow;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.cgdot.avaj.analysis.ControlFlowLogic;
import me.cgdot.avaj.transformers.ClassTransformer;
import me.cgdot.avaj.utils.StatementUtils;
import me.cgdot.avaj.utils.value.ValueInitializer;
import org.apache.commons.lang3.tuple.Pair;
import org.tinylog.Logger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Flattening implements ClassTransformer {
    @Override
    public String getName() {
        return "Flattening";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        for (MethodDeclaration method : clazz.findAll(MethodDeclaration.class)) {
            if (!method.getBody().isPresent()) {
                continue;
            }
            Logger.debug("Flattening: {}", method.getDeclarationAsString(false, false, false));
            flatten(method.getBody().get(), method.getType());
        }
        for (ConstructorDeclaration constructor : clazz.findAll(ConstructorDeclaration.class)) {
            Logger.debug("Flattening: {}", constructor.getDeclarationAsString(false, false, false));
            flatten(constructor.getBody(), StaticJavaParser.parseType("void"));
        }
        for (InitializerDeclaration initializer : clazz.findAll(InitializerDeclaration.class)) {
            Logger.debug("Flattening: static block in {}", clazz.getNameAsString());
            flatten(initializer.getBody(), null);
        }
    }

    private void flatten(BlockStmt body, Type returnType) {
        List<Statement> stmts = new ArrayList<>(body.getStatements());
        List<VariableDeclarator> variables = new ArrayList<>();

        // Clear the original body
        body.getStatements().clear();

        // Create dispatcher selector variable
        VariableDeclarator selectorVar = new VariableDeclarator(PrimitiveType.intType(), UUID.randomUUID().toString());
        variables.add(selectorVar);

        // Exit key
        int exitDispatcherKey = ThreadLocalRandom.current().nextInt();

        // Create an endless while loop
        WhileStmt whileStmt;
        if (returnType == null) {
            whileStmt = new WhileStmt(new BinaryExpr(new NameExpr(selectorVar.getNameAsString()),
                    new IntegerLiteralExpr(String.valueOf(exitDispatcherKey)),
                    BinaryExpr.Operator.NOT_EQUALS), new BlockStmt());
        } else {
            whileStmt = new WhileStmt(new BooleanLiteralExpr(true), new BlockStmt());
        }

        // Set the while block as the method body
        body.getStatements().add(whileStmt);

        // Create dispatcher
        SwitchStmt dispatcherStmt = new SwitchStmt();
        dispatcherStmt.setSelector(new NameExpr(selectorVar.getName()));

        // Add the dispatcher to while block
        ((BlockStmt) whileStmt.getBody()).getStatements().add(dispatcherStmt);

        // Mark variables
        Map<Statement, List<ExpressionStmt>> varReplacement = new HashMap<>();
        for (Statement stmt : stmts) {
            if (stmt.isExpressionStmt()) {
                Expression expr = ((ExpressionStmt) stmt).getExpression();
                if (!expr.isVariableDeclarationExpr()) {
                    continue;
                }

                // Replace variable declaration to assign expression
                List<ExpressionStmt> assignStmts = new ArrayList<>();
                for (VariableDeclarator var : expr.asVariableDeclarationExpr().getVariables()) {
                    if (var.getInitializer().isPresent()) {
                        Expression initExpr = var.getInitializer().get();
                        // Keep array initializer: int[] var = {...};
                        if (initExpr.isArrayInitializerExpr()) {
                            continue;
                        }
                        // Disconnect the initializer
                        var.setInitializer((Expression) null);

                        assignStmts.add(new ExpressionStmt(new AssignExpr(new NameExpr(var.getNameAsString()),
                                initExpr,
                                AssignExpr.Operator.ASSIGN)));
                    }
                }

                varReplacement.put(stmt, assignStmts);
                variables.addAll(expr.asVariableDeclarationExpr().getVariables());
            }
        }

        for (Map.Entry<Statement, List<ExpressionStmt>> entry : varReplacement.entrySet()) {
            Statement stmt = entry.getKey();
            List<ExpressionStmt> replacement = entry.getValue();
            stmts.addAll(stmts.indexOf(stmt), replacement);
            stmts.remove(stmt);
        }

        // Create op for entries
        Set<Integer> usedKeys = new HashSet<>();
        usedKeys.add(exitDispatcherKey);

        List<Pair<Integer, Statement>> entryKeys = new ArrayList<>();
        for (Statement stmt : stmts) {
            int key;
            do {
                key = ThreadLocalRandom.current().nextInt();
            } while (usedKeys.contains(key));
            usedKeys.add(key);
            entryKeys.add(Pair.of(key, stmt));
        }

        // Create dispatcher entries
        List<SwitchEntry> dispatcherEntries = new ArrayList<>();
        for (int i = 0; i < stmts.size(); i++) {
            Statement stmt = stmts.get(i);
            SwitchEntry entry = new SwitchEntry();
            entry.getLabels().add(new IntegerLiteralExpr(String.valueOf(entryKeys.get(i).getLeft())));
            entry.getStatements().add(stmt);
            dispatcherEntries.add(entry);

            // Set redirection for next entry (if exists)
            if (i + 1 < stmts.size()) {
                // Assign the next key
                entry.getStatements().add(new ExpressionStmt(getMutatedConstAssignExpr(
                        new NameExpr(selectorVar.getNameAsString()), // selector
                        entryKeys.get(i).getLeft(), // current entry's key
                        entryKeys.get(i + 1).getLeft()))); // next entry's key
                entry.getStatements().add(new BreakStmt());
            } else if (!stmt.isReturnStmt()) { // Skip inserting `return` if the last stmt is `return` or the return value is not void
                // Check reachability before inserting `return`
                if (ControlFlowLogic.getInstance().canCompleteNormally(stmt, true)) {
                    if (returnType == null) { // Can't insert `return` to static block, set redirection
                        entry.getStatements().add(new ExpressionStmt(getMutatedConstAssignExpr(
                                new NameExpr(selectorVar.getNameAsString()), // selector
                                entryKeys.get(i).getLeft(), // current entry's key
                                exitDispatcherKey))); // next entry's key
                        entry.getStatements().add(new BreakStmt());
                    }
                    // A non-void method does not need to insert `return` in the last entry. We assume
                    // that the last statement of the non-void method always fails to complete normally.
                    else if (returnType.isVoidType()) {
                        entry.getStatements().add(new ReturnStmt());
                    }
                }
            }
        }
        Collections.shuffle(dispatcherEntries);

        // Add entries to dispatcher
        dispatcherStmt.getEntries().addAll(dispatcherEntries);

        // Generate initialized variables
        List<ExpressionStmt> varStmts = getInitializeVarStmts(variables);

        // Assign the key for first entry
        varStmts.add(new ExpressionStmt(new AssignExpr(new NameExpr(selectorVar.getNameAsString()),
                new IntegerLiteralExpr(String.valueOf(entryKeys.get(0).getLeft())),
                AssignExpr.Operator.ASSIGN)));

        // Set variables
        body.getStatements().addAll(0, varStmts);

        // Patch lambda variables
        replaceLambdaVariable(variables, body);
    }

    private void replaceLambdaVariable(List<VariableDeclarator> variables, BlockStmt block) {
        Map<SimpleName, Type> varNames = variables.stream().collect(Collectors.toMap(
                VariableDeclarator::getName, VariableDeclarator::getType));
        Map<NodeWithStatements<?>, Set<Pair<SimpleName, Type>>> parents = new HashMap<>();
        BiMap<SimpleName, SimpleName> varMapping = HashBiMap.create();
        List<NameExpr> replacement = new ArrayList<>();

        GenericVisitorAdapter<Void, Void> visitor = new GenericVisitorAdapter<Void, Void>() {
            @Override
            public Void visit(LambdaExpr n, Void arg) {
                NodeWithStatements<?> parent = (NodeWithStatements<?>) StatementUtils.findParentBlock(n).getParentNode().get();
                List<NameExpr> varExpr = n.findAll(NameExpr.class);
                for (NameExpr v : varExpr) {
                    if (varNames.containsKey(v.getName())) {
                        // Making oldName -> newName mapping
                        varMapping.putIfAbsent(v.getName(), new SimpleName(UUID.randomUUID().toString()));
                        // Mark the new variable name and type
                        parents.computeIfAbsent(parent, k -> new HashSet<>()).add(
                                Pair.of(varMapping.get(v.getName()), varNames.get(v.getName())));
                        replacement.add(v);
                    }
                }
                return null;
            }
        };
        block.accept(visitor, null);

        // Create new variables
        for (Map.Entry<NodeWithStatements<?>, Set<Pair<SimpleName, Type>>> entry : parents.entrySet()) {
            NodeWithStatements<?> list = entry.getKey();
            for (Pair<SimpleName, Type> p : entry.getValue()) {
                list.getStatements().add(0, new ExpressionStmt(new VariableDeclarationExpr(
                        // Type newVar = oldVar;
                        new VariableDeclarator(p.getRight(),  // type
                                p.getLeft(),   // newVar
                                new NameExpr(varMapping.inverse().get(p.getLeft())))))); // oldVar
            }
        }

        // Rename to the new variable
        for (NameExpr v : replacement) {
            v.setName(varMapping.get(v.getName()));
        }
    }

    private AssignExpr getMutatedConstAssignExpr(NameExpr variable, int curKey, int nextKey) {
        try {
            switch (ThreadLocalRandom.current().nextInt(5)) {
                case 0: // -=
                    return new AssignExpr(variable,
                            new IntegerLiteralExpr(String.valueOf(Math.subtractExact(curKey, nextKey))),
                            AssignExpr.Operator.MINUS);
                case 1: // +=
                    return new AssignExpr(variable,
                            new IntegerLiteralExpr(String.valueOf(Math.subtractExact(nextKey, curKey))),
                            AssignExpr.Operator.PLUS);
            }

        } catch (ArithmeticException ignore) {
        }

        // Fallback
        // ^=
        return new AssignExpr(variable,
                new IntegerLiteralExpr(String.valueOf(curKey ^ nextKey)),
                AssignExpr.Operator.XOR);
    }

    private List<ExpressionStmt> getInitializeVarStmts(List<VariableDeclarator> variables) {
        List<ExpressionStmt> varDeclarations = new ArrayList<>();

        variables.forEach(v -> {
            if (!v.getInitializer().isPresent() || !v.getInitializer().get().isArrayInitializerExpr()) {
                v.setInitializer(ValueInitializer.getDefault(v.getType()));
            }
            varDeclarations.add(new ExpressionStmt(new VariableDeclarationExpr(v)));
        });

        Collections.shuffle(varDeclarations);
        return varDeclarations;
    }
}
