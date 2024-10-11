package org.sealang.sinterp;

import java.util.List;
import java.util.Map;

// class
public class SInterpClass implements SInterpCallable {
    final String name;
    private final Map<String, SInterpFunction> methods;

    SInterpClass(String name, Map<String, SInterpFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    SInterpFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
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
