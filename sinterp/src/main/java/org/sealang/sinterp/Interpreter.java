package org.sealang.sinterp;

class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {
    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        }
        catch (RuntimeError error) {
            SInterp.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double) right;

            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double) right;

            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double) right;

            case BANG_EQUAL:
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double) right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String) right;
                }

                throw new RuntimeError(expr.operator,
                        "Operands must be two number or two strings.");

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double) right;

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double) right;
        }

        // unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // unreachable
        return null;
    }



    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers");
    }

    // 명시적인 null, bool 빼고는 모두 ! 연산에서 true 로 간주된다.
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    // 동등성 체크
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    // object 의 string 출력 수정
    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr) {
        /*
        accept 는 Expr.Visitor<R> 타입을 인자로 받으므로 accept 는
        Expr.Visitor<Object> 을 통해서
        'Object Expr.accept(Expr.Visitor<Object> visitor)' 으로 instantiation 된다.
        이때 Expr 은 구체 클래스의 부모 인터페이스 이므로 구체 클래스에 accept()
        함수의 실체가 추가된다.
        */
        return expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }
}
