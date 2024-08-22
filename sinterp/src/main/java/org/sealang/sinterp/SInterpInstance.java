package org.sealang.sinterp;

import java.util.HashMap;
import java.util.Map;

// class 의 instance 를 표현 하는 자료 구조
public class SInterpInstance {
    private SInterpClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    SInterpInstance(SInterpClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    public String toString() {
        return klass.name + " instace";
    }
}
