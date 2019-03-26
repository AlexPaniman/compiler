package nodes;

public class While extends INode {
    private INode condition;
    private INode body;

    public While(INode condition, INode body) {
        this.condition = condition;
        this.body = body;
    }

    public INode condition() {
        return condition;
    }

    public INode body() {
        return body;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += condition.graphViz();
        graphViz += body.graphViz();

        graphViz += "subgraph clusterDW" + counter ++ + " {";

        graphViz += id() + " [label = \"while\", shape = square, style = filled];\n";

        int iCond = INode.counter ++;
        int iBody = INode.counter ++;
        graphViz += iCond + " [label = \"condition\", shape = egg];\n";
        graphViz += iBody + " [label = \"body\", shape = egg];\n";

        graphViz += "}\n";

        graphViz += id() + " -- " + iCond + " [style=dotted];";
        graphViz += id() + " -- " + iBody + " [style=dotted];";

        graphViz += iCond + " -- " + condition.id()  + ";\n";
        graphViz += iBody + " -- " + body.id() + ";\n";
        return graphViz;
    }
}
