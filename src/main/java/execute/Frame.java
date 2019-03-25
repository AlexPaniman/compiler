package execute;

import java.util.HashMap;
import java.util.Map;

class Frame {
    private int returnIndex;
    private Map<Object, Object> variables;

    Frame(int returnIndex) {
        this.returnIndex = returnIndex;
        this.variables = new HashMap<>();
    }

    int returnIndex() {
        return returnIndex;
    }

    Map<Object, Object> vars() {
        return variables;
    }

    @Override
    public String toString() {
        return "Frame(ret = " + returnIndex + ", vars = " + variables + ")";
    }
}
