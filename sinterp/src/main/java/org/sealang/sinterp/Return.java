package org.sealang.sinterp;

/*
 Return 문은 행동이 예외처리와 비슷하기 때문에 RuntimeException 을 상속받아 구현한다.
 */
public class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
