package org.sealang.sinterp;

// class 의 instance 를 표현 하는 자료 구조
public class SInterpInstance {
    private SInterpClass klass;

    SInterpInstance(SInterpClass klass) {
        this.klass = klass;
    }

    public String toString() {
        return klass.name + " instace";
    }
}
