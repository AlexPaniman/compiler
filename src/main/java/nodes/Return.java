package nodes;

public class Return extends INode {
    private INode expression;

    public Return(INode expression) {
        this.expression = expression;
    }

    public INode expression() {
        return expression;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += expression == null? "" : expression.graphViz();
        graphViz += id() + " [label = \"return\", shape = hexagon, color = red, style = filled, fillcolor = grey];\n";
        if (expression != null)
            graphViz += id() + " -- " + expression.id() + "\n";
        return graphViz;
    }
}
