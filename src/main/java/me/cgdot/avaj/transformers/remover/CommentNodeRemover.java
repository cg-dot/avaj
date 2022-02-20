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

package me.cgdot.avaj.transformers.remover;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import me.cgdot.avaj.transformers.ClassTransformer;

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
