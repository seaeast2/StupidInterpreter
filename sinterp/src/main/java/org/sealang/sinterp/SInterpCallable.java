package org.sealang.sinterp;

import java.util.List;

interface SInterpCallable {
    // SInterpCallable 에 올바른 개수의 인수가 전달됐는지 인터프리터가 검사할 때 사용
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
