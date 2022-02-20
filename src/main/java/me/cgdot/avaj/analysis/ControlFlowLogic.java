package me.cgdot.avaj.analysis;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consider Control Flow to determine which statements are reachable.
 * <p>
 * Except for the special treatment of while, do, and for statements whose condition expression has the constant value
 * true, the values of expressions are not taken into account in the flow analysis.
 * <p>
 * See JLS 14.21
 *
 * @author Federico Tomassetti, Cg.
 */
public class ControlFlowLogic {

    private static ControlFlowLogic instance = new ControlFlowLogic();

    private ControlFlowLogic() {
    }

    public static ControlFlowLogic getInstance() {
        return instance;
    }

    public boolean isReachable(Statement statement) {
        if (!statement.getParentNode().isPresent()) {
            return false;
        }

        Node parent = statement.getParentNode().get();
        // The block that is the body of a constructor, method, instance initializer, or static initializer is
        // reachable
        if (statement instanceof BlockStmt) {
            if (parent instanceof ConstructorDeclaration ||
                    parent instanceof MethodDeclaration ||
                    parent instanceof InitializerDeclaration) {
                return true;
            }
        }

        if (!isReachableBecauseOfParent(statement)) {
            return false;
        }

        if (parent instanceof BlockStmt) {
            return isReachableBecauseOfPosition(statement);
        } else {
            return true;
        }
    }

    private boolean isReachableBecauseOfParent(Statement statement) {
        if (!statement.getParentNode().isPresent()) {
            return false;
        }

        Node parent = statement.getParentNode().get();
        GenericVisitorAdapter<Boolean, Void> visitor = new GenericVisitorAdapter<Boolean, Void>() {
            @Override
            public Boolean visit(BlockStmt n, Void arg) {
                return isReachable(n);
            }

            @Override
            public Boolean visit(SwitchEntry n, Void arg) {
                return n.getParentNode().isPresent() &&
                        (n.getParentNode().get() instanceof SwitchStmt || n.getParentNode().get() instanceof SwitchEntry) &&
                        isReachable((Statement) n.getParentNode().get());
            }

            @Override
            public Boolean visit(DoStmt n, Void arg) {
                // The contained statement is reachable iff the do statement is reachable.
                return isReachable(n);
            }

            @Override
            public Boolean visit(ForEachStmt n, Void arg) {
                return isReachable(n);
            }

            @Override
            public Boolean visit(ForStmt n, Void arg) {
                // The contained statement is reachable iff the for statement is reachable and
                // the condition expression is not a constant expression whose value is false.
                return isReachable(n) && (!n.getCompare().isPresent() ||
                        !n.getCompare().get().isBooleanLiteralExpr() ||
                        ((BooleanLiteralExpr) n.getCompare().get()).getValue());
            }

            @Override
            public Boolean visit(WhileStmt n, Void arg) {
                // The contained statement is reachable iff the while statement is reachable and
                // the condition expression is not a constant expression whose value is false.
                return isReachable(n) && (!n.getCondition().isBooleanLiteralExpr() ||
                        ((BooleanLiteralExpr) n.getCondition()).getValue());
            }

            @Override
            public Boolean visit(IfStmt n, Void arg) {
                return isReachable(n);
            }

            @Override
            public Boolean visit(LabeledStmt n, Void arg) {
                // The contained statement is reachable iff the labeled statement is reachable.
                return isReachable(n);
            }

            @Override
            public Boolean visit(SynchronizedStmt n, Void arg) {
                // The contained statement is reachable iff the synchronized statement is reachable.
                return isReachable(n);
            }

            @Override
            public Boolean visit(CatchClause n, Void arg) {
                return n.getParentNode().isPresent() &&
                        n.getParentNode().get() instanceof TryStmt &&
                        isReachable((Statement) n.getParentNode().get());
            }

            @Override
            public Boolean visit(TryStmt n, Void arg) {
                return isReachable(n);
            }
        };

        return parent.accept(visitor, null);
    }

    private boolean isReachableBecauseOfPosition(Statement statement) {
        if (!statement.getParentNode().isPresent() || !(statement.getParentNode().get() instanceof BlockStmt)) {
            return false;
        }

        BlockStmt parent = ((BlockStmt) statement.getParentNode().get());
        if (!parent.isEmpty()) {
            NodeList<Statement> stmts = parent.getStatements();
            Optional<Statement> firstStmt = stmts.getFirst();
            // The first statement in a non-empty block that is not a switch block is reachable
            // iff the block is reachable.
            if (firstStmt.isPresent() && firstStmt.get() == statement) {
                return true;
            }

            // Every other statement S in a non-empty block that is not a switch block is reachable
            // iff the statement preceding S can complete normally.
            for (int i = 0; i < parent.getStatements().size(); i++) {
                Statement s = parent.getStatement(i);
                if (s == statement) {
                    return true;
                }

                if (!canCompleteNormally0(s)) {
                    return false;
                }
            }
        }

        return false;
    }

    public boolean canCompleteNormally(Statement statement, boolean skipReachableCheck) {
        if (!skipReachableCheck) {
            // A statement can complete normally only if it is reachable.
            if (!isReachable(statement)) {
                return false;
            }
        }
        return canCompleteNormally0(statement);
    }

    private boolean canCompleteNormally0(Statement statement) {
        GenericVisitorAdapter<Boolean, Void> visitor = new GenericVisitorAdapter<Boolean, Void>() {
            @Override
            public Boolean visit(BlockStmt block, Void arg) {
                return canBlockCompleteNormally(block);
            }

            @Override
            public Boolean visit(ExpressionStmt n, Void arg) {
                // An expression statement can complete normally iff it is reachable.
                Boolean result = n.getExpression().accept(this, arg);
                return result == null || result;
            }

            @Override
            public Boolean visit(LocalClassDeclarationStmt n, Void arg) {
                // A local class declaration statement can complete normally iff it is reachable.
                return true;
            }

            @Override
            public Boolean visit(LocalRecordDeclarationStmt n, Void arg) {
                // A local record declaration statement can complete normally if it is reachable.
                return true;
            }

            @Override
            public Boolean visit(VariableDeclarationExpr n, Void arg) {
                // A local variable declaration statement can complete normally iff it is reachable.
                return true;
            }

            @Override
            public Boolean visit(EmptyStmt n, Void arg) {
                // An empty statement can complete normally iff it is reachable.
                return true;
            }

            @Override
            public Boolean visit(LabeledStmt n, Void arg) {
                /*
                  A labeled statement can complete normally if at least one of the following is true:
                 */

                // The contained statement can complete normally.
                if (canCompleteNormally0(n.getStatement())) {
                    return true;
                }

                // There is a reachable break statement that exits the labeled statement.
                return exitTheStatement(n, n.getStatement());
            }

            @Override
            public Boolean visit(ExplicitConstructorInvocationStmt n, Void arg) {
                return true;
            }

            @Override
            public Boolean visit(IfStmt n, Void arg) {
                /*
                  - An if-then statement can complete normally iff it is reachable.
                  - The then-statement is reachable iff the if-then statement is reachable.
                  - An if-then-else statement can complete normally iff the then-statement can
                    complete normally or the else-statement can complete normally.
                  - The then-statement is reachable iff the if-then-else statement is reachable.
                  - The else-statement is reachable iff the if-then-else statement is reachable.
                 */
                if (!n.getElseStmt().isPresent()) { // if-then
                    return true;
                } else { // if-then-else
                    return canCompleteNormally0(n.getThenStmt()) || canCompleteNormally0(n.getElseStmt().get());
                }
            }

            @Override
            public Boolean visit(AssertStmt n, Void arg) {
                // An assert statement can complete normally iff it is reachable.
                return true;
            }

            @Override
            public Boolean visit(ForEachStmt n, Void arg) {
                // An enhanced for statement can complete normally iff it is reachable.
                return true;
            }

            @Override
            public Boolean visit(BreakStmt n, Void arg) {
                // A break statement cannot complete normally.
                return false;
            }

            @Override
            public Boolean visit(ContinueStmt n, Void arg) {
                // A continue statement cannot complete normally.
                return false;
            }

            @Override
            public Boolean visit(ReturnStmt n, Void arg) {
                // A return statement cannot complete normally.
                return false;
            }

            @Override
            public Boolean visit(ThrowStmt n, Void arg) {
                // A throw statement cannot complete normally.
                return false;
            }

            @Override
            public Boolean visit(SwitchStmt n, Void arg) {
                /*
                  A switch block is reachable iff its switch statement is reachable.

                  A statement in a switch block is reachable iff its switch statement is reachable
                  and at least one of the following is true:
                  - It bears a case or default label.
                  - There is a statement preceding it in the switch block and that
                    preceding statement can complete normally.

                  A switch statement can complete normally iff at least one of the following is true:
                 */

                // The switch block is empty or contains only switch labels.
                if (n.getEntries().isEmpty()) {
                    return true;
                }

                // The switch block does not contain a default label.
                if (!containsDefaultLabel(n)) {
                    return true;
                }

                // There is at least one switch label after the last switch block statement group.
                // The last statement in the switch block can complete normally.
                SwitchEntry lastEntry = n.getEntry(n.getEntries().size() - 1);
                if (canSwitcbLabelCompleteNormally(lastEntry)) {
                    return true;
                }

                // There is a reachable break statement that exits the switch statement.
                List<BreakStmt> breaks = findAllBreak(n);
                if (!breaks.isEmpty()) {
                    for (BreakStmt stmt : breaks) {
                        if (isReachable(stmt)) {
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public Boolean visit(WhileStmt n, Void arg) {
                /*
                  A while statement can complete normally iff at least one of the following is true:
                 */

                // The while statement is reachable and the condition expression is not a
                // constant expression (§15.28) with value true.
                if (!n.getCondition().isBooleanLiteralExpr() || !((BooleanLiteralExpr) n.getCondition()).getValue()) {
                    return true;
                }

                // There is a reachable break statement that exits the while statement.
                List<BreakStmt> breaks = findAllBreak(n);
                if (!breaks.isEmpty()) {
                    for (BreakStmt stmt : breaks) {
                        if (isReachable(stmt)) {
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public Boolean visit(DoStmt n, Void arg) {
                /*
                   A do statement can complete normally iff at least one of the following is true:
                 */

                if (!n.getCondition().isBooleanLiteralExpr() || !((BooleanLiteralExpr) n.getCondition()).getValue()) {
                    // The contained statement can complete normally and the condition expression is not
                    // a constant expression (§15.28) with value true.
                    if (canCompleteNormally0(n.getBody())) {
                        return true;
                    }

                    // The do statement contains a reachable continue statement with no label,
                    // and the do statement is the innermost while, do, or for statement that contains
                    // that continue statement, and the continue statement continues that do statement,
                    // and the condition expression is not a constant expression with value true.
                    List<ContinueStmt> continues = findAllContinue(n);
                    if (!continues.isEmpty()) {
                        for (ContinueStmt stmt : continues) {
                            if (isReachable(stmt)) {
                                return true;
                            }
                        }
                    }

                    // The do statement contains a reachable continue statement with a label L,
                    // and the do statement has label L, and the continue statement continues that do statement,
                    // and the condition expression is not a constant expression with value true.
                    if (n.getParentNode().isPresent() &&
                            n.getParentNode().get() instanceof LabeledStmt &&
                            continueTheStatement((LabeledStmt) n.getParentNode().get(), n)) {
                        return true;
                    }
                }

                // There is a reachable break statement that exits the do statement.
                List<BreakStmt> breaks = findAllBreak(n);
                if (!breaks.isEmpty()) {
                    for (BreakStmt stmt : breaks) {
                        if (isReachable(stmt)) {
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public Boolean visit(ForStmt n, Void arg) {
                /*
                  A basic for statement can complete normally iff at least one of the following is true:
                 */

                // The for statement is reachable, there is a condition expression,
                // and the condition expression is not a constant expression (§15.28) with value true.
                if (n.getCompare().isPresent() && (!n.getCompare().get().isBooleanLiteralExpr() ||
                        !((BooleanLiteralExpr) n.getCompare().get()).getValue())) {
                    return true;
                }

                // There is a reachable break statement that exits the for statement.
                List<BreakStmt> breaks = findAllBreak(n);
                if (!breaks.isEmpty()) {
                    for (BreakStmt stmt : breaks) {
                        if (isReachable(stmt)) {
                            return true;
                        }
                    }
                }

                return false;
            }

            @Override
            public Boolean visit(SynchronizedStmt n, Void arg) {
                // A synchronized statement can complete normally iff the contained statement can complete normally.
                return canBlockCompleteNormally(n.getBody());
            }

            @Override
            public Boolean visit(TryStmt n, Void arg) {
                /*
                  The try block is reachable iff the try statement is reachable.
                  A try statement can complete normally iff both of the following are true:
                 */

                // The try block can complete normally or any catch block can complete normally.
                if (canBlockCompleteNormally(n.getTryBlock())) {
                    return true;
                } else {
                    /*
                      The Block of a catch block is reachable iff the catch block is reachable.
                      A catch block C is reachable iff both of the following are true:
                        - Either the type of C's parameter is an unchecked exception type or Throwable;
                          or some expression or throw statement in the try block is reachable and
                          can throw a checked exception whose type is assignable to the parameter of
                          the catch clause C. An expression is reachable iff the innermost statement
                          containing it is reachable.
                        - There is no earlier catch block A in the try statement such that the type
                          of C's parameter is the same as or a subclass of the type of A's parameter.
                      */
                    for (CatchClause catchClause : n.getCatchClauses()) {
                        if (canBlockCompleteNormally(catchClause.getBody())) {
                            return true;
                        }
                    }
                }

                // If a finally block is present, it is reachable iff the try statement is reachable.
                // If the try statement has a finally block, then the finally block can complete normally.
                return n.getFinallyBlock().isPresent() && canBlockCompleteNormally(n.getFinallyBlock().get());
            }
        };

        return statement.accept(visitor, null);
    }

    private boolean canBlockCompleteNormally(BlockStmt block) {
        // An empty block that is not a switch block can complete normally iff it is reachable.
        if (block.isEmpty()) {
            return true;
        } else {
            // A non-empty block that is not a switch block can complete normally iff the last statement
            // in it can complete normally.
            Statement lastStmt = block.getStatement(block.getStatements().size() - 1);
            return isReachableBecauseOfPosition(lastStmt) && canCompleteNormally0(lastStmt);
        }
    }

    private boolean canSwitcbLabelCompleteNormally(SwitchEntry entry) {
        if (entry.getStatements().isEmpty()) {
            return true;
        } else {
            for (Statement s : entry.getStatements()) {
                if (!canCompleteNormally0(s)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean continueTheStatement(LabeledStmt label, Statement statement) {
        List<ContinueStmt> continueStmts = statement.findAll(ContinueStmt.class);
        for (ContinueStmt stmt : continueStmts) {
            if (stmt.getLabel().isPresent() &&
                    stmt.getLabel().get().equals(label.getLabel()) &&
                    isReachable(stmt)) {
                return true;
            }
        }
        return false;
    }

    private boolean exitTheStatement(LabeledStmt label, Statement statement) {
        List<BreakStmt> breakStmts = statement.findAll(BreakStmt.class);
        for (BreakStmt stmt : breakStmts) {
            if (stmt.getLabel().isPresent() &&
                    stmt.getLabel().get().equals(label.getLabel()) &&
                    isReachable(stmt)) {
                return true;
            }
        }
        return false;
    }

    private List<ContinueStmt> findAllContinue(Statement continueTarget) {
        List<ContinueStmt> found = new ArrayList<>();
        GenericVisitorAdapter<ContinueStmt, Void> visitor = new GenericVisitorAdapter<ContinueStmt, Void>() {
            @Override
            public ContinueStmt visit(WhileStmt n, Void arg) {
                if (n == continueTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public ContinueStmt visit(DoStmt n, Void arg) {
                if (n == continueTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public ContinueStmt visit(ForEachStmt n, Void arg) {
                if (n == continueTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public ContinueStmt visit(ForStmt n, Void arg) {
                if (n == continueTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public ContinueStmt visit(ContinueStmt n, Void arg) {
                if (!n.getLabel().isPresent()) {
                    found.add(n);
                }
                return null;
            }
        };

        continueTarget.accept(visitor, null);
        return found;
    }

    private List<BreakStmt> findAllBreak(Statement breakTarget) {
        List<BreakStmt> found = new ArrayList<>();
        GenericVisitorAdapter<BreakStmt, Void> visitor = new GenericVisitorAdapter<BreakStmt, Void>() {
            @Override
            public BreakStmt visit(WhileStmt n, Void arg) {
                if (n == breakTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public BreakStmt visit(DoStmt n, Void arg) {
                if (n == breakTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public BreakStmt visit(ForEachStmt n, Void arg) {
                if (n == breakTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public BreakStmt visit(ForStmt n, Void arg) {
                if (n == breakTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public BreakStmt visit(SwitchStmt n, Void arg) {
                if (n == breakTarget) {
                    return super.visit(n, arg);
                } else {
                    return null;
                }
            }

            @Override
            public BreakStmt visit(BreakStmt n, Void arg) {
                if (!n.getLabel().isPresent()) {
                    found.add(n);
                }
                return null;
            }
        };

        breakTarget.accept(visitor, null);
        return found;
    }

    private boolean containsDefaultLabel(SwitchStmt switchStmt) {
        for (SwitchEntry entry : switchStmt.getEntries()) {
            if (entry.getLabels().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private <P extends Node> boolean parentIs(Node node, Class<P> parentClass) {
        if (node.getParentNode().isPresent()) {
            return parentClass.isInstance(node.getParentNode().get());
        } else {
            return false;
        }
    }
}