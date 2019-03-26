package nodes;

public class For extends INode {
    private static int counter = 0;
    private INode initializer;
    private INode condition;
    private INode iterator;
    private INode body;

    public For(INode initializer, INode condition, INode iterator, INode body) {
        this.initializer = initializer;
        this.condition = condition;
        this.iterator = iterator;
        this.body = body;
    }

    public INode initializer() {
        return initializer;
    }

    public INode condition() {
        return condition;
    }

    public INode iterator() {
        return iterator;
    }

    public INode body() {
        return body;
    }

    @Override
    public String graphViz() {
        String graphViz = "";
        graphViz += initializer.graphViz();
        graphViz += condition.graphViz();
        graphViz += iterator.graphViz();
        graphViz += body.graphViz();

        graphViz += "subgraph clusterF" + counter ++ + " {\n";

        graphViz += id() + " [label = \"for\", shape = square, style = filled];\n";

        int iInit = INode.counter ++;
        int iCond = INode.counter ++;
        int iIter = INode.counter ++;
        int iBody = INode.counter ++;

        graphViz += iInit + " [label = \"initializer\", shape = egg];\n";
        graphViz += iCond + " [label = \"condition\", shape = egg];\n";
        graphViz += iIter + " [label = \"iterator\", shape = egg];\n";
        graphViz += iBody + " [label = \"body\", shape = egg];\n";

        graphViz += "}\n";

        graphViz += id() + " -- " + iInit + " [style = dotted];\n";
        graphViz += id() + " -- " + iCond + " [style = dotted];\n";
        graphViz += id() + " -- " + iIter + " [style = dotted];\n";
        graphViz += id() + " -- " + iBody + " [style = dotted];\n";

        graphViz += iInit + " -- " + initializer.id()  + ";\n";
        graphViz += iCond + " -- " + condition.id()  + ";\n";
        graphViz += iIter + " -- " + iterator.id()  + ";\n";
        graphViz += iBody + " -- " + body.id()  + ";\n";
        return graphViz;
    }
}
