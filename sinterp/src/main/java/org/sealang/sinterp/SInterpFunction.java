package org.sealang.sinterp;

import java.util.List;

public class SInterpFunction implements SInterpCallable {
    private final Stmt.Function declaration;
    private final Environment closure;


    SInterpFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    SInterpFunction bind(SInterpInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new SInterpFunction(declaration, environment);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));// 심볼 테이블에 파라미터 등록
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

}
