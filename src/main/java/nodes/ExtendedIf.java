package nodes;

public class ExtendedIf extends INode {
    private INode condition;
    private INode thenNode;
    private INode elseNode;

    public ExtendedIf(INode condition, INode thenNode, INode elseNode) {
        this.condition = condition;
        this.thenNode = thenNode;
        this.elseNode = elseNode;
    }

    public INode condition() {
        return condition;
    }

    public INode thenNode() {
        return thenNode;
    }

    public INode elseNode() {
        return elseNode;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += condition.graphViz();
        graphViz += thenNode.graphViz();
        graphViz += elseNode.graphViz();

        graphViz += "subgraph clusterIE" + counter ++ + " {\n";
        graphViz += "color = black;";
        graphViz += id() + " [label = \"if\", shape = square, style = filled];\n";

        int iCond = INode.counter ++;
        int iThen = INode.counter ++;
        int iElse = INode.counter ++;
        graphViz += iCond + " [label = \"condition\", shape = egg];\n";
        graphViz += iThen + " [label = \"then\", shape = egg];\n";
        graphViz += iElse + " [label = \"else\", shape = egg];\n";
        graphViz += "}\n";

        graphViz += id() + " -- " + iCond + " [style=dotted];\n";
        graphViz += id() + " -- " + iThen + " [style=dotted];\n";
        graphViz += id() + " -- " + iElse + " [style=dotted];\n";

        graphViz += iCond + " -- " + condition.id()  + ";\n";
        graphViz += iThen + " -- " + thenNode.id()  + ";\n";
        graphViz += iElse + " -- " + elseNode.id()  + ";\n";
        return graphViz;
    }
}
