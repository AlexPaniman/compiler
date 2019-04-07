package executor;

class ExecutableMethod {
    private int funcId;
    private Function func;
    private int numArgs;

    ExecutableMethod(int funcId, int numArgs, Function func) {
        this.funcId = funcId;
        this.func = func;
        this.numArgs = numArgs;
    }

    int funcId() {
        return funcId;
    }

    Function func() {
        return func;
    }

    int numArgs() {
        return numArgs;
    }
}
