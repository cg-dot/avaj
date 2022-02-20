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

package me.cgdot.avaj.transformers.misc;

import com.github.javaparser.ast.body.*;
import me.cgdot.avaj.transformers.ClassTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
