package org.sealang.sinterp;

import java.util.List;

// class
public class SInterpClass implements SInterpCallable {
    final String name;

    SInterpClass(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        SInterpInstance instance = new SInterpInstance(this);
        return instance;
    }

    @Override
    public int arity() {
        return 0;
    }
}
