package me.cgdot.avaj.transformers.obfuscation.string;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithStatements;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import me.cgdot.avaj.transformers.ClassTransformer;
import me.cgdot.avaj.utils.StatementUtils;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringEncryption implements ClassTransformer {
    @Override
    public String getName() {
        return "StringEncryption";
    }

    @Override
    public void transform(ClassOrInterfaceDeclaration clazz) {
        List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            if (!method.getBody().isPresent()) {
                return;
            }
            Logger.debug("Encrypting strings in: {}", method.getDeclarationAsString(false, false, false));
            run(method.getBody().get());
        }
        for (ConstructorDeclaration constructor : clazz.findAll(ConstructorDeclaration.class)) {
            Logger.debug("Encrypting strings in: {}", constructor.getDeclarationAsString(false, false, false));
            run(constructor.getBody());
        }
        for (InitializerDeclaration initializer : clazz.findAll(InitializerDeclaration.class)) {
            Logger.debug("Encrypting strings in static block in {}", clazz.getNameAsString());
            run(initializer.getBody());
        }
    }

    private void run(BlockStmt body) {
        // Extract string to variable
        Map<Statement, List<StringEntry>> encStrings = extractAndReplace(body);

        // Insert decryption routine
        for (Map.Entry<Statement, List<StringEntry>> entry : encStrings.entrySet()) {
            Statement stmt = entry.getKey();
            List<StringEntry> strEntries = entry.getValue();
            for (StringEntry strEntry : strEntries) {
                NodeList<Statement> decStmts = Encryptor.makeDecryptor(strEntry);
                NodeWithStatements<?> parent = (NodeWithStatements<?>) stmt.getParentNode().get();
                parent.getStatements().addAll(parent.getStatements().indexOf(stmt), decStmts);
            }
        }
    }

    private Map<Statement, List<StringEntry>> extractAndReplace(BlockStmt block) {
        Map<Statement, List<StringEntry>> result = new HashMap<>();

        ModifierVisitor<Void> visitor = new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(StringLiteralExpr n, Void arg) {
                if (n.getValue().isEmpty()) {
                    return super.visit(n, arg);
                }
                StringEntry entry = new StringEntry(n.asString());
                Statement topStmt = StatementUtils.findParentBlock(n);

                result.computeIfAbsent(topStmt, k -> new ArrayList<>()).add(entry);
                return new NameExpr(entry.getVarName());
            }

            @Override
            public Visitable visit(SwitchEntry n, Void arg) { // Skip string constants from `switch`
                NodeList<Statement> statements = modifyList(n.getStatements(), arg);
                n.setStatements(statements);
                return n;
            }

            private <N extends Node> NodeList<N> modifyList(NodeList<N> list, Void arg) {
                return (NodeList<N>) list.accept(this, arg);
            }
        };
        block.accept(visitor, null);
        return result;
    }
}
