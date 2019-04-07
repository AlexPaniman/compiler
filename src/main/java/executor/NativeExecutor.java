package executor;

import java.util.*;

public class NativeExecutor {
    private List<ExecutableMethod> executableMethods;
    private Map<String, Integer> functions;

    public NativeExecutor(Map<String, Integer> functions) {
        this.executableMethods = new ArrayList<>();
        this.functions = functions;
    }

    public NativeExecutor func(String name, int numCount, Function function) {
        executableMethods.add(new ExecutableMethod(functions.get(name), numCount, function));
        return this;
    }

    public NativeExecutor func(String name, int numCount, VoidFunction function) {
        func(name, numCount, o -> {
            function.apply(o);
            return null;
        });
        return this;
    }

    public Object invoke(int func, Stack<Object> stack) throws NoSuchMethodException {
        ExecutableMethod em = executableMethods
                .stream()
                .filter(executableMethod -> executableMethod.funcId() == func)
                .findAny()
                .orElseThrow(NoSuchMethodException::new);
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < em.numArgs(); i++)
            args.add(stack.pop());
        return em.func().apply(args.toArray());
    }
}
