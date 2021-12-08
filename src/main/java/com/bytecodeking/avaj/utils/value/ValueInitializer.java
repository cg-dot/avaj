package com.bytecodeking.avaj.utils.value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

public class ValueInitializer {
    private static final List<ValueGenerator> OBJECT_VALUE_GENERATOR;
    private static final List<ValueGenerator> STRING_VALUE_GENERATOR;
    private static final List<ValueGenerator> DOUBLE_VALUE_GENERATOR;
    private static final List<ValueGenerator> INTEGER_VALUE_GENERATOR;
    private static final List<ValueGenerator> LONG_VALUE_GENERATOR;
    private static final List<ValueGenerator> BOOLEAN_VALUE_GENERATOR;

    static {
        OBJECT_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultObjectGenerator());
        }};
        STRING_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultStringGenerator());
        }};
        DOUBLE_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultDoubleGenerator());
        }};
        INTEGER_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultIntegerGenerator());
        }};
        LONG_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultLongGenerator());
        }};
        BOOLEAN_VALUE_GENERATOR = new ArrayList<ValueGenerator>() {{
            add(new DefaultBooleanGenerator());
        }};
    }

    public static LiteralExpr getDefault(Type type) {
        if (type.isPrimitiveType()) {
            return getPrimitiveGenerator(((PrimitiveType) type)).generate();
        } else { // Todo: Support Java 10 type inference
            return getReferenceGenerator(type).generate();
        }
    }

    private static ValueGenerator getPrimitiveGenerator(PrimitiveType type) {
        List<ValueGenerator> generators;
        switch (type.getType()) {
            case BOOLEAN:
                generators = BOOLEAN_VALUE_GENERATOR;
                break;
            case BYTE:
            case SHORT:
            case CHAR:
            case INT:
                generators = INTEGER_VALUE_GENERATOR;
                break;
            case LONG:
                generators = LONG_VALUE_GENERATOR;
                break;
            case FLOAT:
            case DOUBLE:
                generators = DOUBLE_VALUE_GENERATOR;
                break;
            default:
                generators = OBJECT_VALUE_GENERATOR;
        }
        return generators.get(ThreadLocalRandom.current().nextInt(generators.size()));
    }

    private static ValueGenerator getReferenceGenerator(Type type) {
        List<ValueGenerator> generators;
        if (isStringType(type)) {
            generators = STRING_VALUE_GENERATOR;
        } else {
            generators = OBJECT_VALUE_GENERATOR;
        }
        return generators.get(ThreadLocalRandom.current().nextInt(generators.size()));
    }

    private static boolean isStringType(Type type) {
        return type.isClassOrInterfaceType() && ("String".equals(type.asString()) || "java.lang.String".equals(type.asString()));
    }
}
