package nodes;

public class Constant extends INode {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    public Object value() {
        return value;
    }

    @Override
    public String graphViz() {
        return id() + " [label = \"const(" + value + ")\", style = filled, color = black, fillcolor = orange];\n";
    }
}
