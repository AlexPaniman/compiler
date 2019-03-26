package nodes;

public class FunctionCall extends INode {
    private String name;
    private INode[] variables;
    private boolean useReturn;

    public FunctionCall(String name, INode[] variables, boolean useReturn) {
        this.name = name;
        this.variables = variables;
        this.useReturn = useReturn;
    }

    public String name() {
        return name;
    }

    public INode[] variables() {
        return variables;
    }

    public boolean useReturn() {
        return useReturn;
    }

    @Override
    public String graphViz() {
        StringBuilder graphViz = new StringBuilder();
        graphViz.append(id()).append(" [label = \"call(").append(name).append(")\", shape = octagon, style = filled, color = black, fillcolor = lightblue];\n");
        for (INode node: variables) {
            graphViz.append(node.graphViz());
            graphViz.append(id()).append(" -- ").append(node.id()).append(";\n");
        }
        return graphViz.toString();
    }
}