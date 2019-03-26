package nodes;

public class ShortIf extends INode {
    private static int counter = 0;
    private INode condition;
    private INode thenNode;

    public ShortIf(INode condition, INode thenNode) {
        this.condition = condition;
        this.thenNode = thenNode;
    }

    public INode condition() {
        return condition;
    }

    public INode thenNode() {
        return thenNode;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += condition.graphViz();
        graphViz += thenNode.graphViz();

        graphViz += "subgraph clusterIS" + counter ++ + " {\n";
        graphViz += "color = black;";
        graphViz += id() + " [label = \"if\", shape = square, style = filled];\n";

        int cond = INode.counter ++;
        int then = INode.counter ++;
        graphViz += cond + " [label = \"condition\", shape = egg];\n";
        graphViz += then + " [label = \"then\", shape = egg];\n";
        graphViz += "}\n";

        graphViz += id() + " -- " + cond + " [style=dotted];\n";
        graphViz += id() + " -- " + then + " [style=dotted];\n";

        graphViz += cond + " -- " + condition.id()  + ";\n";
        graphViz += then + " -- " + thenNode.id()  + ";\n";
        return graphViz;
    }
}
