package org.sealang.sinterp;

import java.util.HashMap;
import java.util.Map;

// class 의 instance 를 표현 하는 자료 구조
// class field 에 해당하는 정보들은 이곳에 저장한다.
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

        SInterpFunction method = klass.findMethod(name.lexeme);
        if (method != null)
            return method;

        throw new RuntimeError(name,
                "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    public String toString() {
        return klass.name + " instace";
    }
}
