/*
 * Avaj
 * Copyright (C) 2022 Cg <cg@bytecodeking.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.cgdot.avaj.visitors;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * {@link com.github.javaparser.ast.visitor.ModifierVisitor} with specific order.
 */
public class ModifierVisitor<A> extends com.github.javaparser.ast.visitor.ModifierVisitor<A> {

    @Override
    public Visitable visit(MethodDeclaration n, A arg) {
        NodeList<AnnotationExpr> annotations = modifyList(n.getAnnotations(), arg);
        NodeList<Modifier> modifiers = modifyList(n.getModifiers(), arg);
        Type type = (Type) n.getType().accept(this, arg);
        SimpleName name = (SimpleName) n.getName().accept(this, arg);
        NodeList<Parameter> parameters = modifyList(n.getParameters(), arg);
        ReceiverParameter receiverParameter = n.getReceiverParameter().map(s -> (ReceiverParameter) s.accept(this, arg)).orElse(null);
        NodeList<ReferenceType> thrownExceptions = modifyList(n.getThrownExceptions(), arg);
        NodeList<TypeParameter> typeParameters = modifyList(n.getTypeParameters(), arg);
        BlockStmt body = n.getBody().map(s -> (BlockStmt) s.accept(this, arg)).orElse(null);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (type == null || name == null)
            return null;
        n.setAnnotations(annotations);
        n.setModifiers(modifiers);
        n.setBody(body);
        n.setType(type);
        n.setName(name);
        n.setParameters(parameters);
        n.setReceiverParameter(receiverParameter);
        n.setThrownExceptions(thrownExceptions);
        n.setTypeParameters(typeParameters);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(DoStmt n, A arg) {
        Expression condition = (Expression) n.getCondition().accept(this, arg);
        Statement body = (Statement) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null || condition == null)
            return null;
        n.setBody(body);
        n.setCondition(condition);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(ForEachStmt n, A arg) {
        VariableDeclarationExpr variable = (VariableDeclarationExpr) n.getVariable().accept(this, arg);
        Expression iterable = (Expression) n.getIterable().accept(this, arg);
        Statement body = (Statement) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null || iterable == null || variable == null)
            return null;
        n.setBody(body);
        n.setIterable(iterable);
        n.setVariable(variable);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(ForStmt n, A arg) {
        NodeList<Expression> initialization = modifyList(n.getInitialization(), arg);
        Expression compare = n.getCompare().map(s -> (Expression) s.accept(this, arg)).orElse(null);
        NodeList<Expression> update = modifyList(n.getUpdate(), arg);
        Statement body = (Statement) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null)
            return null;
        n.setBody(body);
        n.setCompare(compare);
        n.setInitialization(initialization);
        n.setUpdate(update);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(TryStmt n, A arg) {
        NodeList<Expression> resources = modifyList(n.getResources(), arg);
        BlockStmt tryBlock = (BlockStmt) n.getTryBlock().accept(this, arg);
        NodeList<CatchClause> catchClauses = modifyList(n.getCatchClauses(), arg);
        BlockStmt finallyBlock = n.getFinallyBlock().map(s -> (BlockStmt) s.accept(this, arg)).orElse(null);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (tryBlock == null)
            return null;
        n.setCatchClauses(catchClauses);
        n.setFinallyBlock(finallyBlock);
        n.setResources(resources);
        n.setTryBlock(tryBlock);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(final CatchClause n, final A arg) {
        Parameter parameter = (Parameter) n.getParameter().accept(this, arg);
        BlockStmt body = (BlockStmt) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null || parameter == null)
            return null;
        n.setBody(body);
        n.setParameter(parameter);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(WhileStmt n, A arg) {
        Expression condition = (Expression) n.getCondition().accept(this, arg);
        Statement body = (Statement) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null || condition == null)
            return null;
        n.setBody(body);
        n.setCondition(condition);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(LambdaExpr n, A arg) {
        NodeList<Parameter> parameters = modifyList(n.getParameters(), arg);
        Statement body = (Statement) n.getBody().accept(this, arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (body == null)
            return null;
        n.setBody(body);
        n.setParameters(parameters);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(SwitchExpr n, A arg) {
        Expression selector = (Expression) n.getSelector().accept(this, arg);
        NodeList<SwitchEntry> entries = modifyList(n.getEntries(), arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (selector == null)
            return null;
        n.setEntries(entries);
        n.setSelector(selector);
        n.setComment(comment);
        return n;
    }

    @Override
    public Visitable visit(final SwitchStmt n, final A arg) {
        Expression selector = (Expression) n.getSelector().accept(this, arg);
        NodeList<SwitchEntry> entries = modifyList(n.getEntries(), arg);
        Comment comment = n.getComment().map(s -> (Comment) s.accept(this, arg)).orElse(null);
        if (selector == null)
            return null;
        n.setEntries(entries);
        n.setSelector(selector);
        n.setComment(comment);
        return n;
    }

    private <N extends Node> NodeList<N> modifyList(NodeList<N> list, A arg) {
        return (NodeList<N>) list.accept(this, arg);
    }
}
