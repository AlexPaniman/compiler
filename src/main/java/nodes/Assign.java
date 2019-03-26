package nodes;

public class Assign extends INode {
    private String variable;
    private INode expression;

    public Assign(String variable, INode expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public String variable() {
        return variable;
    }

    public INode expression() {
        return expression;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += expression.graphViz();
        graphViz += id() + " [label = \"assign[" + variable + "]\", shape = octane, style = filled, color = blue4, fillcolor = lightblue3];\n";
        graphViz += id() + " -- " + expression.id() + ";\n";
        return graphViz;
    }
}
