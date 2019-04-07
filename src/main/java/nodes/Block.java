package nodes;

public class Block extends INode {
    private INode[] nodes;

    public Block(INode[] nodes) {
        this.nodes = nodes;
    }

    public INode[] nodes() {
        return nodes;
    }

    @Override
    public String graphViz() {
        StringBuilder graphViz = new StringBuilder();
        graphViz
                .append(id())
                .append(" [shape = point];\n");
        if (nodes.length > 1) {
            int[] graph = new int[nodes.length];
            for (int i = 0; i < graph.length; i++) {
                graph[i] = INode.counter++;
                graphViz
                        .append(graph[i])
                        .append(" [color = white, label = \"")
                        .append(i)
                        .append("\"];\n");
                graphViz
                        .append(id())
                        .append(" -- ")
                        .append(graph[i])
                        .append(";\n");
                graphViz
                        .append(nodes[i].graphViz());
                graphViz
                        .append(graph[i])
                        .append(" -- ")
                        .append(nodes[i].id())
                        .append(";\n");
            }
        } else
            graphViz
                    .append(nodes[0].graphViz())
                    .append(id())
                    .append(" -- ")
                    .append(nodes[0].id())
                    .append(";\n");
        return graphViz.toString();
    }
}
