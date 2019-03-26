package nodes;

public class Variable extends INode {
    private String name;

    public Variable(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String graphViz() {
        return id() + " [label = \"var(" + name + ")\", color = black, style = filled, shape = egg, fillcolor = green4];\n";
    }
}
