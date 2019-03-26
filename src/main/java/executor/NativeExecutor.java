package executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class NativeExecutor {
    private Object executor;
    private Map<Integer, Method> methods;

    public NativeExecutor(Object executor) {
        this.executor = executor;
        this.methods = new HashMap<>();
        int counter = 0;
        for (Method method: executor.getClass().getDeclaredMethods())
            this.methods.put(counter ++, method);
    }

    public int get(String name) throws NoSuchMethodException {
        return methods
                .entrySet()
                .stream()
                .filter(e -> e
                        .getValue()
                        .getName()
                        .equals(name)
                )
                .findAny()
                .orElseThrow(NoSuchMethodException::new)
                .getKey();
    }

    public Object invoke(int func, Stack<Object> stack) throws InvocationTargetException, IllegalAccessException {
        Method method = methods.get(func);
        method.setAccessible(true);
        List<Object> args = new ArrayList<>();
        for(int i = 0; i < method.getParameters().length; i ++)
            args.add(stack.pop());
        return method.invoke(executor, args.toArray());
    }
}
