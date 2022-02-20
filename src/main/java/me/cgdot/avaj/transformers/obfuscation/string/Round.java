package me.cgdot.avaj.transformers.obfuscation.string;

import com.github.javaparser.ast.stmt.Statement;
import me.cgdot.avaj.transformers.obfuscation.string.operators.AbstractOperator;
import org.apache.commons.lang3.ArrayUtils;

public class Round {
    private final AbstractOperator operator;
    private final Statement statement;
    private final int[] constants;

    public Round(AbstractOperator operator, Statement statement, int[] constants) {
        this.operator = operator;
        this.statement = statement;
        this.constants = constants;
    }

    public AbstractOperator getOperator() {
        return operator;
    }

    public Statement getStatement() {
        return statement;
    }

    public int[] getConstants() {
        return constants;
    }

    public int getResult(int value, int... csts) {
        return operator.doRound(value, ArrayUtils.addAll(constants, csts));
    }
}
