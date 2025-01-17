package org.sealang.sinterp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// AST 실행
class Interpreter implements Expr.Visitor<Object>,
                             Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new SInterpCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }


    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
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
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        /*
        * "hello"() 와 같은 엉뚱한 호출이 일어나지 않도록 방어
        * */
        if (!(callee instanceof SInterpCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        SInterpCallable function = (SInterpCallable) callee;
        /*
        * 함수의 인자의 개수와 파라미터의 개수가 맞는지 체크
        * */
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof SInterpInstance) {
            return ((SInterpInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name,
                "Only instances have properties.");
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
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof SInterpInstance)) {
            throw new RuntimeError(expr.name,
                    "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((SInterpInstance)object).set(expr.name, value);
        return value;
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

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        //return environment.get(expr.name);
        return lookUpVariable(expr.name, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
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

    // AST 를 순회하면서 이름으로 symbol table 에서 매칭되는 Node 를 반환한다.
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

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous; // 예외가 발생하더라도 복구가 된다.
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.name.lexeme, null);

        Map<String, SInterpFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            SInterpFunction function = new SInterpFunction(method, environment);
            methods.put(method.name.lexeme, function);
        }

        SInterpClass klass = new SInterpClass(stmt.name.lexeme, methods);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        SInterpFunction function = new SInterpFunction(stmt, environment); // closure 구현을 위해 environment 저장
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }
}
